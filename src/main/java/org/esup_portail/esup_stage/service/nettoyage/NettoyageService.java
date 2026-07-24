package org.esup_portail.esup_stage.service.nettoyage;

import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
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
