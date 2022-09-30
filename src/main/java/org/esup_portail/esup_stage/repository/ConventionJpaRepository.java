package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface ConventionJpaRepository extends JpaRepository<Convention, Integer> {

    Convention findById(int id);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.typeConvention.id = :idTypeConvention")
    Long countConventionWithTypeConvention(int idTypeConvention);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.langueConvention.code = :codeLangueConvention")
    Long countConventionWithLangueConvention(String codeLangueConvention);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.tempsTravail.id = :idTempsTravail")
    Long countConventionWithTempsTravail(int idTempsTravail);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.uniteDureeGratification.id = :idUniteDuree OR c.uniteDureeExceptionnelle.id = :idUniteDuree")
    Long countConventionWithUniteDuree(int idUniteDuree);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.theme.id = :idTheme")
    Long countConventionWithTheme(int idTheme);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.uniteGratification.id = :idUniteGratification")
    Long countConventionWithUniteGratification(int idUniteGratification);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.modeVersGratification.id = :idModeVersGratification")
    Long countConventionWithModeVersGratification(int idModeVersGratification);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.modeValidationStage.id = :idModeValidationStage")
    Long countConventionWithModeValidationStage(int idModeValidationStage);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.niveauFormation.id = :idNiveauFormation")
    Long countConventionWithNiveauFormation(int idNiveauFormation);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.typeOffre.id = :idTypeOffre")
    Long countConventionWithTypeOffre(int idTypeOffre);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.offre.contratOffre.id = :idContratOffre")
    Long countConventionWithContratOffre(int idContratOffre);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.origineStage.id = :idOrigineStage")
    Long countConventionWithOrigineStage(int idOrigineStage);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.typeStructure.id = :idTypeStructure")
    Long countConventionWithTypeStructure(int idTypeStructure);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.statutJuridique.id = :idStatutJuridique")
    Long countConventionWithStatutJuridique(int idStatutJuridique);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.pays.id = :idPays")
    Long countConventionWithPays(int idPays);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.devise.id = :idDevise")
    Long countConventionWithDevise(int idDevise);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion")
    Long countConventionWithCentreGestion(int idCentreGestion);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.effectif.id = :idEffectif")
    Long countConventionWithEffectif(int idEffectif);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.natureTravail.id = :idNatureTravail")
    Long countConventionWithNatureTravail(int idNatureTravail);

    @Query("SELECT c FROM Convention c WHERE c.loginCreation = :login AND c.validationCreation = FALSE AND c.creationEnMasse = FALSE")
    Convention findBrouillon(String login);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.validationCreation = TRUE AND c.validationConvention = FALSE")
    List<Convention> getConventionEnAttenteGestionnaire(String annee);

    @Query("SELECT c FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE c.annee = :annee AND p.uidPersonnel = :userLogin AND c.validationCreation = TRUE AND c.validationConvention = FALSE")
    List<Convention> getConventionEnAttenteGestionnaire(String annee, String userLogin);

    @Query("SELECT c FROM Convention c WHERE c.annee = :annee AND c.enseignant.uidEnseignant = :userLogin AND c.validationCreation = TRUE AND c.validationPedagogique = FALSE")
    List<Convention> getConventionEnAttenteEnseignant(String annee, String userLogin);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion and c.ufr.id.code = :codeUfr")
    Long countConventionRattacheUfr(int idCentreGestion, String codeUfr);

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.centreGestion.id = :idCentreGestion AND c.etape.id.code = :codeEtape AND c.etape.id.codeVersionEtape = :codeVersionEtape")
    Long countConventionRattacheEtape(int idCentreGestion, String codeEtape, String codeVersionEtape);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c JOIN c.centreGestion cg JOIN cg.personnels p WHERE p.uidPersonnel = :login AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getGestionnaireAnnees(String login);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.enseignant.uidEnseignant = :login AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getEnseignantAnnees(String login);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.etudiant.identEtudiant = :login AND c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getEtudiantAnnees(String login);

    @Query("SELECT DISTINCT(c.annee) FROM Convention c WHERE c.creationEnMasse = FALSE ORDER BY c.annee")
    List<String> getAnnees(String login);

    @Transactional
    @Modifying
    @Query("UPDATE Convention c SET c.verificationAdministrative = TRUE WHERE c.centreGestion.id = :idCentreGestion AND c.validationPedagogique = TRUE AND c.validationConvention = TRUE")
    void updateVerificationAdministrative(int idCentreGestion);

    @Query("SELECT c.id FROM Convention c WHERE c.id != :conventionId AND c.etudiant.identEtudiant = :login AND c.validationCreation = TRUE AND ((c.dateDebutStage >= :dateDebut AND c.dateFinStage <= :dateFin) OR (c.dateDebutStage <= :dateDebut AND c.dateFinStage >= :dateDebut) OR (c.dateDebutStage <= :dateFin AND c.dateFinStage >= :dateFin))")
    List<Integer> findDatesChevauchent(String login, int conventionId, Date dateDebut, Date dateFin);
}
