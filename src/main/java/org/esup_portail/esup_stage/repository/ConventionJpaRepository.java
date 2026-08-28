package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Structure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface ConventionJpaRepository extends JpaRepository<Convention, Integer> {

    Convention findById(int id);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.typeConvention.id = :idTypeConvention")
    Long countConventionWithTypeConvention(@Param("idTypeConvention") int idTypeConvention);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.langueConvention.code = :codeLangueConvention")
    Long countConventionWithLangueConvention(@Param("codeLangueConvention") String codeLangueConvention);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.tempsTravail.id = :idTempsTravail")
    Long countConventionWithTempsTravail(@Param("idTempsTravail") int idTempsTravail);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.uniteDureeGratification.id = :idUniteDuree OR c.uniteDureeExceptionnelle.id = :idUniteDuree")
    Long countConventionWithUniteDuree(@Param("idUniteDuree") int idUniteDuree);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.theme.id = :idTheme")
    Long countConventionWithTheme(@Param("idTheme") int idTheme);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.uniteGratification.id = :idUniteGratification")
    Long countConventionWithUniteGratification(@Param("idUniteGratification") int idUniteGratification);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.modeVersGratification.id = :idModeVersGratification")
    Long countConventionWithModeVersGratification(@Param("idModeVersGratification") int idModeVersGratification);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.modeValidationStage.id = :idModeValidationStage")
    Long countConventionWithModeValidationStage(@Param("idModeValidationStage") int idModeValidationStage);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.niveauFormation.id = :idNiveauFormation")
    Long countConventionWithNiveauFormation(@Param("idNiveauFormation") int idNiveauFormation);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.typeOffre.id = :idTypeOffre")
    Long countConventionWithTypeOffre(@Param("idTypeOffre") int idTypeOffre);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.contratOffre.id = :idContratOffre")
    Long countConventionWithContratOffre(@Param("idContratOffre") int idContratOffre);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.origineStage.id = :idOrigineStage")
    Long countConventionWithOrigineStage(@Param("idOrigineStage") int idOrigineStage);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.typeStructure.id = :idTypeStructure")
    Long countConventionWithTypeStructure(@Param("idTypeStructure") int idTypeStructure);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.statutJuridique.id = :idStatutJuridique")
    Long countConventionWithStatutJuridique(@Param("idStatutJuridique") int idStatutJuridique);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.pays.id = :idPays")
    Long countConventionWithPays(@Param("idPays") int idPays);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.devise.id = :idDevise")
    Long countConventionWithDevise(@Param("idDevise") int idDevise);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion")
    Long countConventionWithCentreGestion(@Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion AND LOWER(c.enseignant.uidEnseignant) IN :identifiants")
    Long countConventionByEnseignantAndCentreGestion(@Param("identifiants") Collection<String> identifiants, @Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.effectif.id = :idEffectif")
    Long countConventionWithEffectif(@Param("idEffectif") int idEffectif);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.natureTravail.id = :idNatureTravail")
    Long countConventionWithNatureTravail(@Param("idNatureTravail") int idNatureTravail);

    @Query("SELECT c FROM Convention c WHERE c.loginCreation = ?1 AND c.validationCreation = FALSE AND c.creationEnMasse = FALSE AND c.dateArchivage IS NULL")
    Convention findBrouillon(@Param("login") String login);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.validationCreation = TRUE AND c.validationConvention = FALSE AND c.dateArchivage IS NULL")
    List<Convention> getConventionEnAttenteGestionnaire(@Param("annee") String annee);

    @Query("SELECT c FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE c.annee = :annee AND p.uidPersonnel = :userUid AND c.validationCreation = TRUE AND c.validationConvention = FALSE AND c.dateArchivage IS NULL")
    List<Convention> getConventionEnAttenteGestionnaire(@Param("annee") String annee, @Param("userUid") String userUid);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.centreGestion.id IN :idsCentreGestion AND c.validationCreation = TRUE AND c.validationConvention = FALSE AND c.dateArchivage IS NULL")
    List<Convention> getConventionEnAttenteGestionnaireByCentreIds(@Param("annee") String annee, @Param("idsCentreGestion") Collection<Integer> idsCentreGestion);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.enseignant.uidEnseignant = :userUid AND c.validationCreation = TRUE AND c.validationPedagogique = FALSE AND c.dateArchivage IS NULL")
    List<Convention> getConventionEnAttenteEnseignant(@Param("annee") String annee, @Param("userUid") String userUid);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion and c.ufr.id.code = :codeUfr")
    Long countConventionRattacheUfr(@Param("idCentreGestion") int idCentreGestion, @Param("codeUfr") String codeUfr);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion AND c.etape.id.code = :codeEtape AND c.etape.id.codeVersionEtape = :codeVersionEtape")
    Long countConventionRattacheEtape(@Param("idCentreGestion") int idCentreGestion, @Param("codeEtape") String codeEtape, @Param("codeVersionEtape") String codeVersionEtape);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE p.uidPersonnel = :uid AND c.creationEnMasse = FALSE AND c.dateArchivage IS NULL ORDER BY c.annee")
    List<String> getGestionnaireAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.centreGestion.id IN :idsCentreGestion AND c.creationEnMasse = FALSE AND c.dateArchivage IS NULL ORDER BY c.annee")
    List<String> getAnneesByCentreIds(@Param("idsCentreGestion") Collection<Integer> idsCentreGestion);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.enseignant.uidEnseignant = :uid AND c.creationEnMasse = FALSE AND c.dateArchivage IS NULL ORDER BY c.annee")
    List<String> getEnseignantAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.etudiant.identEtudiant = :uid AND c.creationEnMasse = FALSE AND c.dateArchivage IS NULL ORDER BY c.annee")
    List<String> getEtudiantAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getAnnees();

    @Transactional
    @Modifying
    @Query("UPDATE Convention c SET c.verificationAdministrative = TRUE WHERE c.centreGestion.id = :idCentreGestion AND c.validationPedagogique = TRUE AND c.validationConvention = TRUE AND c.dateArchivage IS NULL")
    void updateVerificationAdministrative(@Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT c.id FROM Convention c WHERE c.id != :conventionId AND c.etudiant.identEtudiant = :uid AND c.validationCreation = TRUE AND ((c.dateDebutStage >= :dateDebut AND c.dateFinStage <= :dateFin) OR (c.dateDebutStage <= :dateDebut AND c.dateFinStage >= :dateDebut) OR (c.dateDebutStage <= :dateFin AND c.dateFinStage >= :dateFin))")
    List<Integer> findDatesChevauchent(@Param("uid") String uid, @Param("conventionId") int conventionId, @Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin);

    @Query("SELECT c FROM Convention c WHERE c.dateArchivage IS NULL AND c.documentId IS NOT NULL AND (c.dateSignatureEtudiant IS NULL OR c.dateDepotEtudiant IS NULL OR c.dateSignatureEnseignant IS NULL OR c.dateDepotEnseignant IS NULL OR c.dateSignatureTuteur IS NULL OR c.dateDepotTuteur IS NULL OR c.dateSignatureSignataire IS NULL OR c.dateDepotSignataire IS NULL OR c.dateSignatureViseur IS NULL OR c.dateDepotViseur IS NULL)")
    List<Convention> getSignatureInfoToUpdate();

    @Query("SELECT c FROM Convention c WHERE c.structure.id = :structureId")
    List<Convention> findByStructureId(@Param("structureId") int structureId);

    @Query("SELECT c FROM Convention c WHERE c.temConventionSignee = false AND c.validationConvention = TRUE AND c.dateEnvoiSignature IS NOT NULL AND c.dateArchivage IS NULL")
    List<Convention> findConventionNonSignees();

    // Une convention est considérée "avec gratification" si le témoin est levé ou si un montant est renseigné
    String CRITERE_AVEC_GRATIFICATION = "(c.gratificationStage = TRUE OR (c.montantGratification IS NOT NULL AND c.montantGratification <> ''))";
    String CRITERE_SANS_GRATIFICATION = "((c.gratificationStage IS NULL OR c.gratificationStage = FALSE) AND (c.montantGratification IS NULL OR c.montantGratification = ''))";

    // Conventions dont le délai d'archivage est dépassé (fin de stage, ou création pour les
    // conventions sans dates). Une convention marquée archivée sans que ses fichiers aient été
    // traités est considérée comme restant à archiver (l'archivage n'est effectif que fichiers compris).
    String CRITERE_ARCHIVABLE = "(c.dateArchivage IS NULL OR c.dateArchivageFichiers IS NULL) AND (" +
            " (" + CRITERE_AVEC_GRATIFICATION + " AND ((c.dateFinStage IS NOT NULL AND c.dateFinStage < :seuilAvecGratification) OR (c.dateFinStage IS NULL AND c.dateCreation < :seuilAvecGratification)))" +
            " OR (" + CRITERE_SANS_GRATIFICATION + " AND ((c.dateFinStage IS NOT NULL AND c.dateFinStage < :seuilSansGratification) OR (c.dateFinStage IS NULL AND c.dateCreation < :seuilSansGratification)))" +
            ")";

    // L'archivage se fait convention par convention (fichiers + marquage dans la même
    // transaction) : on sélectionne les identifiants éligibles, le service fait le reste
    @Query("SELECT c.id FROM Convention c WHERE " + CRITERE_ARCHIVABLE + " ORDER BY c.id")
    List<Integer> findIdsConventionsAArchiver(@Param("seuilSansGratification") Date seuilSansGratification, @Param("seuilAvecGratification") Date seuilAvecGratification);

    // Réparation : une convention n'est considérée archivée que si ses fichiers ont été
    // traités ; les états incohérents (ex. hérités d'une ancienne version ou d'un incident)
    // sont réactivés puis repassent par le circuit d'archivage normal
    @Transactional
    @Modifying
    @Query("UPDATE Convention c SET c.dateArchivage = NULL WHERE c.dateArchivage IS NOT NULL AND c.dateArchivageFichiers IS NULL")
    int reactiverConventionsSansFichiersTraites();

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE (c.dateArchivage IS NULL OR c.dateArchivageFichiers IS NULL) AND " + CRITERE_AVEC_GRATIFICATION +
            " AND ((c.dateFinStage IS NOT NULL AND c.dateFinStage < :seuil) OR (c.dateFinStage IS NULL AND c.dateCreation < :seuil))")
    long countAArchiverAvecGratification(@Param("seuil") Date seuil);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE (c.dateArchivage IS NULL OR c.dateArchivageFichiers IS NULL) AND " + CRITERE_SANS_GRATIFICATION +
            " AND ((c.dateFinStage IS NOT NULL AND c.dateFinStage < :seuil) OR (c.dateFinStage IS NULL AND c.dateCreation < :seuil))")
    long countAArchiverSansGratification(@Param("seuil") Date seuil);

    // Seules les conventions complètement archivées (fichiers traités) sont purgeables
    @Query("SELECT c.id FROM Convention c WHERE c.dateArchivage IS NOT NULL AND c.dateArchivageFichiers IS NOT NULL AND c.dateArchivage < :seuil")
    List<Integer> findIdsConventionsAPurger(@Param("seuil") Date seuil);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.dateArchivage IS NOT NULL")
    long countArchivees();

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.dateArchivage IS NOT NULL AND c.dateArchivageFichiers IS NULL")
    long countFichiersATrier();

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.dateArchivage IS NOT NULL AND c.dateArchivageFichiers IS NOT NULL AND c.dateArchivage < :seuil")
    long countArchiveesAvant(@Param("seuil") Date seuil);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.id = :idStructure")
    Long countByStructure(@Param("idStructure") int idStructure);

    /**
     * Conventions validées dont le tuteur professionnel n'a encore jamais été sollicité au titre
     * du droit d'opposition et qui n'a pas déjà exprimé son refus.
     * Le critère de validation reprend celui du filtre "isConventionValide" de ConventionRepository.
     */
    @Query("""
            SELECT c
            FROM Convention c
            JOIN c.contact ct
            WHERE (c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE)
              AND (c.centreGestion.validationConvention = FALSE OR c.validationConvention = TRUE)
              AND ct.mail IS NOT NULL
              AND ct.mail <> ''
              AND (ct.refusEtreContacte IS NULL OR ct.refusEtreContacte = FALSE)
              AND ct.dateEnvoiMailOpposition IS NULL
            ORDER BY c.id DESC
            """)
    List<Convention> findConventionsTuteurProDroitOpposition();

    /**
     * Même sélection que {@link #findConventionsTuteurProDroitOpposition()} pour le signataire.
     */
    @Query("""
            SELECT c
            FROM Convention c
            JOIN c.signataire s
            WHERE (c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE)
              AND (c.centreGestion.validationConvention = FALSE OR c.validationConvention = TRUE)
              AND s.mail IS NOT NULL
              AND s.mail <> ''
              AND (s.refusEtreContacte IS NULL OR s.refusEtreContacte = FALSE)
              AND s.dateEnvoiMailOpposition IS NULL
            ORDER BY c.id DESC
            """)
    List<Convention> findConventionsSignataireDroitOpposition();

    /**
     * Conventions validées les plus récentes sur lesquelles le contact est tuteur professionnel.
     * Sert à l'envoi manuel du mail de droit d'opposition : le contexte du mail est celui de la
     * dernière convention en date.
     */
    @Query("""
            SELECT c
            FROM Convention c
            WHERE c.contact.id = :idContact
              AND (c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE)
              AND (c.centreGestion.validationConvention = FALSE OR c.validationConvention = TRUE)
            ORDER BY c.id DESC
            """)
    List<Convention> findConventionsValideesParTuteurPro(@Param("idContact") int idContact);

    /**
     * Même sélection que {@link #findConventionsValideesParTuteurPro(int)} pour le signataire.
     */
    @Query("""
            SELECT c
            FROM Convention c
            WHERE c.signataire.id = :idContact
              AND (c.centreGestion.validationPedagogique = FALSE OR c.validationPedagogique = TRUE)
              AND (c.centreGestion.validationConvention = FALSE OR c.validationConvention = TRUE)
            ORDER BY c.id DESC
            """)
    List<Convention> findConventionsValideesParSignataire(@Param("idContact") int idContact);

    @Query("SELECT c FROM Convention c WHERE c.temConventionSignee = TRUE AND c.documentId IS NOT NULL")
    List<Convention> findConventionsSignees();
}
