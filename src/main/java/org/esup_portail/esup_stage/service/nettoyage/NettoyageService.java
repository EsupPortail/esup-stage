package org.esup_portail.esup_stage.service.nettoyage;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ArchivageProgressionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Nettoyage des contacts et services d'accueil qui ne sont plus utilisés.
 *
 * Contexte : les étudiants peuvent créer des contacts et des services lors de la saisie de
 * leur convention mais ne peuvent ni les modifier ni les supprimer. Ce fonctionnement génère
 * au fil du temps des créations orphelines (fautes de frappe, doublons abandonnés...) qui
 * alourdissent les listes de sélection. Ces tâches suppriment définitivement les enregistrements
 * qui ne sont réellement plus utilisés.
 *
 * Règle métier retenue :
 * - un contact/service encore référencé par une convention ou un avenant est conservé, même si
 *   cette convention est archivée : la convention archivée pointe toujours vers lui et doit rester
 *   consultable avec son tuteur et son service. Ces enregistrements ne seront supprimés qu'à la
 *   purge de la convention (voir ArchivageService) ;
 * - un contact encore rattaché à une offre ou à un accord de partenariat est conservé (usage réel,
 *   et clé étrangère obligatoire pour l'accord) ;
 * - un token d'évaluation tuteur encore valide (non expiré) conserve son contact ; les tokens
 *   expirés, eux, sont supprimés en même temps que le contact orphelin qu'ils référencent.
 *
 * Performance : les enregistrements à supprimer sont récupérés via une projection (et non des
 * entités), pour éviter tout N+1 et ne pas peupler le contexte de persistance — sans quoi le
 * dirty-checking rejoué à chaque lot de suppression rendrait le traitement quadratique. Les
 * critères et les suppressions sont portés par les repositories ; ce service se limite à
 * orchestrer, tracer l'avancement et produire un CSV récapitulatif dans le répertoire de logs
 * serveur (appli.logs_dir), nommé d'après la tâche et son horodatage.
 */
@Slf4j
@org.springframework.stereotype.Service
public class NettoyageService {

    // Taille des lots pour les suppressions par liste d'identifiants (évite les requêtes IN géantes)
    private static final int TAILLE_LOT = 1000;

    // Palier de journalisation de l'avancement : un log tous les N enregistrements traités
    private static final int PALIER_LOG = 5000;

    @Autowired
    private ContactJpaRepository contactJpaRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private EvaluationTuteurTokenJpaRepository evaluationTuteurTokenJpaRepository;

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private org.esup_portail.esup_stage.repository.ContactRepository contactRepository;

    @Autowired
    private org.esup_portail.esup_stage.repository.ServiceRepository serviceRepository;

    // Filtre « inutilisé » à passer aux repositories de pagination
    private static final String FILTRE_INUTILISE = "{\"inutilise\":{\"specific\":true,\"value\":true}}";

    /**
     * Cache court des compteurs d'inutilisés : le dénombrement est coûteux (sous-requêtes
     * d'existence sur toute la table) et la page d'administration l'interroge à chaque
     * ouverture d'onglet. Le cache est invalidé dès qu'un nettoyage a supprimé des données.
     */
    private static final long TTL_CACHE_COMPTEURS_MS = 120_000;
    private final java.util.Map<String, long[]> cacheCompteurs = new java.util.concurrent.ConcurrentHashMap<>();

    public long compterContactsInutilises() {
        return compteurCache("contacts", () -> contactRepository.count(FILTRE_INUTILISE));
    }

    public long compterServicesInutilises() {
        return compteurCache("services", () -> serviceRepository.count(FILTRE_INUTILISE));
    }

    private long compteurCache(String cle, java.util.function.Supplier<Long> calcul) {
        long maintenant = System.currentTimeMillis();
        long[] entree = cacheCompteurs.get(cle);
        if (entree != null && entree[1] > maintenant) {
            return entree[0];
        }
        long valeur = calcul.get();
        cacheCompteurs.put(cle, new long[]{valeur, maintenant + TTL_CACHE_COMPTEURS_MS});
        return valeur;
    }

    private void invaliderCacheCompteurs() {
        cacheCompteurs.clear();
    }

    // Colonnes des exports/récapitulatifs, réutilisées par le rapport Excel du lancement manuel
    private static final List<String> COLONNES_CONTACT = Arrays.asList("N° du contact", "Nom", "Prénom", "Mail", "Téléphone", "Fonction", "Service", "Établissement d'accueil", "Login création", "Date de création");
    private static final List<String> COLONNES_SERVICE = Arrays.asList("N° du service", "Nom", "Voie", "Code postal", "Commune", "Établissement d'accueil", "Login création", "Date de création");

    // ------------------------------------------------------------------
    // Lancement manuel depuis la page d'administration : traitement asynchrone,
    // suivi de progression (n/total), annulation et rapport Excel des supprimés.
    // ------------------------------------------------------------------

    private final ArchivageProgressionDto progression = new ArchivageProgressionDto();
    private final AtomicBoolean annulationDemandee = new AtomicBoolean(false);
    private final List<Object[]> rapport = Collections.synchronizedList(new ArrayList<>());
    private volatile String rapportType = "";

    private final ExecutorService executeurManuel = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "nettoyage-manuel");
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    public void arreterExecuteur() {
        executeurManuel.shutdownNow();
    }

    /**
     * Lance en arrière-plan le nettoyage des contacts ou des services inutilisés. Un seul
     * traitement à la fois ; l'avancement est suivi via {@link #getProgression()}.
     */
    public void demarrerNettoyageManuel(String type) {
        boolean services = "services".equalsIgnoreCase(type);
        synchronized (progression) {
            if (progression.isEnCours()) {
                throw new AppException(HttpStatus.CONFLICT, "Un nettoyage est déjà en cours");
            }
            progression.setEnCours(true);
            progression.setTache(services ? "Nettoyage des services" : "Nettoyage des contacts");
            progression.setEtape("Démarrage");
            progression.setTraitees(0);
            progression.setTotal(0);
            progression.setDateDebut(new Date());
            progression.setDateFin(null);
            progression.setMessage(null);
            progression.setErreur(false);
            progression.setAnnule(false);
            progression.setRapportDisponible(false);
            progression.setRapportNbLignes(0);
        }
        annulationDemandee.set(false);
        rapport.clear();
        rapportType = services ? "Services" : "Contacts";
        executeurManuel.submit(() -> {
            String bilan = null;
            String erreur = null;
            try {
                bilan = services ? nettoyerServicesAvecSuivi() : nettoyerContactsAvecSuivi();
            } catch (Exception e) {
                log.error("Échec du nettoyage manuel {} : {}", type, e.getMessage(), e);
                erreur = "Échec du nettoyage : " + e.getMessage();
            }
            // Des données ont été supprimées : les compteurs mis en cache ne sont plus valides
            invaliderCacheCompteurs();
            synchronized (progression) {
                progression.setEnCours(false);
                progression.setDateFin(new Date());
                progression.setEtape("Terminé");
                progression.setMessage(erreur != null ? erreur : bilan);
                progression.setErreur(erreur != null);
                progression.setAnnule(annulationDemandee.get());
                progression.setRapportNbLignes(rapport.size());
                progression.setRapportDisponible(!rapport.isEmpty());
            }
            annulationDemandee.set(false);
        });
    }

    public void demanderAnnulation() {
        synchronized (progression) {
            if (!progression.isEnCours()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Aucun nettoyage en cours");
            }
            progression.setEtape(progression.getEtape() + " (annulation en cours)");
        }
        annulationDemandee.set(true);
        log.info("Annulation du nettoyage demandée");
    }

    public ArchivageProgressionDto getProgression() {
        synchronized (progression) {
            ArchivageProgressionDto copie = new ArchivageProgressionDto();
            copie.setEnCours(progression.isEnCours());
            copie.setTache(progression.getTache());
            copie.setEtape(progression.getEtape());
            copie.setTraitees(progression.getTraitees());
            copie.setTotal(progression.getTotal());
            copie.setDateDebut(progression.getDateDebut());
            copie.setDateFin(progression.getDateFin());
            copie.setMessage(progression.getMessage());
            copie.setErreur(progression.isErreur());
            copie.setAnnule(progression.isAnnule());
            copie.setRapportDisponible(progression.isRapportDisponible());
            copie.setRapportNbLignes(progression.getRapportNbLignes());
            return copie;
        }
    }

    private void progressionEtape(String etape, long total) {
        synchronized (progression) {
            progression.setEtape(etape);
            progression.setTraitees(0);
            progression.setTotal(total);
        }
    }

    private void progressionAvance(long traitees) {
        synchronized (progression) {
            progression.setTraitees(traitees);
        }
    }

    /**
     * Nettoyage manuel des contacts : suppression par lots transactionnels (annulable entre
     * lots) avec suivi de progression et alimentation du rapport exportable.
     */
    private String nettoyerContactsAvecSuivi() {
        List<Object[]> lignes = contactJpaRepository.findInutilisesPourNettoyage(new Date());
        progressionEtape("Suppression des contacts", lignes.size());
        int supprimes = traiterParLots(lignes, lot -> {
            List<Integer> ids = lot.stream().map(l -> (Integer) l[0]).collect(Collectors.toList());
            // Les tokens expirés référençant ces contacts sont supprimés d'abord (clé étrangère)
            evaluationTuteurTokenJpaRepository.deleteByContactIdIn(ids);
            return contactJpaRepository.deleteByIdIn(ids);
        });
        return String.format("%s : %d contact(s) supprimé(s)",
                estAnnule() ? "Nettoyage des contacts interrompu à la demande de l'utilisateur" : "Nettoyage des contacts terminé", supprimes);
    }

    private String nettoyerServicesAvecSuivi() {
        List<Object[]> lignes = serviceJpaRepository.findInutilisesPourNettoyage();
        progressionEtape("Suppression des services", lignes.size());
        int supprimes = traiterParLots(lignes, lot -> {
            List<Integer> ids = lot.stream().map(l -> (Integer) l[0]).collect(Collectors.toList());
            return serviceJpaRepository.deleteByIdIn(ids);
        });
        return String.format("%s : %d service(s) supprimé(s)",
                estAnnule() ? "Nettoyage des services interrompu à la demande de l'utilisateur" : "Nettoyage des services terminé", supprimes);
    }

    /**
     * Supprime par lots transactionnels via la fonction fournie, en vérifiant l'annulation
     * entre chaque lot, en avançant la progression et en alimentant le rapport avec les lignes
     * effectivement supprimées.
     */
    private int traiterParLots(List<Object[]> lignes, Function<List<Object[]>, Integer> suppressionLot) {
        int total = lignes.size();
        int supprimes = 0;
        int traites = 0;
        int prochainPalier = PALIER_LOG;
        for (int i = 0; i < total; i += TAILLE_LOT) {
            if (estAnnule()) {
                log.info("Nettoyage interrompu à la demande de l'utilisateur ({}/{})", traites, total);
                break;
            }
            List<Object[]> lot = lignes.subList(i, Math.min(i + TAILLE_LOT, total));
            supprimes += transactionTemplate.execute(status -> suppressionLot.apply(lot));
            rapport.addAll(lot);
            traites += lot.size();
            progressionAvance(traites);
            if (traites >= prochainPalier || traites == total) {
                log.info("Nettoyage : {}/{} traité(s)", traites, total);
                prochainPalier = traites + PALIER_LOG;
            }
        }
        return supprimes;
    }

    private boolean estAnnule() {
        return annulationDemandee.get();
    }

    /**
     * Génère le classeur Excel des contacts/services supprimés par le dernier nettoyage manuel.
     */
    public byte[] exportRapportExcel() {
        List<Object[]> lignes;
        synchronized (rapport) {
            lignes = new ArrayList<>(rapport);
        }
        boolean services = "Services".equals(rapportType);
        List<String> colonnes = services ? COLONNES_SERVICE : COLONNES_CONTACT;
        int nbCol = colonnes.size();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(rapportType.isEmpty() ? "Nettoyage" : rapportType);
            CellStyle entete = workbook.createCellStyle();
            Font gras = workbook.createFont();
            gras.setBold(true);
            entete.setFont(gras);

            Row ligneEntete = sheet.createRow(0);
            for (int i = 0; i < nbCol; i++) {
                Cell cell = ligneEntete.createCell(i);
                cell.setCellValue(colonnes.get(i));
                cell.setCellStyle(entete);
            }

            int numLigne = 1;
            for (Object[] l : lignes) {
                Row row = sheet.createRow(numLigne++);
                // La dernière colonne (dateCreation) est une Date à formater ; les autres sont des chaînes/entiers
                for (int i = 0; i < nbCol; i++) {
                    Object valeur = i < l.length ? l[i] : null;
                    Cell cell = row.createCell(i);
                    if (i == nbCol - 1 && valeur instanceof Date date) {
                        cell.setCellValue(df.format(date));
                    } else {
                        cell.setCellValue(valeur != null ? String.valueOf(valeur) : "");
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la génération du fichier Excel");
        }
    }

    /**
     * Supprime les contacts qui ne sont plus référencés par aucune donnée active. Les éventuels
     * tokens d'évaluation expirés pointant sur ces contacts orphelins sont supprimés au passage.
     *
     * @return le nombre de contacts supprimés
     */
    @Transactional
    public int supprimerContactsInutilises() {
        List<Object[]> lignes = contactJpaRepository.findInutilisesPourNettoyage(new Date());
        if (lignes.isEmpty()) {
            log.info("Nettoyage des contacts inutilisés : aucun contact à supprimer");
            return 0;
        }

        List<Integer> ids = lignes.stream().map(l -> (Integer) l[0]).collect(Collectors.toList());
        log.info("Nettoyage des contacts inutilisés : {} contact(s) à supprimer", ids.size());

        // Les tokens expirés référençant ces contacts sont supprimés d'abord (contrainte de clé étrangère)
        int nbTokens = supprimerParLots(ids, evaluationTuteurTokenJpaRepository::deleteByContactIdIn, null);
        int nb = supprimerParLots(ids, contactJpaRepository::deleteByIdIn, "contacts");

        ecrireRecapCsv("SupprimerContactsInutilises",
                Arrays.asList("idContact", "nom", "prenom", "mail", "telephone", "fonction", "service", "structure", "loginCreation", "dateCreation"),
                lignes, NettoyageService::ligneContact);
        log.info("Nettoyage des contacts inutilisés terminé : {} contact(s) supprimé(s) ({} token(s) d'évaluation expiré(s) supprimé(s) au passage)", nb, nbTokens);
        return nb;
    }

    /**
     * Supprime les services qui n'ont plus aucun contact et qui ne sont référencés ni par une
     * convention ni par un avenant. À lancer de préférence après le nettoyage des contacts : la
     * suppression des contacts orphelins peut rendre de nouveaux services supprimables.
     *
     * @return le nombre de services supprimés
     */
    @Transactional
    public int supprimerServicesInutilises() {
        List<Object[]> lignes = serviceJpaRepository.findInutilisesPourNettoyage();
        if (lignes.isEmpty()) {
            log.info("Nettoyage des services inutilisés : aucun service à supprimer");
            return 0;
        }

        List<Integer> ids = lignes.stream().map(l -> (Integer) l[0]).collect(Collectors.toList());
        log.info("Nettoyage des services inutilisés : {} service(s) à supprimer", ids.size());

        int nb = supprimerParLots(ids, serviceJpaRepository::deleteByIdIn, "services");

        ecrireRecapCsv("SupprimerServicesInutilises",
                Arrays.asList("idService", "nom", "voie", "codePostal", "commune", "structure", "loginCreation", "dateCreation"),
                lignes, NettoyageService::ligneService);
        log.info("Nettoyage des services inutilisés terminé : {} service(s) supprimé(s)", nb);
        return nb;
    }

    /**
     * Supprime les identifiants par lots via la fonction de suppression fournie (un repository).
     * Si {@code libelle} est non nul, journalise l'avancement (n/N) tous les {@link #PALIER_LOG}
     * enregistrements traités, ainsi qu'à la fin.
     *
     * @return le nombre total de lignes effectivement supprimées
     */
    private int supprimerParLots(List<Integer> ids, Function<List<Integer>, Integer> suppression, String libelle) {
        int total = ids.size();
        int supprimes = 0;
        int traites = 0;
        int prochainPalier = PALIER_LOG;
        for (int i = 0; i < total; i += TAILLE_LOT) {
            List<Integer> lot = ids.subList(i, Math.min(i + TAILLE_LOT, total));
            supprimes += suppression.apply(lot);
            traites += lot.size();
            if (libelle != null && (traites >= prochainPalier || traites == total)) {
                log.info("Nettoyage des {} inutilisés : {}/{} traité(s)", libelle, traites, total);
                prochainPalier = traites + PALIER_LOG;
            }
        }
        return supprimes;
    }

    // Projection contact : [id, nom, prenom, mail, tel, fonction, service, structure, loginCreation, dateCreation]
    private static List<String> ligneContact(Object[] l) {
        return Arrays.asList(
                str(l[0]), str(l[1]), str(l[2]), str(l[3]), str(l[4]), str(l[5]), str(l[6]), str(l[7]), str(l[8]), formatDate((Date) l[9])
        );
    }

    // Projection service : [id, nom, voie, codePostal, commune, structure, loginCreation, dateCreation]
    private static List<String> ligneService(Object[] l) {
        return Arrays.asList(
                str(l[0]), str(l[1]), str(l[2]), str(l[3]), str(l[4]), str(l[5]), str(l[6]), formatDate((Date) l[7])
        );
    }

    /**
     * Écrit le récapitulatif CSV dans le répertoire de logs serveur, en flux (sans tout charger en
     * mémoire). Le fichier est nommé &lt;nomTache&gt;_&lt;horodatage&gt;.csv. Une erreur d'écriture est
     * journalisée mais n'interrompt pas la tâche (les suppressions sont déjà effectuées).
     */
    private void ecrireRecapCsv(String nomTache, List<String> entete, List<Object[]> lignes, Function<Object[], List<String>> mapper) {
        String horodatage = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        Path dossier = Paths.get(appliProperties.getLogsDir());
        Path fichier = dossier.resolve(nomTache + "_" + horodatage + ".csv");
        try {
            Files.createDirectories(dossier);
            try (BufferedWriter writer = Files.newBufferedWriter(fichier, StandardCharsets.UTF_8)) {
                writer.write(0xFEFF); // BOM UTF-8 pour l'ouverture directe dans Excel
                writer.write(ligneCsv(entete));
                for (Object[] ligne : lignes) {
                    writer.write(ligneCsv(mapper.apply(ligne)));
                }
            }
            log.info("Récapitulatif de la tâche {} écrit dans {}", nomTache, fichier.toAbsolutePath());
        } catch (IOException e) {
            log.error("Impossible d'écrire le récapitulatif CSV {} : {}", fichier, e.getMessage(), e);
        }
    }

    private static String ligneCsv(List<String> valeurs) {
        return valeurs.stream().map(NettoyageService::echapperCsv).collect(Collectors.joining(";")) + "\r\n";
    }

    private static String echapperCsv(String valeur) {
        if (valeur == null) {
            return "";
        }
        if (valeur.contains(";") || valeur.contains("\"") || valeur.contains("\n") || valeur.contains("\r")) {
            return "\"" + valeur.replace("\"", "\"\"") + "\"";
        }
        return valeur;
    }

    private static String str(Object valeur) {
        return valeur == null ? "" : String.valueOf(valeur);
    }

    private static String formatDate(Date date) {
        return date != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date) : "";
    }
}
