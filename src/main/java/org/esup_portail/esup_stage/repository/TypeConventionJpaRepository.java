package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TypeConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeConventionJpaRepository extends JpaRepository<TypeConvention, Integer> {

    TypeConvention findById(int id);
}
