package org.esup_portail.esup_stage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportReportDto {
    private int totalLines;
    private int imported;
    private int duplicates;
    private List<LineErrorDto> errors = new ArrayList<>();
    private String fatalError; // ex: références manquantes, lecture fichier, etc.

    public boolean hasErrors() {
        return (fatalError != null && !fatalError.isBlank()) || !errors.isEmpty();
    }
}
