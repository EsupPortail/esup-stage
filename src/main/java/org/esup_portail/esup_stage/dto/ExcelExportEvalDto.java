package org.esup_portail.esup_stage.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExcelExportEvalDto {

    List<Integer> idConventions;
    Integer typeFiche;
    List<String> colonnes;
}
