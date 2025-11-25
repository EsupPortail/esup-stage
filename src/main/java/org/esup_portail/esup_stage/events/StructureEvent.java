package org.esup_portail.esup_stage.events;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.security.ServiceContext;

import java.time.LocalDateTime;

public abstract class StructureEvent {
    private final LocalDateTime timestamp;
    private final Utilisateur utilisateur;

    protected StructureEvent() {
        this.timestamp = LocalDateTime.now();
        this.utilisateur = ServiceContext.getUtilisateur();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
}