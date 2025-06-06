package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PersonnelCentreGestionJpaRepository extends JpaRepository<PersonnelCentreGestion, Integer> {

    PersonnelCentreGestion findById(int id);

    @Query("SELECT COUNT(p) FROM PersonnelCentreGestion p WHERE p.uidPersonnel = :uid")
    long countPersonnelByLogin(@Param("uid") String uid);

    @Transactional
    @Modifying
    @Query("DELETE FROM PersonnelCentreGestion p WHERE p.centreGestion.id = :id")
    void deletePersonnelsByCentreId(@Param("id") int id);
}
