package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeOffre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeOffreJpaRepository extends JpaRepository<TypeOffre, Integer> {

    TypeOffre findById(int id);
}
