package org.esup_portail.esup_stage.service.archivage;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private EntityManager em;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * Archive les conventions terminées depuis plus de N années (délais distincts pour les
     * conventions avec et sans gratification) puis les structures dont toutes les conventions
     * sont archivées. Réactive au passage les structures archivées qui auraient été
     * réutilisées sur une convention active, et déplace les fichiers des conventions
     * archivées vers le dossier d'archives du serveur.
     */
    public void archiver() {
        Date now = new Date();
        ConfigGeneraleDto config = appConfigService.getConfigGenerale();
        Date seuilSansGratification = minusYears(now, Math.max(1, config.getDureeArchivageConventionAnnees()));
        Date seuilAvecGratification = minusYears(now, Math.max(1, config.getDureeArchivageConventionGratifieeAnnees()));

        int nbConventions = conventionJpaRepository.archiverConventionsAvant(seuilSansGratification, seuilAvecGratification, now);
        int nbStructuresReactivees = structureJpaRepository.desarchiverStructuresReutilisees();
        int nbStructures = structureJpaRepository.archiverStructuresSansConventionActive(now);
        int nbFichiers = archiverFichiersConventions();

        log.info("Archivage terminé : {} convention(s) archivée(s), {} structure(s) archivée(s), {} structure(s) réactivée(s), fichiers triés pour {} convention(s) (seuils : sans gratification {}, avec gratification {})",
                nbConventions, nbStructures, nbStructuresReactivees, nbFichiers, seuilSansGratification, seuilAvecGratification);
    }

    /**
     * Déplace les fichiers (documents déposés par l'étudiant, PDF signés de la convention et
     * de ses avenants) de chaque convention archivée vers le dossier {@code <dataDir>/archives}.
     * Les fichiers ne sont alors plus téléchargeables depuis l'application ; leur suppression
     * définitive est à la charge du serveur (hors scope de l'application, ici on ne fait que
     * le tri). Les conventions dont le déplacement a échoué sont retentées à la prochaine
     * exécution (dateArchivageFichiers reste nulle).
     */
    int archiverFichiersConventions() {
        List<Integer> ids = conventionJpaRepository.findIdsConventionsFichiersAArchiver();
        log.info("Tri des fichiers : {} convention(s) à traiter", ids.size());
        int nb = 0;
        int traitees = 0;
        for (Integer id : ids) {
            try {
                transactionTemplate.executeWithoutResult(status -> archiverFichiersConvention(id));
                nb++;
            } catch (Exception e) {
                log.error("Échec du tri des fichiers de la convention {} : {}", id, e.getMessage(), e);
            } finally {
                // Vide le contexte de persistance : en exécution manuelle (open-in-view), la session
                // couvre toute la requête HTTP et chaque convention chargée s'y accumule — sans ce
                // clear, le dirty-checking de l'auto-flush rend le traitement quadratique
                em.clear();
            }
            traitees++;
            if (traitees % 500 == 0) {
                log.info("Tri des fichiers : {}/{} convention(s) traitée(s)", traitees, ids.size());
            }
        }
        return nb;
    }

    private void archiverFichiersConvention(int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null || convention.getDateArchivage() == null) {
            return;
        }
        Path racineArchives = getDossierArchives();
        try {
            // Les documents déposés (noms techniques UUID) sont regroupés dans un dossier portant l'id de la convention
            int nbDocuments = conventionDocumentEtudiantService.archiverFichiers(convention, racineArchives.resolve("convention_" + convention.getId()));
            // Les PDF signés sont déposés directement à la racine : leur nom contient déjà l'id de la convention
            int nbSignes = archiverFichiersSignes(convention, racineArchives);
            convention.setDateArchivageFichiers(new Date());
            conventionJpaRepository.save(convention);
            if (nbDocuments > 0 || nbSignes > 0) {
                log.info("Convention {} : {} document(s) déposé(s) et {} PDF signé(s) déplacés vers {}", id, nbDocuments, nbSignes, racineArchives);
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
    public void purger() {
        Date seuil = minusYears(new Date(), getDureePurgeAnnees());

        int nbConventions = purgerConventions(seuil);
        int nbStructures = purgerStructures(seuil);

        log.info("Purge terminée : {} convention(s) supprimée(s), {} structure(s) supprimée(s) (seuil : {})",
                nbConventions, nbStructures, seuil);
    }

    int purgerConventions(Date seuil) {
        List<Integer> ids = conventionJpaRepository.findIdsConventionsAPurger(seuil);
        log.info("Purge : {} convention(s) à purger", ids.size());
        int nb = 0;
        int traitees = 0;
        for (Integer id : ids) {
            try {
                transactionTemplate.executeWithoutResult(status -> purgerConvention(id));
                nb++;
            } catch (Exception e) {
                log.error("Échec de la purge de la convention {} : {}", id, e.getMessage(), e);
            } finally {
                // Voir archiverFichiersConventions : évite l'accumulation dans la session partagée
                em.clear();
            }
            traitees++;
            if (traitees % 500 == 0) {
                log.info("Purge : {}/{} convention(s) traitée(s)", traitees, ids.size());
            }
        }
        return nb;
    }

    private void purgerConvention(int id) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            return;
        }
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
    }

    int purgerStructures(Date seuil) {
        List<Integer> ids = structureJpaRepository.findIdsStructuresAPurger(seuil);
        int nb = 0;
        for (Integer id : ids) {
            try {
                Boolean purgee = transactionTemplate.execute(status -> purgerStructure(id));
                if (Boolean.TRUE.equals(purgee)) {
                    nb++;
                }
            } catch (Exception e) {
                log.error("Échec de la purge de la structure {} : {}", id, e.getMessage(), e);
            } finally {
                // Voir archiverFichiersConventions : évite l'accumulation dans la session partagée
                em.clear();
            }
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
