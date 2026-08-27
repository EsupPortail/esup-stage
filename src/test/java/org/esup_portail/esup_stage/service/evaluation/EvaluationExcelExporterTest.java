package org.esup_portail.esup_stage.service.evaluation;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.esup_portail.esup_stage.dto.EvaluationDto;
import org.esup_portail.esup_stage.enums.ExportType;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.QuestionEvaluationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvaluationExcelExporterTest {

    private EvaluationExcelExporter exporter;
    private QuestionEvaluationJpaRepository questionEvaluationJpaRepository;

    @BeforeEach
    void setUp() {
        exporter = new EvaluationExcelExporter();
        questionEvaluationJpaRepository = mock(QuestionEvaluationJpaRepository.class);
        when(questionEvaluationJpaRepository.findAll()).thenReturn(new ArrayList<>());
        ReflectionTestUtils.setField(exporter, "questionEvaluationJpaRepository", questionEvaluationJpaRepository);
    }

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private EvaluationDto evaluationComplete() {
        EvaluationDto dto = new EvaluationDto();
        dto.setIdConvention(42);

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        dto.setEtudiant(etudiant);

        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        dto.setStructure(structure);

        dto.setDateDebutStage(date(2026, 3, 2));
        dto.setDateFinStage(date(2026, 8, 31));

        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setNomCentre("Centre Sciences");
        dto.setCentreGestion(centreGestion);

        Etape etape = new Etape();
        etape.setLibelle("L3 Informatique");
        dto.setEtape(etape);

        dto.setAnneeUniversitaire("2025/2026");

        FicheEvaluation fiche = new FicheEvaluation();
        fiche.setValidationEtudiant(true);
        fiche.setValidationEnseignant(true);
        fiche.setValidationEntreprise(true);
        dto.setFicheEvaluation(fiche);

        // toutes les réponses remplies (via réflexion) pour exercer chaque
        // branche de formatage : notes 1-5, booléens Oui/Non, textes libres
        ReponseEvaluation reponse = new ReponseEvaluation();
        java.util.Arrays.stream(ReponseEvaluation.class.getMethods())
                .filter(m -> m.getName().startsWith("setReponse") && m.getParameterCount() == 1)
                .forEach(m -> {
                    try {
                        Class<?> type = m.getParameterTypes()[0];
                        if (type == Integer.class) {
                            m.invoke(reponse, 2);
                        } else if (type == Boolean.class) {
                            m.invoke(reponse, true);
                        } else if (type == String.class) {
                            m.invoke(reponse, "Réponse libre");
                        }
                    } catch (ReflectiveOperationException ignored) {
                        // setter non standard : ignoré
                    }
                });
        dto.setReponseEvaluation(reponse);

        QuestionSupplementaire questionSupplementaire = new QuestionSupplementaire();
        questionSupplementaire.setId(7);
        questionSupplementaire.setIdPlacement(1);
        questionSupplementaire.setQuestion("Question bonus ?");
        questionSupplementaire.setTypeQuestion("txt");
        dto.setQuestionSupplementaires(List.of(questionSupplementaire));

        ReponseSupplementaire reponseSupplementaire = new ReponseSupplementaire();
        ReponseSupplementaireId id = new ReponseSupplementaireId();
        id.setIdQuestionSupplementaire(7);
        id.setIdConvention(42);
        reponseSupplementaire.setId(id);
        reponseSupplementaire.setReponseTxt("Réponse bonus");
        dto.setReponseSupplementaires(List.of(reponseSupplementaire));

        return dto;
    }

    private Workbook lire(byte[] bytes) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(bytes));
    }

    private List<String> valeursLigne(Sheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        List<String> values = new ArrayList<>();
        if (row == null) {
            return values;
        }
        for (int i = 0; i < row.getLastCellNum(); i++) {
            values.add(row.getCell(i) == null ? "" : row.getCell(i).toString());
        }
        return values;
    }

    @Test
    void exportEtudiantContientEnteteEtDonnees() throws IOException {
        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ETUDIANT);

        try (Workbook workbook = lire(bytes)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Évaluations");

            List<String> entetes = valeursLigne(sheet, 0);
            assertThat(entetes).contains("ID Convention", "Nom Étudiant", "Prénom Étudiant",
                    "Structure", "Centre de Gestion", "Année Universitaire");
            assertThat(entetes).contains("Question bonus ?");

            List<String> donnees = valeursLigne(sheet, 1);
            assertThat(donnees).isNotEmpty();
            assertThat(String.join("|", donnees))
                    .contains("42")
                    .contains("Durand")
                    .contains("Alice")
                    .contains("ACME")
                    .contains("02/03/2026")
                    .contains("2025/2026");
        }
    }

    @Test
    void exportToutEnUnCreeTroisFeuilles() throws IOException {
        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ALL_IN_ONE);

        try (Workbook workbook = lire(bytes)) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(3);
            assertThat(workbook.getSheetAt(0).getSheetName()).isEqualTo("Évaluations Étudiant");
            assertThat(workbook.getSheetAt(1).getSheetName()).isEqualTo("Évaluations Enseignant");
            assertThat(workbook.getSheetAt(2).getSheetName()).isEqualTo("Évaluations Entreprise");
            // chaque feuille contient la ligne de données (fiche validée partout)
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isEqualTo(1);
            assertThat(workbook.getSheetAt(1).getLastRowNum()).isEqualTo(1);
            assertThat(workbook.getSheetAt(2).getLastRowNum()).isEqualTo(1);
        }
    }

    @Test
    void exportEnseignantEtEntrepriseFonctionnent() throws IOException {
        for (ExportType type : List.of(ExportType.ENSEIGNANT, ExportType.ENTREPRISE)) {
            byte[] bytes = exporter.export(List.of(evaluationComplete()), type);
            try (Workbook workbook = lire(bytes)) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getLastRowNum()).as("une ligne de données pour %s", type).isEqualTo(1);
            }
        }
    }

    @Test
    void evaluationSansFicheOuSansReponseEstExclue() throws IOException {
        EvaluationDto sansFiche = evaluationComplete();
        sansFiche.setFicheEvaluation(null);
        EvaluationDto sansReponse = evaluationComplete();
        sansReponse.setReponseEvaluation(null);
        EvaluationDto ficheNonValidee = evaluationComplete();
        ficheNonValidee.getFicheEvaluation().setValidationEtudiant(false);

        byte[] bytes = exporter.export(List.of(sansFiche, sansReponse, ficheNonValidee), ExportType.ETUDIANT);

        try (Workbook workbook = lire(bytes)) {
            // seule la ligne d'entête est présente
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isZero();
        }
    }

    @Test
    void filtreDeColonnesRestreintLExport() throws IOException {
        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ETUDIANT,
                List.of("COMMON_ID_CONVENTION", "COMMON_NOM_ETUDIANT"));

        try (Workbook workbook = lire(bytes)) {
            List<String> entetes = valeursLigne(workbook.getSheetAt(0), 0);
            assertThat(entetes).containsExactly("ID Convention", "Nom Étudiant");
        }
    }

    @Test
    void filtreDeColonnesParPrefixePourLExportToutEnUn() throws IOException {
        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ALL_IN_ONE,
                List.of("COMMON_ID_CONVENTION", "ETU_ETUI1", "ENS_ENSII1", "ENT_ENT1"));

        try (Workbook workbook = lire(bytes)) {
            List<String> entetesEtu = valeursLigne(workbook.getSheetAt(0), 0);
            assertThat(entetesEtu).hasSize(2).contains("ID Convention");
            List<String> entetesEns = valeursLigne(workbook.getSheetAt(1), 0);
            assertThat(entetesEns).hasSize(2).contains("ID Convention");
            List<String> entetesEnt = valeursLigne(workbook.getSheetAt(2), 0);
            assertThat(entetesEnt).hasSize(2).contains("ID Convention");
        }
    }

    @Test
    void lesLibellesDeQuestionsPersonnalisesSontUtilises() throws IOException {
        QuestionEvaluation etuI1 = new QuestionEvaluation();
        etuI1.setCode("ETUI1");
        etuI1.setTexte("Comment s'est passé l'accueil ?");
        etuI1.setBisQuestion("Précisez");
        etuI1.setType(TypeQuestionEvaluation.SCALE_LIKERT_5);
        QuestionEvaluation etuI4 = new QuestionEvaluation();
        etuI4.setCode("ETUI4");
        etuI4.setTexte("Aides reçues");
        etuI4.setParamsJson("{\"items\":[\"Logement\",\"Transport\",\"Repas\",\"Autre\"]}");
        etuI4.setType(TypeQuestionEvaluation.BOOLEAN_GROUP);
        when(questionEvaluationJpaRepository.findAll()).thenReturn(List.of(etuI1, etuI4));

        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ETUDIANT);

        try (Workbook workbook = lire(bytes)) {
            List<String> entetes = valeursLigne(workbook.getSheetAt(0), 0);
            assertThat(entetes).contains("Comment s'est passé l'accueil ?", "Précisez");
            assertThat(entetes).anyMatch(h -> h.contains("Aides reçues") && h.contains("Logement"));
        }
    }

    @Test
    void chaqueTypeDeQuestionFormateSaValeur() throws IOException {
        List<QuestionEvaluation> questions = new ArrayList<>();
        questions.add(question("ETUI1", TypeQuestionEvaluation.SCALE_LIKERT_5, null));
        questions.add(question("ETUI2", TypeQuestionEvaluation.SCALE_AGREEMENT_5, null));
        questions.add(question("ETUI3", TypeQuestionEvaluation.YES_NO, null));
        questions.add(question("ETUI5", TypeQuestionEvaluation.SINGLE_CHOICE,
                "{\"items\":[\"Choix A\",\"Choix B\",\"Choix C\",\"Choix D\"]}"));
        questions.add(question("ETUI6", TypeQuestionEvaluation.AUTO,
                "{\"items\":[\"Auto A\",\"Auto B\",\"Auto C\"]}"));
        questions.add(question("ETUII4", TypeQuestionEvaluation.AUTO, null));
        questions.add(question("ETUI8", TypeQuestionEvaluation.BOOLEAN_GROUP, null));
        questions.add(question("ENT19", TypeQuestionEvaluation.TEXT, null));
        when(questionEvaluationJpaRepository.findAll()).thenReturn(questions);

        byte[] bytes = exporter.export(List.of(evaluationComplete()), ExportType.ETUDIANT);

        try (Workbook workbook = lire(bytes)) {
            String ligne = String.join("|", valeursLigne(workbook.getSheetAt(0), 1));
            // note 2 sur l'échelle Likert = "Bien", sur l'échelle d'accord = "Sans avis"
            assertThat(ligne).contains("Bien").contains("Sans avis");
            // choix simple : l'indice 2 pointe le 3e item
            assertThat(ligne).contains("Choix C");
            // booléens rendus Oui/Non
            assertThat(ligne).contains("Oui");
        }
    }

    private QuestionEvaluation question(String code, TypeQuestionEvaluation type, String paramsJson) {
        QuestionEvaluation question = new QuestionEvaluation();
        question.setCode(code);
        question.setTexte("Question " + code);
        question.setType(type);
        question.setParamsJson(paramsJson);
        return question;
    }

    @Test
    void filtreSansCorrespondanceBasculeSurLeRemplissageComplet() throws IOException {
        // aucun code de colonne reconnu : l'exporteur retombe sur le remplissage
        // historique (toutes les colonnes du type) pour chaque feuille
        for (ExportType type : List.of(ExportType.ETUDIANT, ExportType.ENSEIGNANT, ExportType.ENTREPRISE)) {
            byte[] bytes = exporter.export(List.of(evaluationComplete()), type, List.of("COLONNE_INCONNUE"));
            try (Workbook workbook = lire(bytes)) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getRow(1)).as("ligne de données présente pour %s", type).isNotNull();
                assertThat(sheet.getRow(1).getLastCellNum()).isGreaterThan((short) 5);
            }
        }
    }

    @Test
    void exportSansEvaluationProduitUnClasseurValide() throws IOException {
        byte[] bytes = exporter.export(List.of(), ExportType.ETUDIANT);

        try (Workbook workbook = lire(bytes)) {
            assertThat(workbook.getSheetAt(0).getLastRowNum()).isZero();
            assertThat(valeursLigne(workbook.getSheetAt(0), 0)).contains("ID Convention");
        }
    }
}
