package org.esup_portail.esup_stage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

public class PdfMetadataDto {

    @NotNull
    @Schema(description = "Encodé en base64")
    private String pdf64;

    @NotNull
    private MetadataDto metadata;

    public String getPdf64() {
        return pdf64;
    }

    public void setPdf64(String pdf64) {
        this.pdf64 = pdf64;
    }

    public MetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDto metadata) {
        this.metadata = metadata;
    }
}
