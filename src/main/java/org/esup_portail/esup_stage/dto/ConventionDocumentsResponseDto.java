package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConventionDocumentsResponseDto {
    private String message;
    private int tailleMaxMo;
    private boolean canUpload;
    private boolean canDelete;
    private boolean canDownload;
    private boolean canPreview;
    private List<ConventionDocumentEtudiantDto> documents = new ArrayList<>();
}