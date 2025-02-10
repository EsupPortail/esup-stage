package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class MetadataDto {
    @NotNull
    private String title;

    @NotNull
    private String companyname;

    @NotNull
    private String school;

    @NotNull
    private String workflowId;

    @NotNull
    private List<MetadataSignataireDto> signatory;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public List<MetadataSignataireDto> getSignatory() {
        return signatory;
    }

    public void setSignatory(List<MetadataSignataireDto> signatory) {
        this.signatory = signatory;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }
}
