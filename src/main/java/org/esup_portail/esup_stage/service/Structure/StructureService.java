package org.esup_portail.esup_stage.service.Structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    public Structure save(String oldStructureJson, Structure structure) {
        boolean isNew = oldStructureJson == null;

        Structure savedStructure = structureJpaRepository.save(structure);

        if (isNew) {
            eventPublisher.publishEvent(new StructureCreatedEvent(savedStructure));
        } else {
            String newStructureJson;
            try {
                newStructureJson = objectMapper.writeValueAsString(savedStructure);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Erreur de sérialisation de la nouvelle structure", e);
            }
            eventPublisher.publishEvent(new StructureUpdatedEvent(structure,oldStructureJson, newStructureJson,false));
        }

        return savedStructure;
    }

    public void delete(Structure structure) {
        structure.setTemEnServStructure(false);
        structureJpaRepository.saveAndFlush(structure);
        eventPublisher.publishEvent(new StructureDeletedEvent(structure));
    }
}