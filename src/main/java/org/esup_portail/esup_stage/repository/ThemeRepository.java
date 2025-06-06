package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.model.Theme;
import org.springframework.stereotype.Repository;

import java.util.Arrays;

@Repository
public class ThemeRepository extends PaginationRepository<Theme> {

    public ThemeRepository(EntityManager em) {
        super(em, Theme.class, "t");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
