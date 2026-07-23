package org.esup_portail.esup_stage.service.archivage;

import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.dto.ArchivageProgressionDto;
import org.esup_portail.esup_stage.dto.ArchivageStatistiquesDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConventionTraiteeDto;
import org.esup_portail.esup_stage.dto.SimulationArchivageResumeDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.EtudiantGroupeEtudiant;
import org.esup_portail.esup_stage.model.GroupeEtudiant;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionDocumentEtudiantService;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Archivage et purge des conventions et de leurs organismes d'accueil.
 *
 * - Archivage : les conventions dont le stage est terminé depuis plus de N années (config
 *   générale, 5 ans par défaut) sont marquées archivées (dateArchivage). Elles ne restent
 *   alors visibles que pour les super-admins, en lecture seule. Les structures dont toutes
 *   les conventions sont archivées sont archivées en même temps.
 * - Purge : M années après leur archivage (2 ans par défaut), les conventions archivées et
 *   leurs données liées (documents déposés, avenants, historiques, tokens d'évaluation...)
 *   sont supprimées définitivement. Les structures archivées depuis le même délai sont
 *   supprimées si plus aucune donnée ne les référence.
 */
@Slf4j
@Service
public class ArchivageService {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @Autowired
    private ConventionDocumentEtudiantService conventionDocumentEtudiantService;

    @Autowired
    private ConventionDocumentEtudiantHistoriqueJpaRepository conventionDocumentEtudiantHistoriqueJpaRepository;

    @Autowired
    private EvaluationTuteurTokenJpaRepository evaluationTuteurTokenJpaRepository;

    @Autowired
    private PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;

    @Autowired
    private EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;

    @Autowired
    private GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;

    @Autowired
    private HistoriqueStructureJpaRepository historiqueStructureJpaRepository;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private ContactJpaRepository contactJpaRepository;

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private AppliProperties appliProperties;

    @Autowired
    private CronTaskJpaRepository cronTaskJpaRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // État d'avancement partagé avec la page d'administration (polling)
    private final ArchivageProgressionDto progression = new ArchivageProgressionDto();

    // Demande d'annulation du traitement en cours : vérifiée entre chaque convention/structure.
    // L'annulation est sans risque : chaque élément est traité dans sa propre transaction,
    // ce qui est déjà fait reste acquis et le reste sera repris au prochain lancement.
    private final AtomicBoolean annulationDemandee = new AtomicBoolean(false);

    // Rapport des conventions effectivement traitées par le dernier traitement (pour export Excel).
    // Alimenté au fil de l'eau après commit de chaque convention ; capturé pendant le
    // traitement car la purge supprime définitivement les conventions.
    private final List<ConventionTraiteeDto> rapport = Collections.synchronizedList(new ArrayList<>());
    private volatile String rapportType = "";

    // Exécuteur des lancements manuels : un seul traitement à la fois
    private final ExecutorService executeurManuel = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "archivage-manuel");
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    public void arreterExecuteur() {
        executeurManuel.shutdownNow();
    }

    /**
     * Lance l'archivage ou la purge en arrière-plan. L'avancement est suivi via
     * {@link #getProgression()} ; un seul traitement peut être en cours à la fois.
     */
    public void demarrerTacheManuelle(String type) {
        boolean purge = "purge".equalsIgnoreCase(type);
        synchronized (progression) {
            if (progression.isEnCours()) {
                throw new AppException(HttpStatus.CONFLICT, "Un traitement d'archivage ou de purge est déjà en cours");
            }
            progression.setEnCours(true);
            progression.setTache(purge ? "Purge" : "Archivage");
            progression.setEtape("Démarrage");
            progression.setTraitees(0);
            progression.setTotal(0);
            progression.setDateDebut(new Date());
            progression.setDateFin(null);
            progression.setMessage(null);
            progression.setErreur(false);
            progression.setAnnule(false);
        }
        annulationDemandee.set(false);
        executeurManuel.submit(() -> {
            String bilan = null;
            String erreur = null;
            try {
                bilan = purge ? purger() : archiver();
                majDerniereExecution(purge ? "PurgerConventions" : "ArchiverConventions");
            } catch (Exception e) {
                log.error("Échec du traitement manuel {} : {}", purge ? "purge" : "archivage", e.getMessage(), e);
                erreur = "Échec du traitement : " + e.getMessage();
            }
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

    /**
     * Demande l'arrêt du traitement en cours. L'arrêt est effectif à la fin de l'élément en
     * cours de traitement : rien n'est perdu ni corrompu, ce qui reste sera repris au
     * prochain lancement.
     */
    public void demanderAnnulation() {
        synchronized (progression) {
            if (!progression.isEnCours()) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Aucun traitement en cours");
            }
            progression.setEtape(progression.getEtape() + " (annulation en cours)");
        }
        annulationDemandee.set(true);
        log.info("Annulation du traitement d'archivage/purge demandée");
    }

    private boolean estAnnule() {
        return annulationDemandee.get();
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

    private void majDerniereExecution(String nomTache) {
        CronTask cronTask = cronTaskJpaRepository.findByNom(nomTache);
        if (cronTask != null) {
            cronTask.setDateDernierExecution(new Date());
            cronTaskJpaRepository.save(cronTask);
        }
    }

    /**
     * Archive les conventions terminées depuis plus de N années (délais distincts pour les
     * conventions avec et sans gratification) puis les structures dont toutes les conventions
     * sont archivées. Réactive au passage les structures archivées qui auraient été
     * réutilisées sur une convention active, et déplace les fichiers des conventions
     * archivées vers le dossier d'archives du serveur.
     */
    public String archiver() {
        Date now = new Date();
        Date seuilSansGratification = getSeuilArchivageSansGratification(now);
        Date seuilAvecGratification = getSeuilArchivageAvecGratification(now);
        demarrerRapport("Archivage");

        // Réparation préalable : les conventions marquées archivées sans que leurs fichiers
        // aient été traités (état hérité d'une ancienne version ou d'un incident) sont
        // réactivées ; elles repassent par le circuit d'archivage atomique ci-dessous
        int nbConventionsReactivees = conventionJpaRepository.reactiverConventionsSansFichiersTraites();
        if (nbConventionsReactivees > 0) {
            log.info("Réparation : {} convention(s) marquée(s) archivée(s) sans fichiers traités réactivée(s)", nbConventionsReactivees);
        }

        // Archivage convention par convention : les fichiers sont déplacés et la convention
        // marquée archivée dans la même transaction. Une convention n'est donc jamais
        // considérée archivée tant que ses fichiers n'ont pas été traités, et une annulation
        // laisse le reste des conventions pleinement actives, fichiers compris.
        List<Integer> ids = conventionJpaRepository.findIdsConventionsAArchiver(seuilSansGratification, seuilAvecGratification);
        log.info("Archivage : {} convention(s) à archiver", ids.size());
        progressionEtape("Archivage des conventions", ids.size());
        int nbConventions = 0;
        int traitees = 0;
        for (Integer id : ids) {
            if (estAnnule()) {
                log.info("Archivage des conventions interrompu à la demande de l'utilisateur ({}/{})", traitees, ids.size());
                break;
            }
            try {
                // La ligne de rapport n'est ajoutée qu'après commit réussi de la transaction
                ConventionTraiteeDto ligne = transactionTemplate.execute(status -> archiverConvention(id));
                if (ligne != null) {
                    nbConventions++;
                    rapport.add(ligne);
                }
            } catch (Exception e) {
                log.error("Échec de l'archivage de la convention {} : {}", id, e.getMessage(), e);
            } finally {
                // Vide le contexte de persistance : en exécution manuelle (open-in-view), la session
                // couvre toute la requête HTTP et chaque convention chargée s'y accumule — sans ce
                // clear, le dirty-checking de l'auto-flush rend le traitement quadratique
                em.clear();
            }
            traitees++;
            progressionAvance(traitees);
            if (traitees % 500 == 0) {
                log.info("Archivage : {}/{} convention(s) traitée(s)", traitees, ids.size());
            }
        }

        int nbStructuresReactivees = 0;
        int nbStructures = 0;
        if (!estAnnule()) {
            progressionEtape("Archivage des structures", 0);
            nbStructuresReactivees = structureJpaRepository.desarchiverStructuresReutilisees();
            nbStructures = structureJpaRepository.archiverStructuresSansConventionActive(now);
        }

        String bilan = String.format("%s : %d convention(s) archivée(s) (fichiers inclus), %d structure(s) archivée(s), %d structure(s) réactivée(s)",
                estAnnule() ? "Archivage interrompu à la demande de l'utilisateur" : "Archivage terminé",
                nbConventions, nbStructures, nbStructuresReactivees);
        if (nbConventionsReactivees > 0) {
            bilan += String.format(", %d convention(s) à l'état incohérent réactivée(s)", nbConventionsReactivees);
        }
        log.info("{} (seuils : sans gratification {}, avec gratification {})", bilan, seuilSansGratification, seuilAvecGratification);
        return bilan;
    }

    /**
     * Archive une convention de manière atomique : déplacement de ses fichiers puis marquage
     * dateArchivage/dateArchivageFichiers dans la même transaction. En cas d'échec sur les
     * fichiers, la convention reste active et sera retentée au prochain passage.
     */
    private ConventionTraiteeDto archiverConvention(int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null || convention.getDateArchivage() != null) {
            return null;
        }
        deplacerFichiers(convention);
        Date maintenant = new Date();
        convention.setDateArchivage(maintenant);
        convention.setDateArchivageFichiers(maintenant);
        conventionJpaRepository.save(convention);
        return ConventionTraiteeDto.from(convention);
    }

    private void deplacerFichiers(Convention convention) {
        Path racineArchives = getDossierArchives();
        try {
            // Les documents déposés (noms techniques UUID) sont regroupés dans un dossier portant l'id de la convention
            int nbDocuments = conventionDocumentEtudiantService.archiverFichiers(convention, racineArchives.resolve("convention_" + convention.getId()));
            // Les PDF signés sont déposés directement à la racine : leur nom contient déjà l'id de la convention
            int nbSignes = archiverFichiersSignes(convention, racineArchives);
            if (nbDocuments > 0 || nbSignes > 0) {
                log.info("Convention {} : {} document(s) déposé(s) et {} PDF signé(s) déplacés vers {}", convention.getId(), nbDocuments, nbSignes, racineArchives);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du déplacement des fichiers vers " + racineArchives, e);
        }
    }

    /**
     * Racine du dossier d'archives du serveur : &lt;dataDir&gt;/archives.
     */
    private Path getDossierArchives() {
        return Paths.get(appliProperties.getDataDir(), "archives");
    }

    private int archiverFichiersSignes(Convention convention, Path dossierArchive) throws IOException {
        int nb = 0;
        String nomEtudiant = convention.getEtudiant() != null ? convention.getEtudiant().getNom() : "";
        String prenomEtudiant = convention.getEtudiant() != null ? convention.getEtudiant().getPrenom() : "";
        if (deplacerFichierSigne("Convention_" + convention.getId() + "_" + nomEtudiant + "_" + prenomEtudiant, dossierArchive)) {
            nb++;
        }
        if (convention.getAvenants() != null) {
            for (Avenant avenant : convention.getAvenants()) {
                if (deplacerFichierSigne("Avenant_" + avenant.getId() + "_" + nomEtudiant + "_" + prenomEtudiant, dossierArchive)) {
                    nb++;
                }
            }
        }
        return nb;
    }

    private boolean deplacerFichierSigne(String titre, Path dossierArchive) throws IOException {
        Path source = Paths.get(signatureService.getSignatureFilePath(titre));
        if (!Files.exists(source)) {
            return false;
        }
        Path cible = dossierArchive.resolve(source.getFileName());
        Files.createDirectories(cible.getParent());
        Files.move(source, cible, StandardCopyOption.REPLACE_EXISTING);
        return true;
    }

    /**
     * Purge définitivement les conventions archivées depuis plus de M années, puis les
     * structures archivées depuis le même délai qui ne sont plus référencées.
     */
    public String purger() {
        Date seuil = getSeuilPurge(new Date());
        demarrerRapport("Purge");

        int nbConventions = purgerConventions(seuil);
        int nbStructures = estAnnule() ? 0 : purgerStructures(seuil);

        String bilan = String.format("%s : %d convention(s) supprimée(s), %d structure(s) supprimée(s)",
                estAnnule() ? "Purge interrompue à la demande de l'utilisateur" : "Purge terminée",
                nbConventions, nbStructures);
        log.info("{} (seuil : {})", bilan, seuil);
        return bilan;
    }

    int purgerConventions(Date seuil) {
        List<Integer> ids = conventionJpaRepository.findIdsConventionsAPurger(seuil);
        log.info("Purge : {} convention(s) à purger", ids.size());
        progressionEtape("Purge des conventions", ids.size());
        int nb = 0;
        int traitees = 0;
        for (Integer id : ids) {
            if (estAnnule()) {
                log.info("Purge des conventions interrompue à la demande de l'utilisateur ({}/{})", traitees, ids.size());
                break;
            }
            try {
                // Les infos sont capturées dans la transaction (avant suppression) et ajoutées
                // au rapport après commit réussi
                ConventionTraiteeDto ligne = transactionTemplate.execute(status -> purgerConvention(id));
                if (ligne != null) {
                    nb++;
                    rapport.add(ligne);
                }
            } catch (Exception e) {
                log.error("Échec de la purge de la convention {} : {}", id, e.getMessage(), e);
            } finally {
                // Évite l'accumulation dans la session partagée (voir la boucle d'archivage)
                em.clear();
            }
            traitees++;
            progressionAvance(traitees);
            if (traitees % 500 == 0) {
                log.info("Purge : {}/{} convention(s) traitée(s)", traitees, ids.size());
            }
        }
        return nb;
    }

    private ConventionTraiteeDto purgerConvention(int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            return null;
        }
        // Capture des infos avant suppression définitive
        ConventionTraiteeDto ligne = ConventionTraiteeDto.from(convention);
        // Suppression des documents déposés (fichiers sur disque) puis de leur historique
        conventionDocumentEtudiantService.deleteAllForConvention(convention);
        conventionDocumentEtudiantHistoriqueJpaRepository.deleteByConventionId(id);
        // Références sans cascade JPA
        evaluationTuteurTokenJpaRepository.deleteByConventionId(id);
        periodeInterruptionAvenantJpaRepository.deleteByConventionId(id);
        List<EtudiantGroupeEtudiant> liens = etudiantGroupeEtudiantJpaRepository.findByConventionOrMergedConvention(id);
        if (!liens.isEmpty()) {
            etudiantGroupeEtudiantJpaRepository.deleteAll(liens);
            etudiantGroupeEtudiantJpaRepository.flush();
        }
        // Si la convention est la convention support d'un groupe de création en masse, la
        // suppression du groupe supprime aussi la convention (cascade REMOVE sur le OneToOne)
        List<GroupeEtudiant> groupes = groupeEtudiantJpaRepository.findByConventionId(id);
        if (!groupes.isEmpty()) {
            groupeEtudiantJpaRepository.deleteAll(groupes);
        } else {
            conventionJpaRepository.delete(convention);
        }
        log.info("Convention {} purgée", id);
        return ligne;
    }

    int purgerStructures(Date seuil) {
        List<Integer> ids = structureJpaRepository.findIdsStructuresAPurger(seuil);
        progressionEtape("Purge des structures", ids.size());
        int nb = 0;
        int traitees = 0;
        for (Integer id : ids) {
            if (estAnnule()) {
                log.info("Purge des structures interrompue à la demande de l'utilisateur ({}/{})", traitees, ids.size());
                break;
            }
            try {
                Boolean purgee = transactionTemplate.execute(status -> purgerStructure(id));
                if (Boolean.TRUE.equals(purgee)) {
                    nb++;
                }
            } catch (Exception e) {
                log.error("Échec de la purge de la structure {} : {}", id, e.getMessage(), e);
            } finally {
                // Évite l'accumulation dans la session partagée (voir la boucle d'archivage)
                em.clear();
            }
            traitees++;
            progressionAvance(traitees);
        }
        return nb;
    }

    private boolean purgerStructure(int id) {
        Structure structure = structureJpaRepository.findById(id);
        if (structure == null) {
            return false;
        }
        String reference = getReferenceRestante(id);
        if (reference != null) {
            log.info("Structure {} non purgée : encore référencée par {}", id, reference);
            return false;
        }
        historiqueStructureJpaRepository.deleteAll(historiqueStructureJpaRepository.findByStructure(structure));
        em.createQuery("DELETE FROM Contact c WHERE c.service.id IN (SELECT sv.id FROM Service sv WHERE sv.structure.id = :id)")
                .setParameter("id", id).executeUpdate();
        em.createQuery("DELETE FROM Service sv WHERE sv.structure.id = :id")
                .setParameter("id", id).executeUpdate();
        structureJpaRepository.delete(structure);
        log.info("Structure {} ({}) purgée", id, structure.getRaisonSociale());
        return true;
    }

    /**
     * Retourne la description de la première donnée qui référence encore la structure ou ses
     * services/contacts, ou null si la structure peut être supprimée sans casser de clé
     * étrangère ni perdre de donnée encore utilisée.
     */
    private String getReferenceRestante(int id) {
        if (conventionJpaRepository.countByStructure(id) > 0) {
            return "une convention (structure d'accueil)";
        }
        Long conventions = em.createQuery("SELECT COUNT(c.id) FROM Convention c" +
                        " LEFT JOIN c.service sv LEFT JOIN c.contact ct LEFT JOIN ct.service cts" +
                        " LEFT JOIN c.signataire sg LEFT JOIN sg.service sgs" +
                        " WHERE sv.structure.id = :id OR cts.structure.id = :id OR sgs.structure.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (conventions > 0) {
            return "une convention (service, tuteur ou signataire)";
        }
        Long avenants = em.createQuery("SELECT COUNT(a.id) FROM Avenant a" +
                        " LEFT JOIN a.service sv LEFT JOIN a.contact ct LEFT JOIN ct.service cts" +
                        " WHERE sv.structure.id = :id OR cts.structure.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (avenants > 0) {
            return "un avenant (service ou tuteur)";
        }
        Long offres = em.createQuery("SELECT COUNT(o.id) FROM Offre o" +
                        " LEFT JOIN o.contactCand cc LEFT JOIN cc.service ccs" +
                        " LEFT JOIN o.contactInfo ci LEFT JOIN ci.service cis" +
                        " LEFT JOIN o.contactProprio cp LEFT JOIN cp.service cps" +
                        " WHERE o.structure.id = :id OR ccs.structure.id = :id OR cis.structure.id = :id OR cps.structure.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (offres > 0) {
            return "une offre";
        }
        Long accords = em.createQuery("SELECT COUNT(ap) FROM AccordPartenariat ap" +
                        " LEFT JOIN ap.contact ct LEFT JOIN ct.service cts" +
                        " WHERE ap.structure.id = :id OR cts.structure.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (accords > 0) {
            return "un accord de partenariat";
        }
        Long tickets = em.createQuery("SELECT COUNT(t) FROM TicketStructure t WHERE t.structure.id = :id", Long.class)
                .setParameter("id", id).getSingleResult();
        if (tickets > 0) {
            return "un ticket structure";
        }
        if (evaluationTuteurTokenJpaRepository.countByContactStructure(id) > 0) {
            return "un token d'évaluation tuteur";
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Rapport des conventions traitées : export Excel
    // ------------------------------------------------------------------

    private void demarrerRapport(String type) {
        rapport.clear();
        rapportType = type;
    }

    /**
     * Génère le classeur Excel des conventions traitées par le dernier archivage/purge.
     */
    public byte[] exportRapportExcel() {
        List<ConventionTraiteeDto> lignes;
        synchronized (rapport) {
            lignes = new ArrayList<>(rapport);
        }
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(rapportType.isEmpty() ? "Conventions traitées" : "Conventions " + rapportType.toLowerCase());
            CellStyle entete = workbook.createCellStyle();
            Font gras = workbook.createFont();
            gras.setBold(true);
            entete.setFont(gras);

            String[] colonnes = {"N° de la convention", "Nom", "Prénom", "Établissement d'accueil", "Année universitaire", "Date de fin de stage", "Gratification", "Date d'archivage"};
            Row ligneEntete = sheet.createRow(0);
            for (int i = 0; i < colonnes.length; i++) {
                Cell cell = ligneEntete.createCell(i);
                cell.setCellValue(colonnes[i]);
                cell.setCellStyle(entete);
            }

            int numLigne = 1;
            for (ConventionTraiteeDto l : lignes) {
                Row row = sheet.createRow(numLigne++);
                row.createCell(0).setCellValue(l.id() != null ? l.id() : 0);
                row.createCell(1).setCellValue(l.nomEtudiant() != null ? l.nomEtudiant() : "");
                row.createCell(2).setCellValue(l.prenomEtudiant() != null ? l.prenomEtudiant() : "");
                row.createCell(3).setCellValue(l.structure() != null ? l.structure() : "");
                row.createCell(4).setCellValue(l.annee() != null ? l.annee() : "");
                row.createCell(5).setCellValue(l.dateFinStage() != null ? df.format(l.dateFinStage()) : "");
                row.createCell(6).setCellValue(l.gratification() ? "Oui" : "Non");
                row.createCell(7).setCellValue(l.dateArchivage() != null ? df.format(l.dateArchivage()) : "");
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la génération du fichier Excel");
        }
    }

    // ------------------------------------------------------------------
    // Simulation : ce que feraient les tâches si elles s'exécutaient maintenant,
    // sans rien modifier ni déplacer
    // ------------------------------------------------------------------

    public SimulationArchivageResumeDto getSimulationResume() {
        Date now = new Date();
        SimulationArchivageResumeDto dto = new SimulationArchivageResumeDto();
        dto.setSeuilArchivageSansGratification(getSeuilArchivageSansGratification(now));
        dto.setSeuilArchivageAvecGratification(getSeuilArchivageAvecGratification(now));
        dto.setSeuilPurge(minusYears(now, getDureePurgeAnnees()));
        dto.setConventionsAArchiverSansGratification(conventionJpaRepository.countAArchiverSansGratification(dto.getSeuilArchivageSansGratification()));
        dto.setConventionsAArchiverAvecGratification(conventionJpaRepository.countAArchiverAvecGratification(dto.getSeuilArchivageAvecGratification()));
        dto.setConventionsAArchiver(dto.getConventionsAArchiverSansGratification() + dto.getConventionsAArchiverAvecGratification());
        dto.setConventionsAPurger(conventionJpaRepository.countArchiveesAvant(dto.getSeuilPurge()));
        return dto;
    }

    public Date getSeuilArchivageSansGratification(Date now) {
        return minusYears(now, Math.max(1, appConfigService.getConfigGenerale().getDureeArchivageConventionAnnees()));
    }

    public Date getSeuilArchivageAvecGratification(Date now) {
        return minusYears(now, Math.max(1, appConfigService.getConfigGenerale().getDureeArchivageConventionGratifieeAnnees()));
    }

    public Date getSeuilPurge(Date now) {
        return minusYears(now, getDureePurgeAnnees());
    }

    /**
     * Statistiques affichées sur la page d'administration de l'archivage.
     */
    public ArchivageStatistiquesDto getStatistiques() {
        ArchivageStatistiquesDto dto = new ArchivageStatistiquesDto();
        dto.setConventionsArchivees(conventionJpaRepository.countArchivees());
        dto.setConventionsFichiersATrier(conventionJpaRepository.countFichiersATrier());
        dto.setConventionsPurgeables(conventionJpaRepository.countArchiveesAvant(minusYears(new Date(), getDureePurgeAnnees())));
        dto.setStructuresArchivees(structureJpaRepository.countArchivees());
        dto.setTacheArchivage(cronTaskJpaRepository.findByNom("ArchiverConventions"));
        dto.setTachePurge(cronTaskJpaRepository.findByNom("PurgerConventions"));
        dto.setProchaineExecutionArchivage(getProchaineExecution(dto.getTacheArchivage()));
        dto.setProchaineExecutionPurge(getProchaineExecution(dto.getTachePurge()));
        return dto;
    }

    private Date getProchaineExecution(CronTask cronTask) {
        if (cronTask == null || !cronTask.isActive() || cronTask.getExpressionCron() == null) {
            return null;
        }
        try {
            LocalDateTime prochaine = CronExpression.parse(cronTask.getExpressionCron()).next(LocalDateTime.now());
            return prochaine != null ? Date.from(prochaine.atZone(ZoneId.systemDefault()).toInstant()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private int getDureePurgeAnnees() {
        ConfigGeneraleDto config = appConfigService.getConfigGenerale();
        return Math.max(1, config.getDureePurgeConventionAnnees());
    }

    private Date minusYears(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, -years);
        return calendar.getTime();
    }
}
