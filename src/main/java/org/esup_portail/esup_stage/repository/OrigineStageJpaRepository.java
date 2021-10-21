package org.esup_portail.esup_stage.repository;

import org.esup_portail.esup_stage.model.OrigineStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrigineStageJpaRepository extends JpaRepository<OrigineStage, Integer> {

    OrigineStage findById(int id);
}
