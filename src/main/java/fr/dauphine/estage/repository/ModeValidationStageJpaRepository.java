package fr.dauphine.estage.repository;

import fr.dauphine.estage.model.ModeValidationStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeValidationStageJpaRepository extends JpaRepository<ModeValidationStage, Integer> {

    ModeValidationStage findById(int id);
}
