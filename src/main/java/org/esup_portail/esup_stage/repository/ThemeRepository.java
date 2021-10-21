package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.Theme;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ThemeRepository extends PaginationRepository<Theme> {

    public ThemeRepository(EntityManager em) {
        super(em, Theme.class, "t");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
