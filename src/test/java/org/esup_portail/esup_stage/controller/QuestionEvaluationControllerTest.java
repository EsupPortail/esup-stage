package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.QuestionEvaluation;
import org.esup_portail.esup_stage.repository.QuestionEvaluationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des questions d'évaluation. On couvre la projection en
 * DTO, la construction des options (JSON explicite, échelles prédéfinies, expand absent)
 * et les 404 systématiques quand aucune question n'est trouvée.
 */
class QuestionEvaluationControllerTest {

    private QuestionEvaluationController controller;
    private QuestionEvaluationJpaRepository repository;

    @BeforeEach
    void setUp() {
        controller = new QuestionEvaluationController();
        repository = mock(QuestionEvaluationJpaRepository.class);
        org.springframework.test.util.ReflectionTestUtils.setField(
                controller, "questionEvaluationJpaRepository", repository);
    }

    private QuestionEvaluation question(String code, TypeQuestionEvaluation type) {
        QuestionEvaluation q = new QuestionEvaluation();
        q.setCode(code);
        q.setTexte("Texte " + code);
        q.setType(type);
        return q;
    }

    private void assertNotFound(org.assertj.core.api.ThrowableAssert.ThrowingCallable appel) {
        assertThatThrownBy(appel)
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getQuestionsLeveUn404QuandAucuneQuestion() {
        when(repository.findAll()).thenReturn(List.of());
        assertNotFound(() -> controller.getQuestions(null));
    }

    @Test
    void getQuestionsProjetteSansOptionsQuandExpandAbsent() {
        when(repository.findAll()).thenReturn(List.of(question("ETU1", TypeQuestionEvaluation.SCALE_LIKERT_5)));

        var resultat = controller.getQuestions(null);

        assertThat(resultat).hasSize(1);
        // expand absent : pas d'options même sur une échelle
        assertThat(resultat.toString()).contains("ETU1").contains("options=[]");
    }

    @Test
    void getQuestionsRenvoieLesOptionsDEchelleQuandExpandOptions() {
        when(repository.findAll()).thenReturn(List.of(question("ETU1", TypeQuestionEvaluation.SCALE_LIKERT_5)));

        var resultat = controller.getQuestions("options");

        assertThat(resultat.toString())
                .contains("Excellent").contains("Insuffisant");
    }

    @Test
    void getQuestionsPrivilegieLesOptionsDuJsonSurLEchelle() {
        QuestionEvaluation q = question("ETU2", TypeQuestionEvaluation.SCALE_LIKERT_5);
        q.setParamsJson("{\"options\":[\"Rouge\",\"Vert\"]}");
        when(repository.findAll()).thenReturn(List.of(q));

        var resultat = controller.getQuestions("options");

        assertThat(resultat.toString())
                .contains("Rouge").contains("Vert")
                .doesNotContain("Excellent");
    }

    @Test
    void getQuestionEvaluationParCodeTrouveOuLeve404() {
        QuestionEvaluation q = question("ENS1", TypeQuestionEvaluation.YES_NO);
        when(repository.findByCode("ENS1")).thenReturn(q);
        assertThat(controller.getQuestionEvaluation("ENS1")).isSameAs(q);

        when(repository.findByCode("XXX")).thenReturn(null);
        assertNotFound(() -> controller.getQuestionEvaluation("XXX"));
    }

    @Test
    void getQuestionsEtuEnsEntFiltrentParCodeEtLeve404SiVide() {
        when(repository.findByCodeContaining("ETU"))
                .thenReturn(List.of(question("ETU1", TypeQuestionEvaluation.TEXT)));
        assertThat(controller.getQuestionsEtu()).hasSize(1);

        when(repository.findByCodeContaining("ENS")).thenReturn(List.of());
        assertNotFound(() -> controller.getQuestionsEns());

        when(repository.findByCodeContaining("ENT")).thenReturn(List.of());
        assertNotFound(() -> controller.getQuestionsEnt());
    }
}
