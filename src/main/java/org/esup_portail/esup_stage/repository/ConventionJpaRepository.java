package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.effectif.id = :idEffectif")
    Long countConventionWithEffectif(@Param("idEffectif") int idEffectif);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.natureTravail.id = :idNatureTravail")
    Long countConventionWithNatureTravail(@Param("idNatureTravail") int idNatureTravail);

    @Query("SELECT c FROM Convention c WHERE c.loginCreation = ?1 AND c.validationCreation = FALSE AND c.creationEnMasse = FALSE")
    Convention findBrouillon(@Param("login") String login);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.validationCreation = TRUE AND c.validationConvention = FALSE")
    List<Convention> getConventionEnAttenteGestionnaire(@Param("annee") String annee);

    @Query("SELECT c FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE c.annee = :annee AND p.uidPersonnel = :userUid AND c.validationCreation = TRUE AND c.validationConvention = FALSE")
    List<Convention> getConventionEnAttenteGestionnaire(@Param("annee") String annee, @Param("userUid") String userUid);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.enseignant.uidEnseignant = :userUid AND c.validationCreation = TRUE AND c.validationPedagogique = FALSE")
    List<Convention> getConventionEnAttenteEnseignant(@Param("annee") String annee, @Param("userUid") String userUid);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion and c.ufr.id.code = :codeUfr")
    Long countConventionRattacheUfr(@Param("idCentreGestion") int idCentreGestion, @Param("codeUfr") String codeUfr);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion AND c.etape.id.code = :codeEtape AND c.etape.id.codeVersionEtape = :codeVersionEtape")
    Long countConventionRattacheEtape(@Param("idCentreGestion") int idCentreGestion, @Param("codeEtape") String codeEtape, @Param("codeVersionEtape") String codeVersionEtape);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE p.uidPersonnel = :uid AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getGestionnaireAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.enseignant.uidEnseignant = :uid AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getEnseignantAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.etudiant.identEtudiant = :uid AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getEtudiantAnnees(@Param("uid") String uid);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getAnnees();

    @Transactional
    @Modifying
    @Query("UPDATE Convention c SET c.verificationAdministrative = TRUE WHERE c.centreGestion.id = :idCentreGestion AND c.validationPedagogique = TRUE AND c.validationConvention = TRUE")
    void updateVerificationAdministrative(@Param("idCentreGestion") int idCentreGestion);

    @Query("SELECT c.id FROM Convention c WHERE c.id != :conventionId AND c.etudiant.identEtudiant = :uid AND c.validationCreation = TRUE AND ((c.dateDebutStage >= :dateDebut AND c.dateFinStage <= :dateFin) OR (c.dateDebutStage <= :dateDebut AND c.dateFinStage >= :dateDebut) OR (c.dateDebutStage <= :dateFin AND c.dateFinStage >= :dateFin))")
    List<Integer> findDatesChevauchent(@Param("uid") String uid, @Param("conventionId") int conventionId, @Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin);

    @Query("SELECT c FROM Convention c WHERE c.documentId IS NOT NULL AND (c.dateSignatureEtudiant IS NULL OR c.dateDepotEtudiant IS NULL OR c.dateSignatureEnseignant IS NULL OR c.dateDepotEnseignant IS NULL OR c.dateSignatureTuteur IS NULL OR c.dateDepotTuteur IS NULL OR c.dateSignatureSignataire IS NULL OR c.dateDepotSignataire IS NULL OR c.dateSignatureViseur IS NULL OR c.dateDepotViseur IS NULL)")
    List<Convention> getSignatureInfoToUpdate();
}
