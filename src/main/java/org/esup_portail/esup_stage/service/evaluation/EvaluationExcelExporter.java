package org.esup_portail.esup_stage.service.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.dto.EvaluationDto;
import org.esup_portail.esup_stage.enums.ExportType;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
import org.esup_portail.esup_stage.model.FicheEvaluation;
import org.esup_portail.esup_stage.model.QuestionEvaluation;
import org.esup_portail.esup_stage.model.QuestionSupplementaire;
import org.esup_portail.esup_stage.model.ReponseEvaluation;
import org.esup_portail.esup_stage.model.ReponseSupplementaire;
import org.esup_portail.esup_stage.repository.QuestionEvaluationJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EvaluationExcelExporter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    @Autowired
    private QuestionEvaluationJpaRepository questionEvaluationJpaRepository;

    private Map<String, QuestionEvaluation> questionsByCode;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final List<String> LIKERT_5 = List.of("Excellent","Très bien","Bien","Satisfaisant","Insuffisant");
    private static final List<String> AGREEMENT_5 = List.of("Tout à fait d'accord","Plutôt d'accord","Sans avis","Plutôt pas d'accord","Pas du tout d'accord");

    public byte[] export(List<EvaluationDto> evaluations, ExportType type) {
        loadQuestionsCache();

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

    private void loadQuestionsCache() {
        if (questionsByCode != null) return;
        List<QuestionEvaluation> all = questionEvaluationJpaRepository.findAll();
        questionsByCode = all.stream().collect(Collectors.toMap(
                QuestionEvaluation::getCode,
                q -> q,
                (a, b) -> a,
                LinkedHashMap::new
        ));
    }

    private String qText(String code, String fallback) {
        QuestionEvaluation q = questionsByCode.get(code);
        return q != null && q.getTexte() != null && !q.getTexte().isBlank() ? q.getTexte().trim() : fallback;
    }

    private String bisText(String code, String fallback) {
        QuestionEvaluation q = questionsByCode.get(code);
        return (q != null && q.getBisQuestion() != null && !q.getBisQuestion().isBlank())
                ? q.getBisQuestion().trim()
                : fallback;
    }

    private List<String> qItems(String code) {
        QuestionEvaluation q = questionsByCode.get(code);
        if (q == null || q.getParamsJson() == null || q.getParamsJson().isBlank()) return Collections.emptyList();
        try {
            Map<String, Object> m = mapper.readValue(q.getParamsJson(), new TypeReference<Map<String, Object>>() {});
            Object items = m.get("items");
            if (items instanceof List<?> list) {
                return list.stream().map(String::valueOf).toList();
            }
        } catch (Exception ignored) {}
        return Collections.emptyList();
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

        // Colonnes spécifiques
        switch (type) {
            case ETUDIANT -> addEtudiantHeaders(headers);
            case ENSEIGNANT -> addEnseignantHeaders(headers);
            case ENTREPRISE -> addEntrepriseHeaders(headers);
        }

        // Questions supplémentaires
        addQuestionSupplementaireHeaders(headers, evaluations, type);

        return headers;
    }

    private void addEtudiantHeaders(List<String> headers) {
        // I.1
        headers.add(qText("ETUI1", "I.1"));
        headers.add(bisText("ETUI1", "Commentaire"));

        // I.2, I.3
        headers.add(qText("ETUI2", "I.2"));
        headers.add(qText("ETUI3", "I.3"));

        // I.4 BOOLEAN_GROUP
        List<String> etuI4Items = qItems("ETUI4");
        String etuI4Label = qText("ETUI4", "I.4");
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 0 ? etuI4Items.get(0) : "a"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 1 ? etuI4Items.get(1) : "b"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 2 ? etuI4Items.get(2) : "c"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 3 ? etuI4Items.get(3) : "d"));

        // I.5, I.6
        headers.add(qText("ETUI5", "I.5"));
        headers.add(qText("ETUI6", "I.6"));

        // I.7 + sous-questions
        headers.add(qText("ETUI7", "I.7"));
        headers.add(qText("ETUI7", "I.7") + " - Si oui, par qui ?");
        headers.add(qText("ETUI7", "I.7") + " - Si non, pourquoi ?");

        // I.8
        headers.add(qText("ETUI8", "I.8"));

        // II.1 (pas de code dédié - fallback manuel)
        headers.add(qText("ETUII1", "II.1"));
        headers.add(bisText("ETUII1", "Commentaire"));

        // II.2
        headers.add(qText("ETUII2", "II.2"));
        headers.add(bisText("ETUII2", "Commentaire"));

        // II.3
        headers.add(qText("ETUII3", "II.3"));
        headers.add(bisText("ETUII3", "Commentaire"));

        // II.4
        headers.add(qText("ETUII4", "II.4"));

        // II.5
        headers.add(qText("ETUII5", "II.5"));
        headers.add(qText("ETUII5", "II.5") + " - De quel ordre ?");
        headers.add(qText("ETUII5", "II.5") + " - Avec autonomie ?");

        // II.6
        headers.add(qText("ETUII6", "II.6"));

        // III.1
        headers.add(qText("ETUIII1", "III.1"));
        headers.add(bisText("ETUIII1", "Commentaire"));

        // III.2
        headers.add(qText("ETUIII2", "III.2"));
        headers.add(bisText("ETUIII2", "Commentaire"));

        // III.4
        headers.add(qText("ETUIII4", "III.4"));

        // III.5 BOOLEAN_GROUP
        List<String> etuIII5Items = qItems("ETUIII5");
        String etuIII5Label = qText("ETUIII5", "III.5");
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 0 ? etuIII5Items.get(0) : "a"));
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 1 ? etuIII5Items.get(1) : "b"));
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 2 ? etuIII5Items.get(2) : "c"));
        headers.add(etuIII5Label + " - Commentaire");

        // III.6 à III.9
        headers.add(qText("ETUIII6", "III.6"));
        headers.add(bisText("ETUIII6", "Commentaire"));
        headers.add(qText("ETUIII7", "III.7"));
        headers.add(bisText("ETUIII7", "Commentaire"));
        headers.add(qText("ETUIII8", "III.8"));
        headers.add(bisText("ETUIII8", "Commentaire"));
        headers.add(qText("ETUIII9", "III.9"));
        headers.add(bisText("ETUIII9", "Commentaire"));

        // III.10 à III.14
        headers.add(qText("ETUIII10", "III.10"));
        headers.add(qText("ETUIII11", "III.11"));
        headers.add(qText("ETUIII12", "III.12"));
        headers.add(qText("ETUIII14", "III.14"));

        // III.15, III.16
        headers.add(qText("ETUIII15", "III.15"));
        headers.add(bisText("ETUIII15", "Commentaire"));
        headers.add(qText("ETUIII16", "III.16"));
        headers.add(bisText("ETUIII16", "Commentaire"));
    }

    private void addEnseignantHeaders(List<String> headers) {
        // I.1 (ENSI1) BOOLEAN_GROUP
        List<String> ensI1Items = qItems("ENSI1");
        String ensI1 = qText("ENSI1", "Suivi avec le stagiaire");
        headers.add(ensI1 + " - " + (ensI1Items.size() > 0 ? ensI1Items.get(0) : "a"));
        headers.add(ensI1 + " - " + (ensI1Items.size() > 1 ? ensI1Items.get(1) : "b"));
        headers.add(ensI1 + " - " + (ensI1Items.size() > 2 ? ensI1Items.get(2) : "c"));

        // I.2 (ENSI2) BOOLEAN_GROUP
        List<String> ensI2Items = qItems("ENSI2");
        String ensI2 = qText("ENSI2", "Suivi avec le tuteur professionnel");
        headers.add(ensI2 + " - " + (ensI2Items.size() > 0 ? ensI2Items.get(0) : "a"));
        headers.add(ensI2 + " - " + (ensI2Items.size() > 1 ? ensI2Items.get(1) : "b"));
        headers.add(ensI2 + " - " + (ensI2Items.size() > 2 ? ensI2Items.get(2) : "c"));

        // I.3 (ENSI3) commentaire libre
        headers.add(qText("ENSI3", "Commentaire(s)"));

        // II.* (ENSII1..ENSII11)
        headers.add(qText("ENSII1", "II.1"));
        headers.add(qText("ENSII10", "II.10"));
        headers.add(qText("ENSII11", "II.11"));
        headers.add(qText("ENSII2", "II.2"));
        headers.add(qText("ENSII3", "II.3"));
        headers.add(qText("ENSII4", "II.4"));
        headers.add(qText("ENSII5", "II.5"));
        headers.add(qText("ENSII6", "II.6"));
        headers.add(qText("ENSII7", "II.7"));
        headers.add(qText("ENSII8", "II.8"));
        headers.add(qText("ENSII9", "II.9"));
    }

    private void addEntrepriseHeaders(List<String> headers) {
        // I. savoir-être
        headers.add(qText("ENT1", "Ent 1"));
        headers.add(bisText("ENT1", "Commentaire"));
        headers.add(qText("ENT2", "Ent 2"));
        headers.add(bisText("ENT2", "Commentaire"));
        headers.add(qText("ENT3", "Ent 3"));
        headers.add(qText("ENT5", "Ent 4"));
        headers.add(bisText("ENT5", "Commentaire"));
        headers.add(qText("ENT9", "Ent 5"));
        headers.add(bisText("ENT9", "Commentaire"));
        headers.add(qText("ENT11", "Ent 6"));
        headers.add(bisText("ENT11", "Commentaire"));
        headers.add(qText("ENT12", "Ent 7"));
        headers.add(bisText("ENT12", "Commentaire"));
        headers.add(qText("ENT13", "Ent 8"));
        headers.add(bisText("ENT13", "Commentaire"));
        headers.add(qText("ENT14", "Ent 9"));
        headers.add(bisText("ENT14", "Commentaire"));

        // II. savoir-faire
        headers.add(qText("ENT4", "Ent 10"));
        headers.add(bisText("ENT4", "Commentaire"));
        headers.add(qText("ENT6", "Ent 11"));
        headers.add(bisText("ENT6", "Commentaire"));
        headers.add(qText("ENT7", "Ent 12"));
        headers.add(bisText("ENT7", "Commentaire"));
        headers.add(qText("ENT8", "Ent 13"));
        headers.add(bisText("ENT8", "Commentaire"));
        headers.add(qText("ENT15", "Ent 14"));
        headers.add(bisText("ENT15", "Commentaire"));

        // III. appréciation générale
        headers.add(qText("ENT16", "Ent 15"));
        headers.add(bisText("ENT16", "Commentaire"));
        headers.add(qText("ENT17", "Ent 16"));
        headers.add(bisText("ENT17", "Commentaire"));
        headers.add(qText("ENT19", "Ent 19"));
        headers.add(qText("ENT10", "Ent 10"));
        headers.add(qText("ENT18", "Ent 18"));
        headers.add(bisText("ENT18", "Commentaire"));
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

        // Spécifiques
        switch (type) {
            case ETUDIANT -> colNum = fillEtudiantData(row, colNum, reponse, dataStyle);
            case ENSEIGNANT -> colNum = fillEnseignantData(row, colNum, reponse, dataStyle);
            case ENTREPRISE -> colNum = fillEntrepriseData(row, colNum, reponse, dataStyle);
        }

        // QS
        List<QuestionSupplementaire> questions = eval.getQuestionSupplementaires();
        List<ReponseSupplementaire> reps = eval.getReponseSupplementaires();

        if (questions != null && !questions.isEmpty()) {
            List<QuestionSupplementaire> filteredQuestions = questions.stream()
                    .filter(q -> isQuestionForType(q.getIdPlacement(), type))
                    .sorted(Comparator.comparingInt(QuestionSupplementaire::getIdPlacement))
                    .toList();

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

        createCell(row, colNum++, "ETUI1",  r.getReponseEtuI1(), style);          // I.1 - Réponse
        createCell(row, colNum++, "ETUI1bis", r.getReponseEtuI1bis(), style);                      // I.1 - Commentaire
        createCell(row, colNum++, "ETUI2",  r.getReponseEtuI2(), style);          // I.2
        createCell(row, colNum++, "ETUI3",  r.getReponseEtuI3(), style);          // I.3
        createCell(row, colNum++, "ETUI4a", r.getReponseEtuI4a(), style);         // I.4 - a
        createCell(row, colNum++, "ETUI4b", r.getReponseEtuI4b(), style);         // I.4 - b
        createCell(row, colNum++, "ETUI4c", r.getReponseEtuI4c(), style);         // I.4 - c
        createCell(row, colNum++, "ETUI4d", r.getReponseEtuI4d(), style);         // I.4 - d
        createCell(row, colNum++, "ETUI5",  r.getReponseEtuI5(), style);          // I.5
        createCell(row, colNum++, "ETUI6",  r.getReponseEtuI6(), style);          // I.6
        createCell(row, colNum++, "ETUI7",  r.getReponseEtuI7(), style);      // I.7
        createCell(row, colNum++, "ETUI7_bis1", r.getReponseEtuI7bis1(), style);  // I.7 - Si oui, par qui ?
        createCell(row, colNum++, "ETUI7_bis2", r.getReponseEtuI7bis2(), style);  // I.7 - Si non, pourquoi ?
        createCell(row, colNum++, "ETUI8", r.getReponseEtuI8(), style);           // I.8

        createCell(row, colNum++, "ETUII1",  r.getReponseEtuII1(), style);        // II.1
        createCell(row, colNum++,"ETUII1bis",  r.getReponseEtuII1bis(), style);                     // II.1 - Commentaire
        createCell(row, colNum++, "ETUII2",  r.getReponseEtuII2(), style);        // II.2
        createCell(row, colNum++,"ETUII2bis",  r.getReponseEtuII2bis(), style);                     // II.2 - Commentaire
        createCell(row, colNum++, "ETUII3",  r.getReponseEtuII3(), style);        // II.3
        createCell(row, colNum++,"ETUII3bis",  r.getReponseEtuII3bis(), style);                     // II.3 - Commentaire
        createCell(row, colNum++, "ETUII4",  r.getReponseEtuII4(), style);        // II.4
        createCell(row, colNum++, "ETUII5",  r.getReponseEtuII5(), style);        // II.5
        createCell(row, colNum++,"ETUII5a",  r.getReponseEtuII5a(), style);                       // II.5 - De quel ordre ?
        createCell(row, colNum++, "ETUII5b",  r.getReponseEtuII5b(), style);                       // II.5 - Avec autonomie ?
        createCell(row, colNum++, "ETUII6",  r.getReponseEtuII6(), style);        // II.6

        createCell(row, colNum++, "ETUIII1",   r.getReponseEtuIII1(), style);     // III.1
        createCell(row, colNum++,"ETUIII1bis",  r.getReponseEtuIII1bis(), style);                   // III.1 - Commentaire
        createCell(row, colNum++, "ETUIII2",   r.getReponseEtuIII2(), style);     // III.2
        createCell(row, colNum++, "ETUIII2bis",  r.getReponseEtuIII2bis(), style);                   // III.2 - Commentaire
        createCell(row, colNum++, "ETUIII4",   r.getReponseEtuIII4(), style);     // III.4
        createCell(row, colNum++, "ETUIII5a",  r.getReponseEtuIII5a(), style);    // III.5 - a
        createCell(row, colNum++, "ETUIII5b",  r.getReponseEtuIII5b(), style);    // III.5 - b
        createCell(row, colNum++, "ETUIII5c",  r.getReponseEtuIII5c(), style);    // III.5 - c
        createCell(row, colNum++,"ETUIII5bis",  r.getReponseEtuIII5bis(), style);                    // III.5 - Commentaire
        createCell(row, colNum++, "ETUIII6",   r.getReponseEtuIII6(), style);     // III.6
        createCell(row, colNum++,"ETUIII6bis",   r.getReponseEtuIII6bis(), style);                   // III.6 - Commentaire
        createCell(row, colNum++, "ETUIII7",   r.getReponseEtuIII7(), style);     // III.7
        createCell(row, colNum++,"ETUIII7bis",   r.getReponseEtuIII7bis(), style);                   // III.7 - Commentaire
        createCell(row, colNum++, "ETUIII8",   r.getReponseEtuIII8(), style);     // III.8
        createCell(row, colNum++,"ETUIII8bis",   r.getReponseEtuIII8bis(), style);                   // III.8 - Commentaire
        createCell(row, colNum++, "ETUIII9",   r.getReponseEtuIII9(), style);     // III.9
        createCell(row, colNum++,"ETUIII9bis",   r.getReponseEtuIII9bis(), style);                   // III.9 - Commentaire
        createCell(row, colNum++, "ETUIII10",  r.getReponseEtuIII10(), style);    // III.10
        createCell(row, colNum++, "ETUIII11",  r.getReponseEtuIII11(), style);    // III.11
        createCell(row, colNum++, "ETUIII12",  r.getReponseEtuIII12(), style);    // III.12
        createCell(row, colNum++, "ETUIII14",  r.getReponseEtuIII14(), style);    // III.14
        createCell(row, colNum++, "ETUIII15",  r.getReponseEtuIII15(), style);    // III.15
        createCell(row, colNum++,"ETUIII15bis",   r.getReponseEtuIII15bis(), style);                  // III.15 - Commentaire
        createCell(row, colNum++, "ETUIII16",  r.getReponseEtuIII16(), style);    // III.16
        createCell(row, colNum++,"ETUIII16bis",   r.getReponseEtuIII16bis(), style);                  // III.16 - Commentaire

        return colNum;
    }
    
    private int fillEnseignantData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // I.1, I.2, I.3
        createCell(row, colNum++, "ENSI1a", r.getReponseEnsI1a(), style);
        createCell(row, colNum++, "ENSI1b", r.getReponseEnsI1b(), style);
        createCell(row, colNum++, "ENSI1c", r.getReponseEnsI1c(), style);
        createCell(row, colNum++, "ENSI2a", r.getReponseEnsI2a(), style);
        createCell(row, colNum++, "ENSI2b", r.getReponseEnsI2b(), style);
        createCell(row, colNum++, "ENSI2c", r.getReponseEnsI2c(), style);
        createCell(row, colNum++, "ENSI3", r.getReponseEnsI3(), style);

        // II.1 à II.11
        createCell(row, colNum++, "ENSII1",  r.getReponseEnsII1(), style);
        createCell(row, colNum++, "ENSII10", r.getReponseEnsII10(), style);
        createCell(row, colNum++, "ENSII11", r.getReponseEnsII11(), style);
        createCell(row, colNum++, "ENSII2",  r.getReponseEnsII2(), style);
        createCell(row, colNum++, "ENSII3",  r.getReponseEnsII3(), style);
        createCell(row, colNum++, "ENSII4",  r.getReponseEnsII4(), style);
        createCell(row, colNum++, "ENSII5",  r.getReponseEnsII5(), style);
        createCell(row, colNum++, "ENSII6",  r.getReponseEnsII6(), style);
        createCell(row, colNum++, "ENSII7",  r.getReponseEnsII7(), style);
        createCell(row, colNum++, "ENSII8",  r.getReponseEnsII8(), style);
        createCell(row, colNum++, "ENSII9",  r.getReponseEnsII9(), style);


        return colNum;
    }

    private int fillEntrepriseData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // I. Savoir-être (ENT1-2-3-5-9-11-12-13-14)
        createCell(row, colNum++, "ENT1",  r.getReponseEnt1(), style);
        createCell(row, colNum++, r.getReponseEnt1bis(), style);
        createCell(row, colNum++, "ENT2",  r.getReponseEnt2(), style);
        createCell(row, colNum++, r.getReponseEnt2bis(), style);
        createCell(row, colNum++, "ENT3",  r.getReponseEnt3(), style);
        createCell(row, colNum++, "ENT5",  r.getReponseEnt5(), style);
        createCell(row, colNum++, r.getReponseEnt5bis(), style);
        createCell(row, colNum++, "ENT9",  r.getReponseEnt9(), style);
        createCell(row, colNum++, r.getReponseEnt9bis(), style);
        createCell(row, colNum++, "ENT11", r.getReponseEnt11(), style);
        createCell(row, colNum++, r.getReponseEnt11bis(), style);
        createCell(row, colNum++, "ENT12", r.getReponseEnt12(), style);
        createCell(row, colNum++, r.getReponseEnt12bis(), style);
        createCell(row, colNum++, "ENT13", r.getReponseEnt13(), style);
        createCell(row, colNum++, r.getReponseEnt13bis(), style);
        createCell(row, colNum++, "ENT14", r.getReponseEnt14(), style);
        createCell(row, colNum++, r.getReponseEnt14bis(), style);

        createCell(row, colNum++, "ENT4",  r.getReponseEnt4(), style);
        createCell(row, colNum++, r.getReponseEnt4bis(), style);
        createCell(row, colNum++, "ENT6",  r.getReponseEnt6(), style);
        createCell(row, colNum++, r.getReponseEnt6bis(), style);
        createCell(row, colNum++, "ENT7",  r.getReponseEnt7(), style);
        createCell(row, colNum++, r.getReponseEnt7bis(), style);
        createCell(row, colNum++, "ENT8",  r.getReponseEnt8(), style);
        createCell(row, colNum++, r.getReponseEnt8bis(), style);
        createCell(row, colNum++, "ENT15", r.getReponseEnt15(), style);
        createCell(row, colNum++, r.getReponseEnt15bis(), style);

        createCell(row, colNum++, "ENT16", r.getReponseEnt16(), style);
        createCell(row, colNum++, r.getReponseEnt16bis(), style);
        createCell(row, colNum++, "ENT17", r.getReponseEnt17(), style);
        createCell(row, colNum++, r.getReponseEnt17bis(), style);
        createCell(row, colNum++, r.getReponseEnt19(), style);
        createCell(row, colNum++, r.getReponseEnt10(), style);
        createCell(row, colNum++, "ENT18", r.getReponseEnt18(), style);
        createCell(row, colNum++, r.getReponseEnt18bis(), style);

        return colNum;
    }

    private void createCell(Row row, int colNum, String code, Object value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
            return;
        }

        QuestionEvaluation question = questionsByCode.get(code);
        if (question == null) {
            // Cas particuliers sans question en base
            cell.setCellValue(formatSpecialCaseValue(code, value));
            return;
        }

        TypeQuestionEvaluation typeQuestion = question.getType();

        switch (typeQuestion) {
            case TypeQuestionEvaluation.YES_NO:
                cell.setCellValue(formatYesNo(value));
                break;

            case TypeQuestionEvaluation.SCALE_LIKERT_5:
                cell.setCellValue(formatLikert5(value));
                break;

            case TypeQuestionEvaluation.SCALE_AGREEMENT_5:
                cell.setCellValue(formatAgreement5(value));
                break;

            case TypeQuestionEvaluation.SINGLE_CHOICE:
                cell.setCellValue(formatSingleChoice(question, value));
                break;

            case TypeQuestionEvaluation.BOOLEAN_GROUP:
                cell.setCellValue(formatBoolean(value));
                break;

            case TypeQuestionEvaluation.AUTO:
                if(!qItems(code).isEmpty()) {
                    cell.setCellValue(formatSingleChoice(question, value));
                } else {
                    cell.setCellValue(formatDefaultValue(value));
                }
                break;
            case TypeQuestionEvaluation.TEXT:
                cell.setCellValue(value.toString());
                break;

            default:
                cell.setCellValue(formatDefaultValue(value));
                break;
        }
    }

    private String formatYesNo(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "Oui" : "Non";
        }
        if (value instanceof Integer) {
            return ((Integer) value == 1) ? "Oui" : "Non";
        }
        return "";
    }

    private String formatBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "Oui" : "Non";
        }
        if (value instanceof Integer) {
            return ((Integer) value == 1) ? "Oui" : "Non";
        }
        return "";
    }

    private String formatLikert5(Object value) {
        if (!(value instanceof Integer)) return "";
        int index = (Integer) value;
        if (index >= 0 && index < LIKERT_5.size()) {
            return LIKERT_5.get(index);
        }
        return "";
    }

    private String formatAgreement5(Object value) {
        if (!(value instanceof Integer)) return "";
        int index = (Integer) value;
        if (index >= 0 && index < AGREEMENT_5.size()) {
            return AGREEMENT_5.get(index);
        }
        return "";
    }

    private String formatSingleChoice(QuestionEvaluation question, Object value) {
        if (!(value instanceof Integer)) return "";

        List<String> items = qItems(question.getCode());
        int index = (Integer) value;

        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return "";
    }

    private String formatDefaultValue(Object value) {
        if (value instanceof Integer) {
            return String.valueOf(value);
        } else if (value instanceof Boolean) {
            return (Boolean) value ? "Oui" : "Non";
        } else {
            return value.toString();
        }
    }

    private String formatSpecialCaseValue(String code, Object value) {
        // Gestion des cas spéciaux pour ETUI7 et ETUII5
        switch (code) {
            case "ETUI7_bis1":
                return formatEtuI7Bis1(value);
            case "ETUI7_bis2":
                return formatEtuI7Bis2(value);
            case "ETUII5a":
                return formatEtuII5a(value);
            case "ETUII5b":
                return formatEtuII5b(value);
            default:
                return formatDefaultValue(value);
        }
    }

    private String formatEtuI7Bis1(Object value) {
        // ETUI7 bis1 : "Si oui, par qui ?"
        // Les options sont : ["Proposé par votre tuteur professionnel", "Proposé par votre tuteur enseignant",
        //                     "Élaboré par vous-même", "Négocié entre les parties", "Autre"]
        if (!(value instanceof Integer)) return value != null ? value.toString() : "";

        int index = (Integer) value;
        List<String> options = List.of(
                "Proposé par votre tuteur professionnel",
                "Proposé par votre tuteur enseignant",
                "Élaboré par vous-même",
                "Négocié entre les parties",
                "Autre"
        );

        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return "";
    }

    private String formatEtuI7Bis2(Object value) {
        // ETUI7 bis2 : "Si non, pourquoi ?"
        // Les options sont : ["Je n'ai pas eu besoin d'aide", "Je ne savais pas à qui m'adresser"]
        if (!(value instanceof Integer)) return value != null ? value.toString() : "";

        int index = (Integer) value;
        List<String> options = List.of(
                "Je n'ai pas eu besoin d'aide",
                "Je ne savais pas à qui m'adresser"
        );

        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        return "";
    }

    private String formatEtuII5a(Object value) {
        // ETUII5a : "De quel ordre ?"
        // C'est généralement un texte libre
        return value != null ? value.toString() : "";
    }

    private String formatEtuII5b(Object value) {
        // ETUII5b : "Avec autonomie ?"
        // C'est un YES_NO
        return formatYesNo(value);
    }

    private void createCell(Row row, int colNum ,Object value, CellStyle style) {
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
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    private Object getReponseSupplementaireValue(ReponseSupplementaire repSup) {
        if (repSup.getReponseBool() != null) {
            return repSup.getReponseBool();
        } else if (repSup.getReponseInt() != null) {
            return formatLikert5(repSup.getReponseInt());
        } else if (repSup.getReponseTxt() != null) {
            return repSup.getReponseTxt();
        }
        return "";
    }

    private void addQuestionSupplementaireHeaders(List<String> headers, List<EvaluationDto> evaluations, ExportType type) {
        if (evaluations.isEmpty()) return;

        for (EvaluationDto eval : evaluations) {
            if (eval.getQuestionSupplementaires() != null && !eval.getQuestionSupplementaires().isEmpty()) {
                eval.getQuestionSupplementaires().stream()
                        .filter(q -> isQuestionForType(q.getIdPlacement(), type))
                        .sorted(Comparator.comparingInt(QuestionSupplementaire::getIdPlacement))
                        .forEach(q -> headers.add(q.getQuestion())); // libellé QS en clair
                break;
            }
        }
    }

    private boolean isQuestionForType(Integer idPlacement, ExportType type) {
        if (idPlacement == null) return false;

        return switch (type) {
            case ETUDIANT -> idPlacement >= 0 && idPlacement <= 2;
            case ENSEIGNANT -> idPlacement >= 3 && idPlacement <= 4;
            case ENTREPRISE -> idPlacement >= 5 && idPlacement <= 7;
            case ALL_IN_ONE -> true;
            default -> false;
        };
    }
}
