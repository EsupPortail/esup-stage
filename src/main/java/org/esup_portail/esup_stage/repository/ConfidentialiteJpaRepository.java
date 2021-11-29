package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Confidentialite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfidentialiteJpaRepository extends JpaRepository<Confidentialite, Integer> {

    List<Confidentialite> findAll();
}
