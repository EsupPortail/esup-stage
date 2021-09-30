package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.OrigineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrigineStageJpaRepository extends JpaRepository<OrigineStage, Integer> {

    OrigineStage findById(int id);
}
