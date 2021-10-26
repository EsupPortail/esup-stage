package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CentreGestionJpaRepository extends JpaRepository<CentreGestion, Integer> {

    CentreGestion findById(int id);

    @Query("SELECT cg FROM CentreGestion cg WHERE cg.loginCreation = :loginCreation AND cg.validationCreation = FALSE")
    CentreGestion findBrouillon(String loginCreation);
}
