package org.esup_portail.esup_stage.service.evaluation;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.dto.EvaluationDto;
import org.esup_portail.esup_stage.model.QuestionSupplementaire;
import org.esup_portail.esup_stage.model.ReponseEvaluation;
import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.esup_portail.esup_stage.model.ReponseSupplementaire;
import org.springframework.stereotype.Component;
import org.esup_portail.esup_stage.enums.ExportType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EvaluationExcelExporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public byte[] export(List<EvaluationDto> evaluations, ExportType type) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if (type == ExportType.ALL_IN_ONE) {
                createSheet(workbook, evaluations, ExportType.ETUDIANT, "Évaluations Étudiant");
                createSheet(workbook, evaluations, ExportType.ENSEIGNANT, "Évaluations Enseignant");
                createSheet(workbook, evaluations, ExportType.ENTREPRISE, "Évaluations Entreprise");
            } else {
                createSheet(workbook, evaluations, type, "Évaluations");
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du fichier Excel", e);
        }
    }

    private void createSheet(Workbook workbook, List<EvaluationDto> evaluations, ExportType type, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        List<String> headers = getHeaders(evaluations, type);
        createHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (EvaluationDto eval : evaluations) {
            if (shouldIncludeEvaluation(eval, type)) {
                Row row = sheet.createRow(rowNum++);
                fillDataRow(row, eval, type, dataStyle);
            }
        }

        autoSizeColumns(sheet, headers.size());
    }

    private boolean shouldIncludeEvaluation(EvaluationDto eval, ExportType type) {
        if (eval.getFicheEvaluation() == null || eval.getReponseEvaluation() == null) {
            return false;
        }

        FicheEvaluation fiche = eval.getFicheEvaluation();

        switch (type) {
            case ETUDIANT:
                return fiche.getValidationEtudiant() != null && fiche.getValidationEtudiant();
            case ENSEIGNANT:
                return fiche.getValidationEnseignant() != null && fiche.getValidationEnseignant();
            case ENTREPRISE:
                return fiche.getValidationEntreprise() != null && fiche.getValidationEntreprise();
            default:
                return true;
        }
    }

    private List<String> getHeaders(List<EvaluationDto> evaluations, ExportType type) {
        List<String> headers = new ArrayList<>();

        // Colonnes communes
        headers.add("ID Convention");
        headers.add("Nom Étudiant");
        headers.add("Prénom Étudiant");
        headers.add("Structure");
        headers.add("Date Début Stage");
        headers.add("Date Fin Stage");
        headers.add("Centre de Gestion");
        headers.add("Étape");
        headers.add("Année Universitaire");

        // Colonnes spécifiques selon le type
        switch (type) {
            case ETUDIANT:
                addEtudiantHeaders(headers);
                break;
            case ENSEIGNANT:
                addEnseignantHeaders(headers);
                break;
            case ENTREPRISE:
                addEntrepriseHeaders(headers);
                break;
        }

        // Questions supplémentaires - récupérer toutes les questions uniques
        addQuestionSupplementaireHeaders(headers, evaluations, type);

        return headers;
    }

    private void addEtudiantHeaders(List<String> headers) {
        // Section I
        headers.add("Etu I.1 - Réponse");
        headers.add("Etu I.1 - Commentaire");
        headers.add("Etu I.2");
        headers.add("Etu I.3");
        headers.add("Etu I.4a");
        headers.add("Etu I.4b");
        headers.add("Etu I.4c");
        headers.add("Etu I.4d");
        headers.add("Etu I.5");
        headers.add("Etu I.6");
        headers.add("Etu I.7");
        headers.add("Etu I.7bis1");
        headers.add("Etu I.7bis1a");
        headers.add("Etu I.7bis1b");
        headers.add("Etu I.7bis2");
        headers.add("Etu I.8");

        // Section II
        headers.add("Etu II.1 - Réponse");
        headers.add("Etu II.1 - Commentaire");
        headers.add("Etu II.2 - Réponse");
        headers.add("Etu II.2 - Commentaire");
        headers.add("Etu II.3 - Réponse");
        headers.add("Etu II.3 - Commentaire");
        headers.add("Etu II.4");
        headers.add("Etu II.5");
        headers.add("Etu II.5a");
        headers.add("Etu II.5b");
        headers.add("Etu II.6");

        // Section III
        headers.add("Etu III.1");
        headers.add("Etu III.1 - Commentaire");
        headers.add("Etu III.2");
        headers.add("Etu III.2 - Commentaire");
        headers.add("Etu III.3");
        headers.add("Etu III.3 - Commentaire");
        headers.add("Etu III.4");
        headers.add("Etu III.5a");
        headers.add("Etu III.5b");
        headers.add("Etu III.5c");
        headers.add("Etu III.5 - Commentaire");
        headers.add("Etu III.6 - Réponse");
        headers.add("Etu III.6 - Commentaire");
        headers.add("Etu III.7 - Réponse");
        headers.add("Etu III.7 - Commentaire");
        headers.add("Etu III.8");
        headers.add("Etu III.8 - Commentaire");
        headers.add("Etu III.9");
        headers.add("Etu III.9 - Commentaire");
        headers.add("Etu III.10");
        headers.add("Etu III.11");
        headers.add("Etu III.12");
        headers.add("Etu III.13");
        headers.add("Etu III.14");
        headers.add("Etu III.15 - Réponse");
        headers.add("Etu III.15 - Commentaire");
        headers.add("Etu III.16 - Réponse");
        headers.add("Etu III.16 - Commentaire");
    }

    private void addEnseignantHeaders(List<String> headers) {
        // Section I
        headers.add("Ens I.1a");
        headers.add("Ens I.1b");
        headers.add("Ens I.1c");
        headers.add("Ens I.2a");
        headers.add("Ens I.2b");
        headers.add("Ens I.2c");
        headers.add("Ens I.3 - Commentaire");

        // Section II
        headers.add("Ens II.1");
        headers.add("Ens II.2");
        headers.add("Ens II.3");
        headers.add("Ens II.4");
        headers.add("Ens II.5");
        headers.add("Ens II.6");
        headers.add("Ens II.7");
        headers.add("Ens II.8");
        headers.add("Ens II.9");
        headers.add("Ens II.10");
        headers.add("Ens II.11 - Commentaire");
    }

    private void addEntrepriseHeaders(List<String> headers) {
        headers.add("Ent 1 - Réponse");
        headers.add("Ent 1 - Commentaire");
        headers.add("Ent 2 - Réponse");
        headers.add("Ent 2 - Commentaire");
        headers.add("Ent 3");
        headers.add("Ent 4 - Réponse");
        headers.add("Ent 4 - Commentaire");
        headers.add("Ent 5 - Réponse");
        headers.add("Ent 5 - Commentaire");
        headers.add("Ent 6 - Réponse");
        headers.add("Ent 6 - Commentaire");
        headers.add("Ent 7 - Réponse");
        headers.add("Ent 7 - Commentaire");
        headers.add("Ent 8 - Réponse");
        headers.add("Ent 8 - Commentaire");
        headers.add("Ent 9 - Réponse");
        headers.add("Ent 9 - Commentaire");
        headers.add("Ent 10");
        headers.add("Ent 10 - Commentaire");
        headers.add("Ent 11 - Réponse");
        headers.add("Ent 11 - Commentaire");
        headers.add("Ent 12 - Réponse");
        headers.add("Ent 12 - Commentaire");
        headers.add("Ent 13 - Réponse");
        headers.add("Ent 13 - Commentaire");
        headers.add("Ent 14 - Réponse");
        headers.add("Ent 14 - Commentaire");
        headers.add("Ent 15 - Réponse");
        headers.add("Ent 15 - Commentaire");
        headers.add("Ent 16 - Réponse");
        headers.add("Ent 16 - Commentaire");
        headers.add("Ent 17 - Réponse");
        headers.add("Ent 17 - Commentaire");
        headers.add("Ent 18");
        headers.add("Ent 18 - Commentaire");
        headers.add("Ent 19 - Commentaire");
    }

    private void createHeaderRow(Sheet sheet, List<String> headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillDataRow(Row row, EvaluationDto eval, ExportType type, CellStyle dataStyle) {
        int colNum = 0;

        // Colonnes communes
        createCell(row, colNum++, eval.getIdConvention(), dataStyle);
        createCell(row, colNum++, eval.getEtudiant() != null ? eval.getEtudiant().getNom() : "", dataStyle);
        createCell(row, colNum++, eval.getEtudiant() != null ? eval.getEtudiant().getPrenom() : "", dataStyle);
        createCell(row, colNum++, eval.getStructure() != null ? eval.getStructure().getRaisonSociale() : "", dataStyle);
        createCell(row, colNum++, eval.getDateDebutStage() != null ? DATE_FORMAT.format(eval.getDateDebutStage()) : "", dataStyle);
        createCell(row, colNum++, eval.getDateFinStage() != null ? DATE_FORMAT.format(eval.getDateFinStage()) : "", dataStyle);
        createCell(row, colNum++, eval.getCentreGestion() != null ? eval.getCentreGestion().getNomCentre() : "", dataStyle);
        createCell(row, colNum++, eval.getEtape() != null ? eval.getEtape().getLibelle() : "", dataStyle);
        createCell(row, colNum++, eval.getAnneeUniversitaire(), dataStyle);

        ReponseEvaluation reponse = eval.getReponseEvaluation();
        if (reponse == null) {
            return;
        }

        // Colonnes spécifiques
        switch (type) {
            case ETUDIANT:
                colNum = fillEtudiantData(row, colNum, reponse, dataStyle);
                break;
            case ENSEIGNANT:
                colNum = fillEnseignantData(row, colNum, reponse, dataStyle);
                break;
            case ENTREPRISE:
                colNum = fillEntrepriseData(row, colNum, reponse, dataStyle);
                break;
        }

        List<QuestionSupplementaire> questions = eval.getQuestionSupplementaires();
        List<ReponseSupplementaire> reps = eval.getReponseSupplementaires();

        if (questions != null && !questions.isEmpty()) {
            List<QuestionSupplementaire> filteredQuestions = questions.stream()
                    .filter(q -> isQuestionForType(q.getIdPlacement(), type))
                    .sorted(Comparator.comparingInt(QuestionSupplementaire::getIdPlacement))
                    .toList();

            // Pour chaque question filtrée, trouver la réponse correspondante
            for (QuestionSupplementaire question : filteredQuestions) {
                Object reponseValue = "";

                if (reps != null) {
                    ReponseSupplementaire repSup = reps.stream()
                            .filter(r -> r.getQuestionSupplementaire() != null
                                    && r.getQuestionSupplementaire().getId() == (question.getId()))
                            .findFirst()
                            .orElse(null);

                    if (repSup != null) {
                        reponseValue = getReponseSupplementaireValue(repSup);
                    }
                }

                createCell(row, colNum++, reponseValue, dataStyle);
            }
        }
    }

    private int fillEtudiantData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // Section I
        createCell(row, colNum++, r.getReponseEtuI1(), style);
        createCell(row, colNum++, r.getReponseEtuI1bis(), style);
        createCell(row, colNum++, r.getReponseEtuI2(), style);
        createCell(row, colNum++, r.getReponseEtuI3(), style);
        createCell(row, colNum++, r.getReponseEtuI4a(), style);
        createCell(row, colNum++, r.getReponseEtuI4b(), style);
        createCell(row, colNum++, r.getReponseEtuI4c(), style);
        createCell(row, colNum++, r.getReponseEtuI4d(), style);
        createCell(row, colNum++, r.getReponseEtuI5(), style);
        createCell(row, colNum++, r.getReponseEtuI6(), style);
        createCell(row, colNum++, r.getReponseEtuI7(), style);
        createCell(row, colNum++, r.getReponseEtuI7bis1(), style);
        createCell(row, colNum++, r.getReponseEtuI7bis1a(), style);
        createCell(row, colNum++, r.getReponseEtuI7bis1b(), style);
        createCell(row, colNum++, r.getReponseEtuI7bis2(), style);
        createCell(row, colNum++, r.getReponseEtuI8(), style);

        // Section II
        createCell(row, colNum++, r.getReponseEtuII1(), style);
        createCell(row, colNum++, r.getReponseEtuII1bis(), style);
        createCell(row, colNum++, r.getReponseEtuII2(), style);
        createCell(row, colNum++, r.getReponseEtuII2bis(), style);
        createCell(row, colNum++, r.getReponseEtuII3(), style);
        createCell(row, colNum++, r.getReponseEtuII3bis(), style);
        createCell(row, colNum++, r.getReponseEtuII4(), style);
        createCell(row, colNum++, r.getReponseEtuII5(), style);
        createCell(row, colNum++, r.getReponseEtuII5a(), style);
        createCell(row, colNum++, r.getReponseEtuII5b(), style);
        createCell(row, colNum++, r.getReponseEtuII6(), style);

        // Section III
        createCell(row, colNum++, r.getReponseEtuIII1(), style);
        createCell(row, colNum++, r.getReponseEtuIII1bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII2(), style);
        createCell(row, colNum++, r.getReponseEtuIII2bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII3(), style);
        createCell(row, colNum++, r.getReponseEtuIII3bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII4(), style);
        createCell(row, colNum++, r.getReponseEtuIII5a(), style);
        createCell(row, colNum++, r.getReponseEtuIII5b(), style);
        createCell(row, colNum++, r.getReponseEtuIII5c(), style);
        createCell(row, colNum++, r.getReponseEtuIII5bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII6(), style);
        createCell(row, colNum++, r.getReponseEtuIII6bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII7(), style);
        createCell(row, colNum++, r.getReponseEtuIII7bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII8(), style);
        createCell(row, colNum++, r.getReponseEtuIII8bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII9(), style);
        createCell(row, colNum++, r.getReponseEtuIII9bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII10(), style);
        createCell(row, colNum++, r.getReponseEtuIII11(), style);
        createCell(row, colNum++, r.getReponseEtuIII12(), style);
        createCell(row, colNum++, r.getReponseEtuIII13(), style);
        createCell(row, colNum++, r.getReponseEtuIII14(), style);
        createCell(row, colNum++, r.getReponseEtuIII15(), style);
        createCell(row, colNum++, r.getReponseEtuIII15bis(), style);
        createCell(row, colNum++, r.getReponseEtuIII16(), style);
        createCell(row, colNum++, r.getReponseEtuIII16bis(), style);

        return colNum;
    }

    private int fillEnseignantData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // Section I
        createCell(row, colNum++, r.getReponseEnsI1a(), style);
        createCell(row, colNum++, r.getReponseEnsI1b(), style);
        createCell(row, colNum++, r.getReponseEnsI1c(), style);
        createCell(row, colNum++, r.getReponseEnsI2a(), style);
        createCell(row, colNum++, r.getReponseEnsI2b(), style);
        createCell(row, colNum++, r.getReponseEnsI2c(), style);
        createCell(row, colNum++, r.getReponseEnsI3(), style);

        // Section II
        createCell(row, colNum++, r.getReponseEnsII1(), style);
        createCell(row, colNum++, r.getReponseEnsII2(), style);
        createCell(row, colNum++, r.getReponseEnsII3(), style);
        createCell(row, colNum++, r.getReponseEnsII4(), style);
        createCell(row, colNum++, r.getReponseEnsII5(), style);
        createCell(row, colNum++, r.getReponseEnsII6(), style);
        createCell(row, colNum++, r.getReponseEnsII7(), style);
        createCell(row, colNum++, r.getReponseEnsII8(), style);
        createCell(row, colNum++, r.getReponseEnsII9(), style);
        createCell(row, colNum++, r.getReponseEnsII10(), style);
        createCell(row, colNum++, r.getReponseEnsII11(), style);

        return colNum;
    }

    private int fillEntrepriseData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        createCell(row, colNum++, r.getReponseEnt1(), style);
        createCell(row, colNum++, r.getReponseEnt1bis(), style);
        createCell(row, colNum++, r.getReponseEnt2(), style);
        createCell(row, colNum++, r.getReponseEnt2bis(), style);
        createCell(row, colNum++, r.getReponseEnt3(), style);
        createCell(row, colNum++, r.getReponseEnt4(), style);
        createCell(row, colNum++, r.getReponseEnt4bis(), style);
        createCell(row, colNum++, r.getReponseEnt5(), style);
        createCell(row, colNum++, r.getReponseEnt5bis(), style);
        createCell(row, colNum++, r.getReponseEnt6(), style);
        createCell(row, colNum++, r.getReponseEnt6bis(), style);
        createCell(row, colNum++, r.getReponseEnt7(), style);
        createCell(row, colNum++, r.getReponseEnt7bis(), style);
        createCell(row, colNum++, r.getReponseEnt8(), style);
        createCell(row, colNum++, r.getReponseEnt8bis(), style);
        createCell(row, colNum++, r.getReponseEnt9(), style);
        createCell(row, colNum++, r.getReponseEnt9bis(), style);
        createCell(row, colNum++, r.getReponseEnt10(), style);
        createCell(row, colNum++, r.getReponseEnt10bis(), style);
        createCell(row, colNum++, r.getReponseEnt11(), style);
        createCell(row, colNum++, r.getReponseEnt11bis(), style);
        createCell(row, colNum++, r.getReponseEnt12(), style);
        createCell(row, colNum++, r.getReponseEnt12bis(), style);
        createCell(row, colNum++, r.getReponseEnt13(), style);
        createCell(row, colNum++, r.getReponseEnt13bis(), style);
        createCell(row, colNum++, r.getReponseEnt14(), style);
        createCell(row, colNum++, r.getReponseEnt14bis(), style);
        createCell(row, colNum++, r.getReponseEnt15(), style);
        createCell(row, colNum++, r.getReponseEnt15bis(), style);
        createCell(row, colNum++, r.getReponseEnt16(), style);
        createCell(row, colNum++, r.getReponseEnt16bis(), style);
        createCell(row, colNum++, r.getReponseEnt17(), style);
        createCell(row, colNum++, r.getReponseEnt17bis(), style);
        createCell(row, colNum++, r.getReponseEnt18(), style);
        createCell(row, colNum++, r.getReponseEnt18bis(), style);
        createCell(row, colNum++, r.getReponseEnt19(), style);

        return colNum;
    }

    private void createCell(Row row, int colNum, Object value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "Oui" : "Non");
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Limite la largeur maximale des colonnes
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    private Object getReponseSupplementaireValue(org.esup_portail.esup_stage.model.ReponseSupplementaire repSup) {
        // Vérifier dans l'ordre : Boolean, Integer, puis String
        if (repSup.getReponseBool() != null) {
            return repSup.getReponseBool();
        } else if (repSup.getReponseInt() != null) {
            return repSup.getReponseInt();
        } else if (repSup.getReponseTxt() != null) {
            return repSup.getReponseTxt();
        }
        return "";
    }

    private void addQuestionSupplementaireHeaders(List<String> headers, List<EvaluationDto> evaluations, ExportType type) {
        // Récupérer toutes les questions supplémentaires uniques
        if (evaluations.isEmpty()) {
            return;
        }

        // Utiliser la première évaluation qui a des questions
        for (EvaluationDto eval : evaluations) {
            if (eval.getQuestionSupplementaires() != null && !eval.getQuestionSupplementaires().isEmpty()) {
                // Filtrer selon le type et l'idPlacement
                eval.getQuestionSupplementaires().stream()
                        .filter(q -> isQuestionForType(q.getIdPlacement(), type))
                        .sorted(Comparator.comparingInt(QuestionSupplementaire::getIdPlacement))
                        .forEach(q -> headers.add("QS" + q.getIdPlacement() + ": " + q.getQuestion()));
                break;
            }
        }
    }

    private boolean isQuestionForType(Integer idPlacement, ExportType type) {
        if (idPlacement == null) {
            return false;
        }

        return switch (type) {
            case ETUDIANT ->
                // idPlacement de 0 à 2 pour les étudiants
                    idPlacement >= 0 && idPlacement <= 2;
            case ENSEIGNANT ->
                // idPlacement 3 et 4 pour les enseignants
                    idPlacement >= 3 && idPlacement <= 4;
            case ENTREPRISE ->
                // idPlacement de 5 à 7 pour les tuteurs/entreprise
                    idPlacement >= 5 && idPlacement <= 7;
            case ALL_IN_ONE ->
                // Pour ALL_IN_ONE, on inclut toutes les questions
                    true;
            default -> false;
        };
    }
}