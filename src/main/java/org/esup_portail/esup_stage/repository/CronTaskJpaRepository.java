package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.CronTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CronTaskJpaRepository extends JpaRepository<CronTask, Integer> {

    List<CronTask> findByActiveTrue();
}