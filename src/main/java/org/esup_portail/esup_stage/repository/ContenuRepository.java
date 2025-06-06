package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Contenu;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ContenuRepository extends PaginationRepository<Contenu> {
    public ContenuRepository(EntityManager em) {
        super(em, Contenu.class, "c");
        this.predicateWhitelist = Arrays.asList("code", "texte");
    }
}
