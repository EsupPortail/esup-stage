package org.esup_portail.esup_stage.service.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.dto.EvaluationDto;
import org.esup_portail.esup_stage.enums.ExportType;
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
    private QuestionEvaluation q(String code) {
        if (code == null) return null;
        String canon = canonicalCode(code);
        return questionsByCode.get(canon);
    }

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
        headers.add(qText("ETUI1", "I.1") + " - Réponse");
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
        headers.add(qText("ETUI7", "I.7") + " - Réseau personnel");
        headers.add(qText("ETUI7", "I.7") + " - Au sein de la formation");
        headers.add(qText("ETUI7", "I.7") + " - Si non, pourquoi ?");

        // I.8
        headers.add(qText("ETUI8", "I.8"));

        // II.1 (pas de code dédié - fallback manuel)
        headers.add("Quand ces modalités vous ont-elles été présentées ? - Réponse");
        headers.add("Quand ces modalités vous ont-elles été présentées ? - Commentaire");

        // II.2, II.3
        headers.add(qText("ETUII1", "II.2") + " - Réponse");
        headers.add(bisText("ETUII1", "Commentaire"));
        headers.add(qText("ETUII2", "II.3") + " - Réponse");
        headers.add(bisText("ETUII2", "Commentaire"));

        // II.4 + commentaire ✅ CORRECTION ICI
        headers.add(qText("ETUII3", "II.4"));
        headers.add(bisText("ETUII3", "Commentaire"));  // ✅ Ajouté

        // II.5 + précision ✅ CORRECTION ICI
        headers.add(qText("ETUII4", "II.5"));
        headers.add(qText("ETUII4", "II.5") + " - Précision");  // ✅ C'est bien ETUII4bis

        // II.6 ✅ CORRECTION ICI
        headers.add(qText("ETUII5", "II.6"));

        // III.1 (dimension internationale - ETUII6)
        headers.add(qText("ETUII6", "III.1"));
        headers.add(qText("ETUII6", "III.1") + " - Commentaire");

        // III.2
        headers.add(qText("ETUIII1", "III.2"));
        headers.add(bisText("ETUIII1", "Commentaire"));

        // III.3
        headers.add(qText("ETUIII2", "III.3"));
        headers.add(bisText("ETUIII2", "Commentaire"));

        // III.3 bis
        headers.add(qText("ETUIII3", "III.3 bis"));
        headers.add(bisText("ETUIII3", "Commentaire"));

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
        headers.add(qText("ETUIII6", "III.6") + " - Réponse");
        headers.add(bisText("ETUIII6", "Commentaire"));
        headers.add(qText("ETUIII7", "III.7") + " - Réponse");
        headers.add(bisText("ETUIII7", "Commentaire"));
        headers.add(qText("ETUIII8", "III.8"));
        headers.add(bisText("ETUIII8", "Commentaire"));
        headers.add(qText("ETUIII9", "III.9"));
        headers.add(bisText("ETUIII9", "Commentaire"));

        // III.10 à III.14
        headers.add(qText("ETUIII10", "III.10"));
        headers.add(qText("ETUIII11", "III.11"));
        headers.add(qText("ETUIII12", "III.12"));
        headers.add(qText("ETUIII14", "III.14"));  // ✅ Pas de III.13 dans la BD

        // III.15, III.16
        headers.add(qText("ETUIII15", "III.15") + " - Réponse");
        headers.add(bisText("ETUIII15", "Commentaire"));
        headers.add(qText("ETUIII16", "III.16") + " - Réponse");
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
        headers.add(qText("ENSII2", "II.2"));
        headers.add(qText("ENSII3", "II.3"));
        headers.add(bisText("ENSII3","II.3 bis"));
        headers.add(qText("ENSII4", "II.4"));
        headers.add(qText("ENSII5", "II.5"));
        headers.add(qText("ENSII6", "II.6"));
        headers.add(qText("ENSII7", "II.7"));
        headers.add(qText("ENSII8", "II.8"));
        headers.add(qText("ENSII9", "II.9"));
        headers.add(qText("ENSII10", "II.10"));
        headers.add(qText("ENSII11", "II.11") + " - Commentaire");
    }

    private void addEntrepriseHeaders(List<String> headers) {
        // I. savoir-être (ENT1..ENT14) avec commentaires éventuels
        headers.add(qText("ENT1", "Ent 1") + " - Réponse");
        headers.add(bisText("ENT1", "Commentaire"));
        headers.add(qText("ENT2", "Ent 2") + " - Réponse");
        headers.add(bisText("ENT2", "Commentaire"));
        headers.add(qText("ENT3", "Ent 3"));
        headers.add(qText("ENT5", "Ent 4") + " - Réponse");
        headers.add(bisText("ENT5", "Commentaire"));
        headers.add(qText("ENT9", "Ent 5") + " - Réponse");
        headers.add(bisText("ENT9", "Commentaire"));
        headers.add(qText("ENT11", "Ent 6") + " - Réponse");
        headers.add(bisText("ENT11", "Commentaire"));
        headers.add(qText("ENT12", "Ent 7") + " - Réponse");
        headers.add(bisText("ENT12", "Commentaire"));
        headers.add(qText("ENT13", "Ent 8") + " - Réponse");
        headers.add(bisText("ENT13", "Commentaire"));
        headers.add(qText("ENT14", "Ent 9") + " - Réponse");
        headers.add(bisText("ENT14", "Commentaire"));

        // II. savoir-faire (ENT4, ENT6..ENT8, ENT15)
        headers.add(qText("ENT4", "Ent 10"));
        headers.add(bisText("ENT4", "Commentaire"));
        headers.add(qText("ENT6", "Ent 11") + " - Réponse");
        headers.add(bisText("ENT6", "Commentaire"));
        headers.add(qText("ENT7", "Ent 12") + " - Réponse");
        headers.add(bisText("ENT7", "Commentaire"));
        headers.add(qText("ENT8", "Ent 13") + " - Réponse");
        headers.add(bisText("ENT8", "Commentaire"));
        headers.add(qText("ENT15", "Ent 14") + " - Réponse");
        headers.add(bisText("ENT15", "Commentaire"));

        // III. appréciation générale (ENT16, ENT17, ENT19, ENT18)
        headers.add(qText("ENT16", "Ent 15") + " - Réponse");
        headers.add(bisText("ENT16", "Commentaire"));
        headers.add(qText("ENT17", "Ent 16") + " - Réponse");
        headers.add(bisText("ENT17", "Commentaire"));
        headers.add(qText("ENT19", "Ent 17") + " - Commentaire");
        headers.add(qText("ENT18", "Ent 18"));
        headers.add(bisText("ENT18", "Commentaire"));
        // Le dernier “Ent 19 - Commentaire” était déjà couvert par ENT19 ci-dessus.
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
        // I.1 à I.4
        createCellForCode(row, colNum++, "ETUI1",  r.getReponseEtuI1(), style);
        createCell(row,        colNum++,  r.getReponseEtuI1bis(), style);
        createCellForCode(row, colNum++, "ETUI2",  r.getReponseEtuI2(), style);
        createCellForCode(row, colNum++, "ETUI3",  r.getReponseEtuI3(), style);
        createCellForCode(row, colNum++, "ETUI4a", r.getReponseEtuI4a(), style);
        createCellForCode(row, colNum++, "ETUI4b", r.getReponseEtuI4b(), style);
        createCellForCode(row, colNum++, "ETUI4c", r.getReponseEtuI4c(), style);
        createCellForCode(row, colNum++, "ETUI4d", r.getReponseEtuI4d(), style);

        // I.5, I.6
        createCellForCode(row, colNum++, "ETUI5",  r.getReponseEtuI5(), style);
        createCellForCode(row, colNum++, "ETUI6",  r.getReponseEtuI6(), style);

        // I.7 + sous-questions
        createCellForCode(row, colNum++, "ETUI7",      r.getReponseEtuI7(), style);
        createCellForCode(row, colNum++, "ETUI7_bis1", r.getReponseEtuI7bis1(), style);
        createCell(row,        colNum++,    r.getReponseEtuI7bis1a(), style);
        createCell(row,        colNum++,    r.getReponseEtuI7bis1b(), style);
        createCellForCode(row, colNum++, "ETUI7_bis2", r.getReponseEtuI7bis2(), style);

        // I.8
        createCellForCode(row, colNum++, "ETUI8", r.getReponseEtuI8(), style);

        // II.1 (pas de code - données supposées nulles ou manuelles)
        createCell(row, colNum++, "", style);  // Réponse vide
        createCell(row, colNum++, "", style);  // Commentaire vide

        // II.2, II.3
        createCellForCode(row, colNum++, "ETUII1",  r.getReponseEtuII1(), style);
        createCell(row,        colNum++, r.getReponseEtuII1bis(), style);
        createCellForCode(row, colNum++, "ETUII2",  r.getReponseEtuII2(), style);
        createCell(row,        colNum++, r.getReponseEtuII2bis(), style);

        // II.4 + commentaire ✅ CORRECTION
        createCellForCode(row, colNum++, "ETUII3",  r.getReponseEtuII3(), style);
        createCell(row,        colNum++, r.getReponseEtuII3bis(), style);  // ✅ Ajouté

        // II.5 + précision ✅ CORRECTION
        createCellForCode(row, colNum++, "ETUII4",  r.getReponseEtuII4(), style);
        createCell(row,        colNum++, null, style);  //

        // II.6 ✅ CORRECTION
        createCellForCode(row, colNum++, "ETUII5",  r.getReponseEtuII5(), style);  // ✅ Maintenant correctement placé

        // III.1
        createCellForCode(row, colNum++, "ETUII6",  r.getReponseEtuII6(), style);
        createCell(row,        colNum++, "", style);  // Commentaire si disponible (non présent dans données)

        // III.2 à III.16 (inchangé)
        createCellForCode(row, colNum++, "ETUIII1",   r.getReponseEtuIII1(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII1bis(), style);
        createCellForCode(row, colNum++, "ETUIII2",   r.getReponseEtuIII2(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII2bis(), style);
        createCellForCode(row, colNum++, "ETUIII3",   r.getReponseEtuIII3(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII3bis(), style);
        createCellForCode(row, colNum++, "ETUIII4",   r.getReponseEtuIII4(), style);
        createCellForCode(row, colNum++, "ETUIII5a",  r.getReponseEtuIII5a(), style);
        createCellForCode(row, colNum++, "ETUIII5b",  r.getReponseEtuIII5b(), style);
        createCellForCode(row, colNum++, "ETUIII5c",  r.getReponseEtuIII5c(), style);
        createCell(row,        colNum++, r.getReponseEtuIII5bis(), style);
        createCellForCode(row, colNum++, "ETUIII6",   r.getReponseEtuIII6(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII6bis(), style);
        createCellForCode(row, colNum++, "ETUIII7",   r.getReponseEtuIII7(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII7bis(), style);
        createCellForCode(row, colNum++, "ETUIII8",   r.getReponseEtuIII8(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII8bis(), style);
        createCellForCode(row, colNum++, "ETUIII9",   r.getReponseEtuIII9(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII9bis(), style);
        createCellForCode(row, colNum++, "ETUIII10",  r.getReponseEtuIII10(), style);
        createCellForCode(row, colNum++, "ETUIII11",  r.getReponseEtuIII11(), style);
        createCellForCode(row, colNum++, "ETUIII12",  r.getReponseEtuIII12(), style);
        // ✅ Pas de ETUIII13
        createCellForCode(row, colNum++, "ETUIII14",  r.getReponseEtuIII14(), style);
        createCellForCode(row, colNum++, "ETUIII15",  r.getReponseEtuIII15(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII15bis(), style);
        createCellForCode(row, colNum++, "ETUIII16",  r.getReponseEtuIII16(), style);
        createCell(row,        colNum++,   r.getReponseEtuIII16bis(), style);

        return colNum;
    }

    private int fillEnseignantData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // I.1, I.2, I.3
        createCellForCode(row, colNum++, "ENSI1a", r.getReponseEnsI1a(), style);
        createCellForCode(row, colNum++, "ENSI1b", r.getReponseEnsI1b(), style);
        createCellForCode(row, colNum++, "ENSI1c", r.getReponseEnsI1c(), style);
        createCellForCode(row, colNum++, "ENSI2a", r.getReponseEnsI2a(), style);
        createCellForCode(row, colNum++, "ENSI2b", r.getReponseEnsI2b(), style);
        createCellForCode(row, colNum++, "ENSI2c", r.getReponseEnsI2c(), style);
        createCell(row,        colNum++,  r.getReponseEnsI3(), style);

        // II.1 à II.11
        createCellForCode(row, colNum++, "ENSII1",  r.getReponseEnsII1(), style);
        createCellForCode(row, colNum++, "ENSII2",  r.getReponseEnsII2(), style);
        createCellForCode(row, colNum++, "ENSII3",  r.getReponseEnsII3(), style);
        createCell(row,        colNum++, "", style);  // ✅ ENSII3bis ajouté (données non disponibles -> vide)
        createCellForCode(row, colNum++, "ENSII4",  r.getReponseEnsII4(), style);
        createCellForCode(row, colNum++, "ENSII5",  r.getReponseEnsII5(), style);
        createCellForCode(row, colNum++, "ENSII6",  r.getReponseEnsII6(), style);
        createCellForCode(row, colNum++, "ENSII7",  r.getReponseEnsII7(), style);
        createCellForCode(row, colNum++, "ENSII8",  r.getReponseEnsII8(), style);
        createCellForCode(row, colNum++, "ENSII9",  r.getReponseEnsII9(), style);
        createCellForCode(row, colNum++, "ENSII10", r.getReponseEnsII10(), style);
        createCellForCode(row, colNum++, "ENSII11", r.getReponseEnsII11(), style);

        return colNum;
    }

    private int fillEntrepriseData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        // I. Savoir-être (ENT1-2-3-5-9-11-12-13-14)
        createCellForCode(row, colNum++, "ENT1",  r.getReponseEnt1(), style);
        createCell(row,        colNum++, r.getReponseEnt1bis(), style);
        createCellForCode(row, colNum++, "ENT2",  r.getReponseEnt2(), style);
        createCell(row,        colNum++, r.getReponseEnt2bis(), style);
        createCellForCode(row, colNum++, "ENT3",  r.getReponseEnt3(), style);
        createCellForCode(row, colNum++, "ENT5",  r.getReponseEnt5(), style);
        createCell(row,        colNum++, r.getReponseEnt5bis(), style);
        createCellForCode(row, colNum++, "ENT9",  r.getReponseEnt9(), style);
        createCell(row,        colNum++, r.getReponseEnt9bis(), style);
        createCellForCode(row, colNum++, "ENT11", r.getReponseEnt11(), style);
        createCell(row,        colNum++, r.getReponseEnt11bis(), style);
        createCellForCode(row, colNum++, "ENT12", r.getReponseEnt12(), style);
        createCell(row,        colNum++, r.getReponseEnt12bis(), style);
        createCellForCode(row, colNum++, "ENT13", r.getReponseEnt13(), style);
        createCell(row,        colNum++, r.getReponseEnt13bis(), style);
        createCellForCode(row, colNum++, "ENT14", r.getReponseEnt14(), style);
        createCell(row,        colNum++, r.getReponseEnt14bis(), style);

        // II. Savoir-faire (ENT4-6-7-8-15) ✅ ENT10 RETIRÉ D'ICI
        createCellForCode(row, colNum++, "ENT4",  r.getReponseEnt4(), style);
        createCell(row,        colNum++, r.getReponseEnt4bis(), style);
        createCellForCode(row, colNum++, "ENT6",  r.getReponseEnt6(), style);
        createCell(row,        colNum++, r.getReponseEnt6bis(), style);
        createCellForCode(row, colNum++, "ENT7",  r.getReponseEnt7(), style);
        createCell(row,        colNum++, r.getReponseEnt7bis(), style);
        createCellForCode(row, colNum++, "ENT8",  r.getReponseEnt8(), style);
        createCell(row,        colNum++, r.getReponseEnt8bis(), style);
        createCellForCode(row, colNum++, "ENT15", r.getReponseEnt15(), style);
        createCell(row,        colNum++, r.getReponseEnt15bis(), style);

        // III. Appréciation générale (ENT16-17-19-18) ✅ ENT10 SUPPRIMÉ
        createCellForCode(row, colNum++, "ENT16", r.getReponseEnt16(), style);
        createCell(row,        colNum++, r.getReponseEnt16bis(), style);
        createCellForCode(row, colNum++, "ENT17", r.getReponseEnt17(), style);
        createCell(row,        colNum++, r.getReponseEnt17bis(), style);
        createCell(row,        colNum++, r.getReponseEnt19(), style);
        createCellForCode(row, colNum++, "ENT18", r.getReponseEnt18(), style);
        createCell(row,        colNum++, r.getReponseEnt18bis(), style);

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
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }

    private Object getReponseSupplementaireValue(ReponseSupplementaire repSup) {
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

    private String qType(String code) {
        QuestionEvaluation qe = q(code);
        return (qe != null && qe.getType() != null) ? qe.getType().name() : null;
    }

    // 3) Options depuis paramsJson.items
    private List<String> qOptions(String code) {
        return qItems(canonicalCode(code)); // tu as déjà qItems(paramsJson)
    }

    private List<String> defaultScaleFor(String code) {
        String t = qType(code);
        if ("SCALE_AGREEMENT_5".equals(t)) return AGREEMENT_5;
        return LIKERT_5;
    }

    private Object mapIndexedAnswer(String code, Object value) {
        if (!(value instanceof Number)) return value;
        int idx = ((Number) value).intValue();

        // YES_NO/BOOLEAN_GROUP : on force Oui/Non, même si c'est 0/1
        String t = qType(code);
        if ("YES_NO".equals(t) || "BOOLEAN_GROUP".equals(t)) {
            return mapBooleanLike(idx);
        }

        // sinon, options DB si dispo
        List<String> opts = qOptions(code);
        if (!opts.isEmpty() && idx >= 0 && idx < opts.size()) return opts.get(idx);

        // sinon, échelle par défaut Likert/Accord selon type DB
        List<String> table = defaultScaleFor(code);
        if (idx >= 0 && idx < table.size()) return table.get(idx);

        // fallback: garde la valeur brute
        return idx;
    }

    private Object mapBoolean(Object value) {
        if (value instanceof Boolean b) return b ? "Oui" : "Non";
        return value;
    }

    private Object mapBooleanLike(Object value) {
        // Accepte boolean / int / string
        if (value instanceof Boolean b) return b ? "Oui" : "Non";
        if (value instanceof Number n)  return n.intValue() == 1 ? "Oui" : "Non";
        String s = String.valueOf(value).trim().toLowerCase();
        if (s.equals("1") || s.equals("true") || s.equals("oui"))  return "Oui";
        if (s.equals("0") || s.equals("false") || s.equals("non")) return "Non";
        return value; // fallback
    }

    private void createCellForCode(Row row, int colNum, String code, Object rawValue, CellStyle style) {
        Object val = rawValue;

        // 1) Si type DB YES_NO / BOOLEAN_GROUP -> tout en Oui/Non (supporte 0/1/true/false)
        String t = qType(code);
        if ("YES_NO".equals(t) || "BOOLEAN_GROUP".equals(t)) {
            val = mapBooleanLike(val);
        } else {
            // 2) Sinon, si nombre, mappe index -> libellé (options DB sinon table par défaut)
            if (val instanceof Number) {
                val = mapIndexedAnswer(code, val);
            } else if (val instanceof Boolean) {
                // cas booleen isolé
                val = ((Boolean) val) ? "Oui" : "Non";
            }
        }

        createCell(row, colNum, val, style);
    }

    private String canonicalCode(String code) {
        if (code == null) return null;
        // supprime suffixes a..h (BOOLEAN_GROUP) ex: ETUI4a -> ETUI4
        String c = code.replaceFirst("([A-Z]+\\d{1,3})[a-h]$", "$1");
        // supprime suffixes ...bis, ...bis1, ...bis2
        c = c.replaceFirst("(.*)bis\\d*$", "$1");
        // nettoie éventuels underscores qu'on aurait introduits (si tu en as mis côté front)
        c = c.replace("_", "");
        return c;
    }

}
