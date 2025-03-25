package org.esup_portail.esup_stage.service.Structure;

import org.esup_portail.esup_stage.events.StructureCreatedEvent;
import org.esup_portail.esup_stage.events.StructureDeletedEvent;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.esup_portail.esup_stage.model.Structure;
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

    public Structure save(Structure structure) {
        boolean isNew = structure.getId() == null;
        Structure oldStructure = null;

        if (!isNew) {
            oldStructure = structureJpaRepository.findById(structure.getId()).orElse(null);
        }

        Structure savedStructure = structureJpaRepository.saveAndFlush(structure);

        if (isNew) {
            eventPublisher.publishEvent(new StructureCreatedEvent(savedStructure));
        } else {
            eventPublisher.publishEvent(new StructureUpdatedEvent(oldStructure, savedStructure));
        }

        return savedStructure;
    }

    public void delete(Integer id) {
        Structure structure = structureJpaRepository.findById(id).orElse(null);
        if (structure != null) {
            structureJpaRepository.delete(structure);
            eventPublisher.publishEvent(new StructureDeletedEvent(structure));
        }
    }
}