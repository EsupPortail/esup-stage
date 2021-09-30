package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.OrigineStage;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Arrays;

@Repository
public class OrigineStageRepository extends PaginationRepository<OrigineStage> {

    public OrigineStageRepository(EntityManager em) {
        super(em, OrigineStage.class, "os");
        this.predicateWhitelist = Arrays.asList("id", "libelle");
    }
}
