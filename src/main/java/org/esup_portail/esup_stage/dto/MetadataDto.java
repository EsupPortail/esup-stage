package org.esup_portail.esup_stage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
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

    private List<MetadataObservateurDto> watchers;
}
