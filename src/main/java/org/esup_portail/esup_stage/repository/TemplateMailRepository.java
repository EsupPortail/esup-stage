package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.TemplateMail;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class TemplateMailRepository extends PaginationRepository<TemplateMail> {

    public TemplateMailRepository(EntityManager em) {
        super(em, TemplateMail.class, "tm");
        this.predicateWhitelist = Arrays.asList("code", "libelle", "objet");
    }
}
