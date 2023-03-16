package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CPAM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CPAMJpaRepository extends JpaRepository<CPAM, Integer> {

    CPAM findById(int id);

    List<CPAM> findAll();
}
