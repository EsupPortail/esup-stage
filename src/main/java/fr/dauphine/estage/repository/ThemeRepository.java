package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.Theme;
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
