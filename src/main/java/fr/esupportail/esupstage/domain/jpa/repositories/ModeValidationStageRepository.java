package fr.esupportail.esupstage.domain.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.esupportail.esupstage.domain.jpa.entities.ModeValidationStage;

@Repository
public interface ModeValidationStageRepository extends JpaRepository<ModeValidationStage, Integer> {
}
