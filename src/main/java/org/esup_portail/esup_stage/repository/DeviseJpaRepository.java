package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Devise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviseJpaRepository extends JpaRepository<Devise, Integer> {

    Devise findById(int id);
}
