package org.esup_portail.esup_stage.service.Structure;

import jakarta.persistence.EntityManager;
import org.esup_portail.esup_stage.events.StructureCreatedEvent;
import org.esup_portail.esup_stage.events.StructureDeletedEvent;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class StructureService {

    @Autowired
    private StructureJpaRepository structureJpaRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private EntityManager entityManager;

    public Structure save(Structure structure) {
        boolean isNew = structure.getId() == null || structure.getId() == 0;
        Structure oldStructure = null;

        if (!isNew) {
            oldStructure = structureJpaRepository.findById(structure.getId()).orElse(null);
            entityManager.detach(oldStructure);
        }

        Structure savedStructure = structureJpaRepository.save(structure);

        if (isNew) {
            eventPublisher.publishEvent(new StructureCreatedEvent(savedStructure));
        } else {
            eventPublisher.publishEvent(new StructureUpdatedEvent(oldStructure, savedStructure));
        }

        return savedStructure;
    }

    public void delete(Structure structure) {
        structure.setTemEnServStructure(false);
        structureJpaRepository.saveAndFlush(structure);
        eventPublisher.publishEvent(new StructureDeletedEvent(structure));
    }
}