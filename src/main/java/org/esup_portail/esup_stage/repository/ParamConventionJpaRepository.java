package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.ParamConvention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParamConventionJpaRepository extends JpaRepository<ParamConvention, String> {
}
