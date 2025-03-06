package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.TemplateMailGroupe;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class TemplateMailGroupeRepository extends PaginationRepository<TemplateMailGroupe> {

    public TemplateMailGroupeRepository(EntityManager em) {
        super(em, TemplateMailGroupe.class, "tm");
        this.predicateWhitelist = Arrays.asList("code", "libelle", "objet");
    }
}
