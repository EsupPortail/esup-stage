package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ModeValidationStage;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class ModeValidationStageRepository extends PaginationRepository<ModeValidationStage> {

    public ModeValidationStageRepository(EntityManager em) {
        super(em, ModeValidationStage.class, "mv");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
