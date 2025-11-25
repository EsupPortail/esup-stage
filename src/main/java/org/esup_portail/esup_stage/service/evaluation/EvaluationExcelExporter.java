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

    // Nouvelles constantes pour les codes de colonnes communes
    private static final String COL_ID_CONVENTION = "ID_CONVENTION";
    private static final String COL_NOM_ETUDIANT = "NOM_ETUDIANT";
    private static final String COL_PRENOM_ETUDIANT = "PRENOM_ETUDIANT";
    private static final String COL_STRUCTURE = "STRUCTURE";
    private static final String COL_DATE_DEBUT = "DATE_DEBUT_STAGE";
    private static final String COL_DATE_FIN = "DATE_FIN_STAGE";
    private static final String COL_CENTRE_GESTION = "CENTRE_GESTION";
    private static final String COL_ETAPE = "ETAPE";
    private static final String COL_ANNEE_UNIV = "ANNEE_UNIVERSITAIRE";

    public byte[] export(List<EvaluationDto> evaluations, ExportType type) {
        return export(evaluations, type, null);
    }

    public byte[] export(List<EvaluationDto> evaluations, ExportType type, List<String> columnFilter) {
        loadQuestionsCache();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if (type == ExportType.ALL_IN_ONE) {
                // Filtrer les colonnes par type pour ALL_IN_ONE
                List<String> etudiantColumns = filterColumnsByPrefix(columnFilter, "ETU_", "COMMON_");
                List<String> enseignantColumns = filterColumnsByPrefix(columnFilter, "ENS_", "COMMON_");
                List<String> entrepriseColumns = filterColumnsByPrefix(columnFilter, "ENT_", "COMMON_");

                createSheet(workbook, evaluations, ExportType.ETUDIANT, "Évaluations Étudiant", etudiantColumns);
                createSheet(workbook, evaluations, ExportType.ENSEIGNANT, "Évaluations Enseignant", enseignantColumns);
                createSheet(workbook, evaluations, ExportType.ENTREPRISE, "Évaluations Entreprise", entrepriseColumns);
            } else {
                // Pour un export simple, on retire les préfixes
                List<String> cleanedColumns = columnFilter != null ?
                        columnFilter.stream()
                                .map(this::removePrefix)
                                .toList() : null;
                createSheet(workbook, evaluations, type, "Évaluations", cleanedColumns);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la création du fichier Excel", e);
        }
    }

    /**
     * Filtre les colonnes selon les préfixes autorisés
     * @param columnFilter Liste complète des colonnes avec préfixes
     * @param allowedPrefixes Préfixes autorisés pour cette feuille
     * @return Liste des colonnes filtrées et sans préfixes
     */
    private List<String> filterColumnsByPrefix(List<String> columnFilter, String... allowedPrefixes) {
        if (columnFilter == null || columnFilter.isEmpty()) {
            return null; // Pas de filtre = toutes les colonnes
        }

        return columnFilter.stream()
                .filter(col -> {
                    for (String prefix : allowedPrefixes) {
                        if (col.startsWith(prefix)) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(this::removePrefix)
                .toList();
    }

    /**
     * Retire le préfixe d'une colonne (ETU_, ENS_, ENT_, COMMON_)
     */
    private String removePrefix(String column) {
        if (column.startsWith("ETU_")) {
            return column.substring(4);
        } else if (column.startsWith("ENS_")) {
            return column.substring(4);
        } else if (column.startsWith("ENT_")) {
            return column.substring(4);
        } else if (column.startsWith("COMMON_")) {
            return column.substring(7);
        }
        return column;
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

    private void createSheet(Workbook workbook, List<EvaluationDto> evaluations, ExportType type, String sheetName, List<String> columnFilter) {
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        // Récupérer toutes les colonnes puis filtrer si nécessaire
        Map<String, String> allColumnsMap = getAllColumnsMap(evaluations, type);
        LinkedHashMap<String, String> filteredColumns = filterColumns(allColumnsMap, columnFilter);

        List<String> headers = new ArrayList<>(filteredColumns.values());
        createHeaderRow(sheet, headers, headerStyle);

        int rowNum = 1;
        for (EvaluationDto eval : evaluations) {
            if (shouldIncludeEvaluation(eval, type)) {
                Row row = sheet.createRow(rowNum++);
                fillDataRow(row, eval, type, dataStyle, new ArrayList<>(filteredColumns.keySet()));
            }
        }

        autoSizeColumns(sheet, headers.size());
    }

    /**
     * Filtre les colonnes selon la liste fournie
     * @param allColumns Map complète code -> label
     * @param columnFilter Liste des codes à conserver (null = toutes les colonnes)
     * @return Map filtrée et ordonnée selon columnFilter
     */
    private LinkedHashMap<String, String> filterColumns(Map<String, String> allColumns, List<String> columnFilter) {
        if (columnFilter == null || columnFilter.isEmpty()) {
            return new LinkedHashMap<>(allColumns);
        }

        LinkedHashMap<String, String> filtered = new LinkedHashMap<>();
        for (String code : columnFilter) {
            if (allColumns.containsKey(code)) {
                filtered.put(code, allColumns.get(code));
            }
        }
        return filtered;
    }

    /**
     * Construit la map complète code -> label pour toutes les colonnes
     */
    private Map<String, String> getAllColumnsMap(List<EvaluationDto> evaluations, ExportType type) {
        LinkedHashMap<String, String> columns = new LinkedHashMap<>();

        // Colonnes communes
        columns.put(COL_ID_CONVENTION, "ID Convention");
        columns.put(COL_NOM_ETUDIANT, "Nom Étudiant");
        columns.put(COL_PRENOM_ETUDIANT, "Prénom Étudiant");
        columns.put(COL_STRUCTURE, "Structure");
        columns.put(COL_DATE_DEBUT, "Date Début Stage");
        columns.put(COL_DATE_FIN, "Date Fin Stage");
        columns.put(COL_CENTRE_GESTION, "Centre de Gestion");
        columns.put(COL_ETAPE, "Étape");
        columns.put(COL_ANNEE_UNIV, "Année Universitaire");

        // Colonnes spécifiques par type
        switch (type) {
            case ETUDIANT -> addEtudiantColumnsMap(columns);
            case ENSEIGNANT -> addEnseignantColumnsMap(columns);
            case ENTREPRISE -> addEntrepriseColumnsMap(columns);
        }

        // Questions supplémentaires
        addQuestionSupplementaireColumnsMap(columns, evaluations, type);

        return columns;
    }

    private void addEtudiantColumnsMap(Map<String, String> columns) {
        columns.put("ETUI1", qText("ETUI1", "I.1"));
        columns.put("ETUI1bis", bisText("ETUI1", "Commentaire"));
        columns.put("ETUI2", qText("ETUI2", "I.2"));
        columns.put("ETUI3", qText("ETUI3", "I.3"));

        List<String> etuI4Items = qItems("ETUI4");
        String etuI4Label = qText("ETUI4", "I.4");
        columns.put("ETUI4a", etuI4Label + " - " + (etuI4Items.size() > 0 ? etuI4Items.get(0) : "a"));
        columns.put("ETUI4b", etuI4Label + " - " + (etuI4Items.size() > 1 ? etuI4Items.get(1) : "b"));
        columns.put("ETUI4c", etuI4Label + " - " + (etuI4Items.size() > 2 ? etuI4Items.get(2) : "c"));
        columns.put("ETUI4d", etuI4Label + " - " + (etuI4Items.size() > 3 ? etuI4Items.get(3) : "d"));

        columns.put("ETUI5", qText("ETUI5", "I.5"));
        columns.put("ETUI6", qText("ETUI6", "I.6"));
        columns.put("ETUI7", qText("ETUI7", "I.7"));
        columns.put("ETUI7_bis1", qText("ETUI7", "I.7") + " - Si oui, par qui ?");
        columns.put("ETUI7_bis2", qText("ETUI7", "I.7") + " - Si non, pourquoi ?");
        columns.put("ETUI8", qText("ETUI8", "I.8"));

        columns.put("ETUII1", qText("ETUII1", "II.1"));
        columns.put("ETUII1bis", bisText("ETUII1", "Commentaire"));
        columns.put("ETUII2", qText("ETUII2", "II.2"));
        columns.put("ETUII2bis", bisText("ETUII2", "Commentaire"));
        columns.put("ETUII3", qText("ETUII3", "II.3"));
        columns.put("ETUII3bis", bisText("ETUII3", "Commentaire"));
        columns.put("ETUII4", qText("ETUII4", "II.4"));
        columns.put("ETUII5", qText("ETUII5", "II.5"));
        columns.put("ETUII5a", qText("ETUII5", "II.5") + " - De quel ordre ?");
        columns.put("ETUII5b", qText("ETUII5", "II.5") + " - Avec autonomie ?");
        columns.put("ETUII6", qText("ETUII6", "II.6"));

        columns.put("ETUIII1", qText("ETUIII1", "III.1"));
        columns.put("ETUIII1bis", bisText("ETUIII1", "Commentaire"));
        columns.put("ETUIII2", qText("ETUIII2", "III.2"));
        columns.put("ETUIII2bis", bisText("ETUIII2", "Commentaire"));
        columns.put("ETUIII4", qText("ETUIII4", "III.4"));

        List<String> etuIII5Items = qItems("ETUIII5");
        String etuIII5Label = qText("ETUIII5", "III.5");
        columns.put("ETUIII5a", etuIII5Label + " - " + (etuIII5Items.size() > 0 ? etuIII5Items.get(0) : "a"));
        columns.put("ETUIII5b", etuIII5Label + " - " + (etuIII5Items.size() > 1 ? etuIII5Items.get(1) : "b"));
        columns.put("ETUIII5c", etuIII5Label + " - " + (etuIII5Items.size() > 2 ? etuIII5Items.get(2) : "c"));
        columns.put("ETUIII5bis", "Commentaire");

        columns.put("ETUIII6", qText("ETUIII6", "III.6"));
        columns.put("ETUIII6bis", bisText("ETUIII6", "Commentaire"));
        columns.put("ETUIII7", qText("ETUIII7", "III.7"));
        columns.put("ETUIII7bis", bisText("ETUIII7", "Commentaire"));
        columns.put("ETUIII8", qText("ETUIII8", "III.8"));
        columns.put("ETUIII8bis", bisText("ETUIII8", "Commentaire"));
        columns.put("ETUIII9", qText("ETUIII9", "III.9"));
        columns.put("ETUIII9bis", bisText("ETUIII9", "Commentaire"));
        columns.put("ETUIII10", qText("ETUIII10", "III.10"));
        columns.put("ETUIII11", qText("ETUIII11", "III.11"));
        columns.put("ETUIII12", qText("ETUIII12", "III.12"));
        columns.put("ETUIII14", qText("ETUIII14", "III.14"));
        columns.put("ETUIII15", qText("ETUIII15", "III.15"));
        columns.put("ETUIII15bis", bisText("ETUIII15", "Commentaire"));
        columns.put("ETUIII16", qText("ETUIII16", "III.16"));
        columns.put("ETUIII16bis", bisText("ETUIII16", "Commentaire"));
    }

    private void addEnseignantColumnsMap(Map<String, String> columns) {
        List<String> ensI1Items = qItems("ENSI1");
        String ensI1 = qText("ENSI1", "Suivi avec le stagiaire");
        columns.put("ENSI1a", ensI1 + " - " + (ensI1Items.size() > 0 ? ensI1Items.get(0) : "a"));
        columns.put("ENSI1b", ensI1 + " - " + (ensI1Items.size() > 1 ? ensI1Items.get(1) : "b"));
        columns.put("ENSI1c", ensI1 + " - " + (ensI1Items.size() > 2 ? ensI1Items.get(2) : "c"));

        List<String> ensI2Items = qItems("ENSI2");
        String ensI2 = qText("ENSI2", "Suivi avec le tuteur professionnel");
        columns.put("ENSI2a", ensI2 + " - " + (ensI2Items.size() > 0 ? ensI2Items.get(0) : "a"));
        columns.put("ENSI2b", ensI2 + " - " + (ensI2Items.size() > 1 ? ensI2Items.get(1) : "b"));
        columns.put("ENSI2c", ensI2 + " - " + (ensI2Items.size() > 2 ? ensI2Items.get(2) : "c"));

        columns.put("ENSI3", qText("ENSI3", "Commentaire(s)"));
        columns.put("ENSII1", qText("ENSII1", "II.1"));
        columns.put("ENSII10", qText("ENSII10", "II.10"));
        columns.put("ENSII11", qText("ENSII11", "II.11"));
        columns.put("ENSII2", qText("ENSII2", "II.2"));
        columns.put("ENSII3", qText("ENSII3", "II.3"));
        columns.put("ENSII4", qText("ENSII4", "II.4"));
        columns.put("ENSII5", qText("ENSII5", "II.5"));
        columns.put("ENSII6", qText("ENSII6", "II.6"));
        columns.put("ENSII7", qText("ENSII7", "II.7"));
        columns.put("ENSII8", qText("ENSII8", "II.8"));
        columns.put("ENSII9", qText("ENSII9", "II.9"));
    }

    private void addEntrepriseColumnsMap(Map<String, String> columns) {
        columns.put("ENT1", qText("ENT1", "Ent 1"));
        columns.put("ENT1bis", bisText("ENT1", "Commentaire"));
        columns.put("ENT2", qText("ENT2", "Ent 2"));
        columns.put("ENT2bis", bisText("ENT2", "Commentaire"));
        columns.put("ENT3", qText("ENT3", "Ent 3"));
        columns.put("ENT5", qText("ENT5", "Ent 4"));
        columns.put("ENT5bis", bisText("ENT5", "Commentaire"));
        columns.put("ENT9", qText("ENT9", "Ent 5"));
        columns.put("ENT9bis", bisText("ENT9", "Commentaire"));
        columns.put("ENT11", qText("ENT11", "Ent 6"));
        columns.put("ENT11bis", bisText("ENT11", "Commentaire"));
        columns.put("ENT12", qText("ENT12", "Ent 7"));
        columns.put("ENT12bis", bisText("ENT12", "Commentaire"));
        columns.put("ENT13", qText("ENT13", "Ent 8"));
        columns.put("ENT13bis", bisText("ENT13", "Commentaire"));
        columns.put("ENT14", qText("ENT14", "Ent 9"));
        columns.put("ENT14bis", bisText("ENT14", "Commentaire"));
        columns.put("ENT4", qText("ENT4", "Ent 10"));
        columns.put("ENT4bis", bisText("ENT4", "Commentaire"));
        columns.put("ENT6", qText("ENT6", "Ent 11"));
        columns.put("ENT6bis", bisText("ENT6", "Commentaire"));
        columns.put("ENT7", qText("ENT7", "Ent 12"));
        columns.put("ENT7bis", bisText("ENT7", "Commentaire"));
        columns.put("ENT8", qText("ENT8", "Ent 13"));
        columns.put("ENT8bis", bisText("ENT8", "Commentaire"));
        columns.put("ENT15", qText("ENT15", "Ent 14"));
        columns.put("ENT15bis", bisText("ENT15", "Commentaire"));
        columns.put("ENT16", qText("ENT16", "Ent 15"));
        columns.put("ENT16bis", bisText("ENT16", "Commentaire"));
        columns.put("ENT17", qText("ENT17", "Ent 16"));
        columns.put("ENT17bis", bisText("ENT17", "Commentaire"));
        columns.put("ENT19", qText("ENT19", "Ent 19"));
        columns.put("ENT10", qText("ENT10", "Ent 10"));
        columns.put("ENT18", qText("ENT18", "Ent 18"));
        columns.put("ENT18bis", bisText("ENT18", "Commentaire"));
    }

    private void addQuestionSupplementaireColumnsMap(Map<String, String> columns, List<EvaluationDto> evaluations, ExportType type) {
        if (evaluations.isEmpty()) return;

        for (EvaluationDto eval : evaluations) {
            if (eval.getQuestionSupplementaires() != null && !eval.getQuestionSupplementaires().isEmpty()) {
                eval.getQuestionSupplementaires().stream()
                        .filter(q -> isQuestionForType(q.getIdPlacement(), type))
                        .sorted(Comparator.comparingInt(QuestionSupplementaire::getIdPlacement))
                        .forEach(q -> columns.put("QS_" + q.getId(), q.getQuestion()));
                break;
            }
        }
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
        headers.add(qText("ETUI1", "I.1"));
        headers.add(bisText("ETUI1", "Commentaire"));
        headers.add(qText("ETUI2", "I.2"));
        headers.add(qText("ETUI3", "I.3"));

        List<String> etuI4Items = qItems("ETUI4");
        String etuI4Label = qText("ETUI4", "I.4");
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 0 ? etuI4Items.get(0) : "a"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 1 ? etuI4Items.get(1) : "b"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 2 ? etuI4Items.get(2) : "c"));
        headers.add(etuI4Label + " - " + (etuI4Items.size() > 3 ? etuI4Items.get(3) : "d"));

        headers.add(qText("ETUI5", "I.5"));
        headers.add(qText("ETUI6", "I.6"));
        headers.add(qText("ETUI7", "I.7"));
        headers.add(qText("ETUI7", "I.7") + " - Si oui, par qui ?");
        headers.add(qText("ETUI7", "I.7") + " - Si non, pourquoi ?");
        headers.add(qText("ETUI8", "I.8"));

        headers.add(qText("ETUII1", "II.1"));
        headers.add(bisText("ETUII1", "Commentaire"));
        headers.add(qText("ETUII2", "II.2"));
        headers.add(bisText("ETUII2", "Commentaire"));
        headers.add(qText("ETUII3", "II.3"));
        headers.add(bisText("ETUII3", "Commentaire"));
        headers.add(qText("ETUII4", "II.4"));
        headers.add(qText("ETUII5", "II.5"));
        headers.add(qText("ETUII5", "II.5") + " - De quel ordre ?");
        headers.add(qText("ETUII5", "II.5") + " - Avec autonomie ?");
        headers.add(qText("ETUII6", "II.6"));

        headers.add(qText("ETUIII1", "III.1"));
        headers.add(bisText("ETUIII1", "Commentaire"));
        headers.add(qText("ETUIII2", "III.2"));
        headers.add(bisText("ETUIII2", "Commentaire"));
        headers.add(qText("ETUIII4", "III.4"));

        List<String> etuIII5Items = qItems("ETUIII5");
        String etuIII5Label = qText("ETUIII5", "III.5");
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 0 ? etuIII5Items.get(0) : "a"));
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 1 ? etuIII5Items.get(1) : "b"));
        headers.add(etuIII5Label + " - " + (etuIII5Items.size() > 2 ? etuIII5Items.get(2) : "c"));
        headers.add(etuIII5Label + " - Commentaire");

        headers.add(qText("ETUIII6", "III.6"));
        headers.add(bisText("ETUIII6", "Commentaire"));
        headers.add(qText("ETUIII7", "III.7"));
        headers.add(bisText("ETUIII7", "Commentaire"));
        headers.add(qText("ETUIII8", "III.8"));
        headers.add(bisText("ETUIII8", "Commentaire"));
        headers.add(qText("ETUIII9", "III.9"));
        headers.add(bisText("ETUIII9", "Commentaire"));
        headers.add(qText("ETUIII10", "III.10"));
        headers.add(qText("ETUIII11", "III.11"));
        headers.add(qText("ETUIII12", "III.12"));
        headers.add(qText("ETUIII14", "III.14"));
        headers.add(qText("ETUIII15", "III.15"));
        headers.add(bisText("ETUIII15", "Commentaire"));
        headers.add(qText("ETUIII16", "III.16"));
        headers.add(bisText("ETUIII16", "Commentaire"));
    }

    private void addEnseignantHeaders(List<String> headers) {
        List<String> ensI1Items = qItems("ENSI1");
        String ensI1 = qText("ENSI1", "Suivi avec le stagiaire");
        headers.add(ensI1 + " - " + (ensI1Items.size() > 0 ? ensI1Items.get(0) : "a"));
        headers.add(ensI1 + " - " + (ensI1Items.size() > 1 ? ensI1Items.get(1) : "b"));
        headers.add(ensI1 + " - " + (ensI1Items.size() > 2 ? ensI1Items.get(2) : "c"));

        List<String> ensI2Items = qItems("ENSI2");
        String ensI2 = qText("ENSI2", "Suivi avec le tuteur professionnel");
        headers.add(ensI2 + " - " + (ensI2Items.size() > 0 ? ensI2Items.get(0) : "a"));
        headers.add(ensI2 + " - " + (ensI2Items.size() > 1 ? ensI2Items.get(1) : "b"));
        headers.add(ensI2 + " - " + (ensI2Items.size() > 2 ? ensI2Items.get(2) : "c"));

        headers.add(qText("ENSI3", "Commentaire(s)"));
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
        fillDataRow(row, eval, type, dataStyle, null);
    }

    private void fillDataRow(Row row, EvaluationDto eval, ExportType type, CellStyle dataStyle, List<String> columnCodes) {
        ReponseEvaluation reponse = eval.getReponseEvaluation();

        // Si pas de filtrage, utiliser l'ancienne méthode
        if (columnCodes == null || columnCodes.isEmpty()) {
            fillDataRowLegacy(row, eval, type, dataStyle);
            return;
        }

        // Remplissage avec filtrage
        int colNum = 0;
        for (String code : columnCodes) {
            Object value = getValueByCode(code, eval, reponse, type);

            if (code.startsWith("QS_")) {
                // Question supplémentaire
                createCell(row, colNum++, value, dataStyle);
            } else {
                // Question standard
                createCell(row, colNum++, code, value, dataStyle);
            }
        }
    }

    /**
     * Récupère la valeur d'une cellule selon son code
     */
    private Object getValueByCode(String code, EvaluationDto eval, ReponseEvaluation r, ExportType type) {
        // Colonnes communes
        switch (code) {
            case COL_ID_CONVENTION: return eval.getIdConvention();
            case COL_NOM_ETUDIANT: return eval.getEtudiant() != null ? eval.getEtudiant().getNom() : "";
            case COL_PRENOM_ETUDIANT: return eval.getEtudiant() != null ? eval.getEtudiant().getPrenom() : "";
            case COL_STRUCTURE: return eval.getStructure() != null ? eval.getStructure().getRaisonSociale() : "";
            case COL_DATE_DEBUT: return eval.getDateDebutStage() != null ? DATE_FORMAT.format(eval.getDateDebutStage()) : "";
            case COL_DATE_FIN: return eval.getDateFinStage() != null ? DATE_FORMAT.format(eval.getDateFinStage()) : "";
            case COL_CENTRE_GESTION: return eval.getCentreGestion() != null ? eval.getCentreGestion().getNomCentre() : "";
            case COL_ETAPE: return eval.getEtape() != null ? eval.getEtape().getLibelle() : "";
            case COL_ANNEE_UNIV: return eval.getAnneeUniversitaire();
        }

        // Questions supplémentaires
        if (code.startsWith("QS_")) {
            long qsId = Long.parseLong(code.substring(3));
            return getQuestionSupplementaireValue(eval, qsId);
        }

        // Questions spécifiques selon le type
        if (r == null) return "";

        switch (type) {
            case ETUDIANT: return getEtudiantValue(code, r);
            case ENSEIGNANT: return getEnseignantValue(code, r);
            case ENTREPRISE: return getEntrepriseValue(code, r);
            default: return "";
        }
    }

    private Object getEtudiantValue(String code, ReponseEvaluation r) {
        return switch (code) {
            case "ETUI1" -> r.getReponseEtuI1();
            case "ETUI1bis" -> r.getReponseEtuI1bis();
            case "ETUI2" -> r.getReponseEtuI2();
            case "ETUI3" -> r.getReponseEtuI3();
            case "ETUI4a" -> r.getReponseEtuI4a();
            case "ETUI4b" -> r.getReponseEtuI4b();
            case "ETUI4c" -> r.getReponseEtuI4c();
            case "ETUI4d" -> r.getReponseEtuI4d();
            case "ETUI5" -> r.getReponseEtuI5();
            case "ETUI6" -> r.getReponseEtuI6();
            case "ETUI7" -> r.getReponseEtuI7();
            case "ETUI7_bis1" -> r.getReponseEtuI7bis1();
            case "ETUI7_bis2" -> r.getReponseEtuI7bis2();
            case "ETUI8" -> r.getReponseEtuI8();
            case "ETUII1" -> r.getReponseEtuII1();
            case "ETUII1bis" -> r.getReponseEtuII1bis();
            case "ETUII2" -> r.getReponseEtuII2();
            case "ETUII2bis" -> r.getReponseEtuII2bis();
            case "ETUII3" -> r.getReponseEtuII3();
            case "ETUII3bis" -> r.getReponseEtuII3bis();
            case "ETUII4" -> r.getReponseEtuII4();
            case "ETUII5" -> r.getReponseEtuII5();
            case "ETUII5a" -> r.getReponseEtuII5a();
            case "ETUII5b" -> r.getReponseEtuII5b();
            case "ETUII6" -> r.getReponseEtuII6();
            case "ETUIII1" -> r.getReponseEtuIII1();
            case "ETUIII1bis" -> r.getReponseEtuIII1bis();
            case "ETUIII2" -> r.getReponseEtuIII2();
            case "ETUIII2bis" -> r.getReponseEtuIII2bis();
            case "ETUIII4" -> r.getReponseEtuIII4();
            case "ETUIII5a" -> r.getReponseEtuIII5a();
            case "ETUIII5b" -> r.getReponseEtuIII5b();
            case "ETUIII5c" -> r.getReponseEtuIII5c();
            case "ETUIII5bis" -> r.getReponseEtuIII5bis();
            case "ETUIII6" -> r.getReponseEtuIII6();
            case "ETUIII6bis" -> r.getReponseEtuIII6bis();
            case "ETUIII7" -> r.getReponseEtuIII7();
            case "ETUIII7bis" -> r.getReponseEtuIII7bis();
            case "ETUIII8" -> r.getReponseEtuIII8();
            case "ETUIII8bis" -> r.getReponseEtuIII8bis();
            case "ETUIII9" -> r.getReponseEtuIII9();
            case "ETUIII9bis" -> r.getReponseEtuIII9bis();
            case "ETUIII10" -> r.getReponseEtuIII10();
            case "ETUIII11" -> r.getReponseEtuIII11();
            case "ETUIII12" -> r.getReponseEtuIII12();
            case "ETUIII14" -> r.getReponseEtuIII14();
            case "ETUIII15" -> r.getReponseEtuIII15();
            case "ETUIII15bis" -> r.getReponseEtuIII15bis();
            case "ETUIII16" -> r.getReponseEtuIII16();
            case "ETUIII16bis" -> r.getReponseEtuIII16bis();
            default -> "";
        };
    }

    private Object getEnseignantValue(String code, ReponseEvaluation r) {
        return switch (code) {
            case "ENSI1a" -> r.getReponseEnsI1a();
            case "ENSI1b" -> r.getReponseEnsI1b();
            case "ENSI1c" -> r.getReponseEnsI1c();
            case "ENSI2a" -> r.getReponseEnsI2a();
            case "ENSI2b" -> r.getReponseEnsI2b();
            case "ENSI2c" -> r.getReponseEnsI2c();
            case "ENSI3" -> r.getReponseEnsI3();
            case "ENSII1" -> r.getReponseEnsII1();
            case "ENSII10" -> r.getReponseEnsII10();
            case "ENSII11" -> r.getReponseEnsII11();
            case "ENSII2" -> r.getReponseEnsII2();
            case "ENSII3" -> r.getReponseEnsII3();
            case "ENSII4" -> r.getReponseEnsII4();
            case "ENSII5" -> r.getReponseEnsII5();
            case "ENSII6" -> r.getReponseEnsII6();
            case "ENSII7" -> r.getReponseEnsII7();
            case "ENSII8" -> r.getReponseEnsII8();
            case "ENSII9" -> r.getReponseEnsII9();
            default -> "";
        };
    }

    private Object getEntrepriseValue(String code, ReponseEvaluation r) {
        return switch (code) {
            case "ENT1" -> r.getReponseEnt1();
            case "ENT1bis" -> r.getReponseEnt1bis();
            case "ENT2" -> r.getReponseEnt2();
            case "ENT2bis" -> r.getReponseEnt2bis();
            case "ENT3" -> r.getReponseEnt3();
            case "ENT4" -> r.getReponseEnt4();
            case "ENT4bis" -> r.getReponseEnt4bis();
            case "ENT5" -> r.getReponseEnt5();
            case "ENT5bis" -> r.getReponseEnt5bis();
            case "ENT6" -> r.getReponseEnt6();
            case "ENT6bis" -> r.getReponseEnt6bis();
            case "ENT7" -> r.getReponseEnt7();
            case "ENT7bis" -> r.getReponseEnt7bis();
            case "ENT8" -> r.getReponseEnt8();
            case "ENT8bis" -> r.getReponseEnt8bis();
            case "ENT9" -> r.getReponseEnt9();
            case "ENT9bis" -> r.getReponseEnt9bis();
            case "ENT10" -> r.getReponseEnt10();
            case "ENT11" -> r.getReponseEnt11();
            case "ENT11bis" -> r.getReponseEnt11bis();
            case "ENT12" -> r.getReponseEnt12();
            case "ENT12bis" -> r.getReponseEnt12bis();
            case "ENT13" -> r.getReponseEnt13();
            case "ENT13bis" -> r.getReponseEnt13bis();
            case "ENT14" -> r.getReponseEnt14();
            case "ENT14bis" -> r.getReponseEnt14bis();
            case "ENT15" -> r.getReponseEnt15();
            case "ENT15bis" -> r.getReponseEnt15bis();
            case "ENT16" -> r.getReponseEnt16();
            case "ENT16bis" -> r.getReponseEnt16bis();
            case "ENT17" -> r.getReponseEnt17();
            case "ENT17bis" -> r.getReponseEnt17bis();
            case "ENT18" -> r.getReponseEnt18();
            case "ENT18bis" -> r.getReponseEnt18bis();
            case "ENT19" -> r.getReponseEnt19();
            default -> "";
        };
    }

    private Object getQuestionSupplementaireValue(EvaluationDto eval, long questionId) {
        if (eval.getReponseSupplementaires() == null) return "";

        ReponseSupplementaire repSup = eval.getReponseSupplementaires().stream()
                .filter(r -> r.getQuestionSupplementaire() != null && r.getQuestionSupplementaire().getId() == questionId)
                .findFirst()
                .orElse(null);

        if (repSup != null) {
            return getReponseSupplementaireValue(repSup);
        }
        return "";
    }

    /**
     * Ancienne méthode de remplissage (sans filtrage) - conservée pour rétrocompatibilité
     */
    private void fillDataRowLegacy(Row row, EvaluationDto eval, ExportType type, CellStyle dataStyle) {
        int colNum = 0;

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

        switch (type) {
            case ETUDIANT -> colNum = fillEtudiantData(row, colNum, reponse, dataStyle);
            case ENSEIGNANT -> colNum = fillEnseignantData(row, colNum, reponse, dataStyle);
            case ENTREPRISE -> colNum = fillEntrepriseData(row, colNum, reponse, dataStyle);
        }

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
        createCell(row, colNum++, "ETUI1",  r.getReponseEtuI1(), style);
        createCell(row, colNum++, "ETUI1bis", r.getReponseEtuI1bis(), style);
        createCell(row, colNum++, "ETUI2",  r.getReponseEtuI2(), style);
        createCell(row, colNum++, "ETUI3",  r.getReponseEtuI3(), style);
        createCell(row, colNum++, "ETUI4a", r.getReponseEtuI4a(), style);
        createCell(row, colNum++, "ETUI4b", r.getReponseEtuI4b(), style);
        createCell(row, colNum++, "ETUI4c", r.getReponseEtuI4c(), style);
        createCell(row, colNum++, "ETUI4d", r.getReponseEtuI4d(), style);
        createCell(row, colNum++, "ETUI5",  r.getReponseEtuI5(), style);
        createCell(row, colNum++, "ETUI6",  r.getReponseEtuI6(), style);
        createCell(row, colNum++, "ETUI7",  r.getReponseEtuI7(), style);
        createCell(row, colNum++, "ETUI7_bis1", r.getReponseEtuI7bis1(), style);
        createCell(row, colNum++, "ETUI7_bis2", r.getReponseEtuI7bis2(), style);
        createCell(row, colNum++, "ETUI8", r.getReponseEtuI8(), style);
        createCell(row, colNum++, "ETUII1",  r.getReponseEtuII1(), style);
        createCell(row, colNum++,"ETUII1bis",  r.getReponseEtuII1bis(), style);
        createCell(row, colNum++, "ETUII2",  r.getReponseEtuII2(), style);
        createCell(row, colNum++,"ETUII2bis",  r.getReponseEtuII2bis(), style);
        createCell(row, colNum++, "ETUII3",  r.getReponseEtuII3(), style);
        createCell(row, colNum++,"ETUII3bis",  r.getReponseEtuII3bis(), style);
        createCell(row, colNum++, "ETUII4",  r.getReponseEtuII4(), style);
        createCell(row, colNum++, "ETUII5",  r.getReponseEtuII5(), style);
        createCell(row, colNum++,"ETUII5a",  r.getReponseEtuII5a(), style);
        createCell(row, colNum++, "ETUII5b",  r.getReponseEtuII5b(), style);
        createCell(row, colNum++, "ETUII6",  r.getReponseEtuII6(), style);
        createCell(row, colNum++, "ETUIII1",   r.getReponseEtuIII1(), style);
        createCell(row, colNum++,"ETUIII1bis",  r.getReponseEtuIII1bis(), style);
        createCell(row, colNum++, "ETUIII2",   r.getReponseEtuIII2(), style);
        createCell(row, colNum++, "ETUIII2bis",  r.getReponseEtuIII2bis(), style);
        createCell(row, colNum++, "ETUIII4",   r.getReponseEtuIII4(), style);
        createCell(row, colNum++, "ETUIII5a",  r.getReponseEtuIII5a(), style);
        createCell(row, colNum++, "ETUIII5b",  r.getReponseEtuIII5b(), style);
        createCell(row, colNum++, "ETUIII5c",  r.getReponseEtuIII5c(), style);
        createCell(row, colNum++,"ETUIII5bis",  r.getReponseEtuIII5bis(), style);
        createCell(row, colNum++, "ETUIII6",   r.getReponseEtuIII6(), style);
        createCell(row, colNum++,"ETUIII6bis",   r.getReponseEtuIII6bis(), style);
        createCell(row, colNum++, "ETUIII7",   r.getReponseEtuIII7(), style);
        createCell(row, colNum++,"ETUIII7bis",   r.getReponseEtuIII7bis(), style);
        createCell(row, colNum++, "ETUIII8",   r.getReponseEtuIII8(), style);
        createCell(row, colNum++,"ETUIII8bis",   r.getReponseEtuIII8bis(), style);
        createCell(row, colNum++, "ETUIII9",   r.getReponseEtuIII9(), style);
        createCell(row, colNum++,"ETUIII9bis",   r.getReponseEtuIII9bis(), style);
        createCell(row, colNum++, "ETUIII10",  r.getReponseEtuIII10(), style);
        createCell(row, colNum++, "ETUIII11",  r.getReponseEtuIII11(), style);
        createCell(row, colNum++, "ETUIII12",  r.getReponseEtuIII12(), style);
        createCell(row, colNum++, "ETUIII14",  r.getReponseEtuIII14(), style);
        createCell(row, colNum++, "ETUIII15",  r.getReponseEtuIII15(), style);
        createCell(row, colNum++,"ETUIII15bis",   r.getReponseEtuIII15bis(), style);
        createCell(row, colNum++, "ETUIII16",  r.getReponseEtuIII16(), style);
        createCell(row, colNum++,"ETUIII16bis",   r.getReponseEtuIII16bis(), style);
        return colNum;
    }

    private int fillEnseignantData(Row row, int colNum, ReponseEvaluation r, CellStyle style) {
        createCell(row, colNum++, "ENSI1a", r.getReponseEnsI1a(), style);
        createCell(row, colNum++, "ENSI1b", r.getReponseEnsI1b(), style);
        createCell(row, colNum++, "ENSI1c", r.getReponseEnsI1c(), style);
        createCell(row, colNum++, "ENSI2a", r.getReponseEnsI2a(), style);
        createCell(row, colNum++, "ENSI2b", r.getReponseEnsI2b(), style);
        createCell(row, colNum++, "ENSI2c", r.getReponseEnsI2c(), style);
        createCell(row, colNum++, "ENSI3", r.getReponseEnsI3(), style);
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
        return value != null ? value.toString() : "";
    }

    private String formatEtuII5b(Object value) {
        return formatYesNo(value);
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
                        .forEach(q -> headers.add(q.getQuestion()));
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