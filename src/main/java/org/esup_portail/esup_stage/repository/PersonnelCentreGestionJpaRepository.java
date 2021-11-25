package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonnelCentreGestionJpaRepository extends JpaRepository<PersonnelCentreGestion, Integer> {

    PersonnelCentreGestion findById(int id);

    @Query("SELECT COUNT(p) FROM PersonnelCentreGestion p WHERE p.uidPersonnel = :login")
    long countPersonnelByLogin(String login);
}
