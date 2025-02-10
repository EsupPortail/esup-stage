package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EtudiantJpaRepository extends JpaRepository<Etudiant, Integer> {

    Etudiant findById(int id);

    @Query("SELECT e FROM Etudiant e WHERE e.identEtudiant = :uid")
    Etudiant findByLogin(@Param("uid") String uid);

}
