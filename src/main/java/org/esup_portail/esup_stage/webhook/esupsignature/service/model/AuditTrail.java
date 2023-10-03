package org.esup_portail.esup_stage.webhook.esupsignature.service.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditTrail {
    private int id;
    private int documentId;
    private String documentName;
    private List<AuditStep> auditSteps;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public List<AuditStep> getAuditSteps() {
        return auditSteps;
    }

    public void setAuditSteps(List<AuditStep> auditSteps) {
        this.auditSteps = auditSteps;
    }
}
