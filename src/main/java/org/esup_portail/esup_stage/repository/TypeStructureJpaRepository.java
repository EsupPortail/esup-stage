package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeStructureJpaRepository extends JpaRepository<TypeStructure, Integer> {

    TypeStructure findById(int id);

    @Query("SELECT t FROM TypeStructure t WHERE t.libelle = :lib")
    TypeStructure findByLibelle(@Param("lib") String lib);
}
