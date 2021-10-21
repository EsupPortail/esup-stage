package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TempsTravail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TempsTravailJpaRepository extends JpaRepository<TempsTravail, Integer> {

    TempsTravail findById(int id);
}
