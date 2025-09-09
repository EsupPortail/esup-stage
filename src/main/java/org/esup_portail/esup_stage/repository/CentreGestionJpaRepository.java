package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.dto.CentreGestionSearchDTO;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CentreGestionJpaRepository extends JpaRepository<CentreGestion, Integer> {

    CentreGestion findById(int id);

    @Query("SELECT cg FROM CentreGestion cg WHERE cg.loginCreation = :loginCreation AND cg.validationCreation = FALSE")
    CentreGestion findBrouillon(@Param("loginCreation") String loginCreation);

    @Query("SELECT cg FROM CentreGestion cg JOIN cg.criteres c WHERE c.id.code = :codeEtape and c.id.codeVersionEtape = :codeVersion")
    CentreGestion findByCodeEtape(@Param("codeEtape") String codeEtape, @Param("codeVersion") String codeVersion);

    @Query("SELECT cg FROM CentreGestion cg JOIN cg.personnels p WHERE p.uidPersonnel = :uid")
    CentreGestion getByGestionnaireUid(@Param("uid") String uid);

    @Query("SELECT cg FROM CentreGestion cg where LOWER(cg.nomCentre) = LOWER(:code)")
    CentreGestion getByCode(@Param("code") String code);

    @Query("SELECT cg FROM CentreGestion cg where cg.niveauCentre.libelle = 'ETABLISSEMENT'")
    CentreGestion getCentreEtablissement();

    @Query("SELECT new org.esup_portail.esup_stage.dto.CentreGestionSearchDTO(cg.id, cg.nomCentre) FROM CentreGestion cg")
    List<CentreGestionSearchDTO> findAllForSearch();
}
