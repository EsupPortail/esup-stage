package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.LangueConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LangueConventionJpaRepository extends JpaRepository<LangueConvention, Integer> {

    LangueConvention findByCode(String code);
}
