package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Date;

public class UpdateDatesRequest {
    @NotNull
    private String mail;

    @NotNull
    private int order;

    @NotNull
    private Date signatureDate;

    private Date submissionDate;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public Date getSubmissionDate() {
        return submissionDate;
    }

    public void setSubmissionDate(Date submissionDate) {
        this.submissionDate = submissionDate;
    }
}
