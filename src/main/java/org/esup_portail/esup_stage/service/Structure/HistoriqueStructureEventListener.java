package org.esup_portail.esup_stage.service.Structure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.model.HistoriqueStructure;
import org.esup_portail.esup_stage.repository.HistoriqueStructureJpaRepository;
import org.esup_portail.esup_stage.repository.StructureJpaRepository;
import org.esup_portail.esup_stage.events.StructureCreatedEvent;
import org.esup_portail.esup_stage.events.StructureUpdatedEvent;
import org.esup_portail.esup_stage.events.StructureDeletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.esup_portail.esup_stage.enums.OperationType;

@Component
public class HistoriqueStructureEventListener {

    @Autowired
    private HistoriqueStructureJpaRepository historiqueStructureJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StructureJpaRepository structureRepository;

    @EventListener
    public void handleStructureCreated(StructureCreatedEvent event) throws JsonProcessingException {
        HistoriqueStructure history = new HistoriqueStructure();
        history.setStructure(structureRepository.findById(event.getStructure().getId()).orElse(null));
        history.setOperationType(OperationType.CREATION);
        history.setOperationDate(event.getTimestamp());
        history.setUtilisateur(event.getUtilisateur());
        history.setEtatActuel(objectMapper.writeValueAsString(event.getStructure()));

        historiqueStructureJpaRepository.save(history);
    }

    @EventListener
    public void handleStructureUpdated(StructureUpdatedEvent event) throws JsonProcessingException {
        HistoriqueStructure history = new HistoriqueStructure();
        history.setStructure(structureRepository.findById(event.getStructure().getId()).orElse(null));
        if(event.isAuto()) {
            history.setOperationType(OperationType.MODIFICATION_AUTOMATIQUE);
        } else {
            history.setOperationType(OperationType.MODIFICATION);
        }
        history.setOperationDate(event.getTimestamp());
        history.setUtilisateur(event.getUtilisateur());
        history.setEtatActuel(event.getNewStructure());
        history.setEtatPrecedent(event.getOldStructure());

        historiqueStructureJpaRepository.save(history);
    }

    @EventListener
    public void handleStructureDeleted(StructureDeletedEvent event) throws JsonProcessingException {
        HistoriqueStructure history = new HistoriqueStructure();
        history.setStructure(structureRepository.findById(event.getStructure().getId()).orElse(null));
        history.setOperationType(OperationType.SUPPRESSION);
        history.setOperationDate(event.getTimestamp());
        history.setUtilisateur(event.getUtilisateur());
        history.setEtatActuel(objectMapper.writeValueAsString(event.getStructure()));

        historiqueStructureJpaRepository.save(history);
    }
}
