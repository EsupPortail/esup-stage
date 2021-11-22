package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.DroitAdministration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DroitAdministrationJpaRepository extends JpaRepository<DroitAdministration, Integer> {

    List<DroitAdministration> findAll();
}
