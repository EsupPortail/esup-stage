package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.PeriodeInterruptionAvenant;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

@Repository
public class PeriodeInterruptionAvenantRepository extends PaginationRepository<PeriodeInterruptionAvenant> {

    public PeriodeInterruptionAvenantRepository(EntityManager em) {super(em, PeriodeInterruptionAvenant.class, "pia");}
}