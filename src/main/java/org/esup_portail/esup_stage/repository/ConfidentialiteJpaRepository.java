package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Confidentialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfidentialiteJpaRepository extends JpaRepository<Confidentialite, String> {
}