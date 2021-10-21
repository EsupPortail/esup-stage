package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Convention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

    @Query("SELECT COUNT(c.id) FROM Convention c WHERE c.structure.effectif.id = :idEffectif")
    Long countConventionWithEffectif(int idEffectif);

    @Query("SELECT c FROM Convention c WHERE c.loginCreation = :login AND c.validationCreation = FALSE")
    Convention findBrouillon(String login);
}
