package org.esup_portail.esup_stage.webhook.esupsignature.service.model;

import java.util.ArrayList;
import java.util.List;

public class WorkflowStep {
    private int stepNumber = 1;
    private List<Recipient> recipients = new ArrayList<>();

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public List<Recipient> getRecipients() {
        return recipients;
    }

    public void setRecipients(List<Recipient> recipients) {
        this.recipients = recipients;
    }

}
