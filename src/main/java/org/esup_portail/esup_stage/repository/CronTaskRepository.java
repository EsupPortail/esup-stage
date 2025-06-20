package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.CronTask;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class CronTaskRepository extends PaginationRepository<CronTask> {

    public CronTaskRepository(EntityManager em) {
        super(em, CronTask.class, "ct");
        this.predicateWhitelist = Arrays.asList("id", "nom", "expressionCron", "dateCreation", "loginCreation", "dateModification", "loginModification", "dateDernierExecution", "active");
    }
}

