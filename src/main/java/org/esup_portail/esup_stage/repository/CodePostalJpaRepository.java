package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CodePostal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodePostalJpaRepository extends JpaRepository<CodePostal, Integer> {

    CodePostal findById(int id);

    List<CodePostal> findAll();
}
