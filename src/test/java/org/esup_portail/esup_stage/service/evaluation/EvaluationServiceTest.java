package org.esup_portail.esup_stage.service.evaluation;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenJpaRepository;
import org.esup_portail.esup_stage.security.EvaluationJwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EvaluationServiceTest {

    private EvaluationService service;
    private EvaluationTuteurTokenJpaRepository tokenRepository;
    private AppliProperties appliProperties;
    private ObjectProvider<EvaluationJwtService> jwtServiceProvider;
    private EvaluationJwtService jwtService;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        service = new EvaluationService();
        tokenRepository = mock(EvaluationTuteurTokenJpaRepository.class);
        jwtServiceProvider = mock(ObjectProvider.class);
        jwtService = mock(EvaluationJwtService.class);
        appliProperties = new AppliProperties();
        appliProperties.setUrl("https://stage.univ.fr");
        appliProperties.setNbJoursValideToken(30L);
        ReflectionTestUtils.setField(service, "tokenRepository", tokenRepository);
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "evaluationJwtServiceProvider", jwtServiceProvider);
    }

    private Date dans(int jours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, jours);
        return calendar.getTime();
    }

    private EvaluationTuteurToken token(boolean utilise, boolean revoque, Date expiration) {
        EvaluationTuteurToken token = new EvaluationTuteurToken();
        token.setToken("TOKEN-1");
        token.setUtilise(utilise);
        token.setRevoque(revoque);
        token.setExpiresAt(expiration);
        Contact contact = new Contact();
        contact.setId(5);
        token.setContact(contact);
        return token;
    }

    // ------------------------------------------------------------------
    // getToken / validateToken / validateUsedToken
    // ------------------------------------------------------------------

    @Test
    void getTokenIgnoreLesValeursVides() {
        assertThat(service.getToken(null)).isNull();
        assertThat(service.getToken("  ")).isNull();
        verify(tokenRepository, never()).findByToken(any());
    }

    @Test
    void getTokenAccepteUnTokenNonExpireMemeDejaUtilise() {
        EvaluationTuteurToken token = token(true, false, dans(10));
        when(tokenRepository.findByToken("TOKEN-1")).thenReturn(token);

        assertThat(service.getToken("TOKEN-1")).isSameAs(token);
    }

    @Test
    void getTokenRejetteExpireOuRevoqueOuInconnu() {
        when(tokenRepository.findByToken("EXPIRE")).thenReturn(token(false, false, dans(-1)));
        when(tokenRepository.findByToken("REVOQUE")).thenReturn(token(false, true, dans(10)));
        when(tokenRepository.findByToken("ABSENT")).thenReturn(null);

        assertThat(service.getToken("EXPIRE")).isNull();
        assertThat(service.getToken("REVOQUE")).isNull();
        assertThat(service.getToken("ABSENT")).isNull();
    }

    @Test
    void validateTokenNAccepteQueLesTokensActifs() {
        when(tokenRepository.findByToken("ACTIF")).thenReturn(token(false, false, dans(10)));
        when(tokenRepository.findByToken("UTILISE")).thenReturn(token(true, false, dans(10)));

        assertThat(service.validateToken("ACTIF")).isNotNull();
        assertThat(service.validateToken("UTILISE")).isNull();
        assertThat(service.validateToken(null)).isNull();
    }

    @Test
    void validateUsedTokenExigeUnTokenDejaUtiliseNonExpireNonRevoque() {
        when(tokenRepository.findByToken("UTILISE")).thenReturn(token(true, false, dans(10)));
        when(tokenRepository.findByToken("NEUF")).thenReturn(token(false, false, dans(10)));
        when(tokenRepository.findByToken("REVOQUE")).thenReturn(token(true, true, dans(10)));
        when(tokenRepository.findByToken("EXPIRE")).thenReturn(token(true, false, dans(-2)));

        assertThat(service.validateUsedToken("UTILISE")).isNotNull();
        assertThat(service.validateUsedToken("NEUF")).isNull();
        assertThat(service.validateUsedToken("REVOQUE")).isNull();
        assertThat(service.validateUsedToken("EXPIRE")).isNull();
        assertThat(service.validateUsedToken("")).isNull();
    }

    // ------------------------------------------------------------------
    // validateAndUseToken / revokeToken
    // ------------------------------------------------------------------

    @Test
    void validateAndUseTokenMarqueLeTokenUtilise() {
        EvaluationTuteurToken token = token(false, false, dans(10));
        when(tokenRepository.findByToken("TOKEN-1")).thenReturn(token);

        EvaluationTuteurToken resultat = service.validateAndUseToken("TOKEN-1");

        assertThat(resultat).isSameAs(token);
        assertThat(token.getUtilise()).isTrue();
        verify(tokenRepository).save(token);
    }

    @Test
    void validateAndUseTokenRejetteLesTokensInactifsSansLesModifier() {
        EvaluationTuteurToken utilise = token(true, false, dans(10));
        when(tokenRepository.findByToken("UTILISE")).thenReturn(utilise);

        assertThat(service.validateAndUseToken("UTILISE")).isNull();
        assertThat(service.validateAndUseToken("ABSENT")).isNull();
        assertThat(service.validateAndUseToken(" ")).isNull();
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void revokeTokenRevoqueUneSeuleFois() {
        EvaluationTuteurToken token = token(false, false, dans(10));
        when(tokenRepository.findByToken("TOKEN-1")).thenReturn(token);

        assertThat(service.revokeToken("TOKEN-1")).isTrue();
        assertThat(token.getRevoque()).isTrue();
        verify(tokenRepository).save(token);

        // déjà révoqué : pas de double révocation
        assertThat(service.revokeToken("TOKEN-1")).isFalse();
        assertThat(service.revokeToken("ABSENT")).isFalse();
        assertThat(service.revokeToken(null)).isFalse();
    }

    // ------------------------------------------------------------------
    // buildEvaluationTuteurUrl
    // ------------------------------------------------------------------

    @Test
    void urlDEvaluationDepuisUnTokenBrut() {
        assertThat(service.buildEvaluationTuteurUrl(""))
                .isEmpty();
        assertThat(service.buildEvaluationTuteurUrl("TOK"))
                .isEqualTo("https://stage.univ.fr/frontend/#/evaluation-tuteur?token=TOK");
    }

    @Test
    void urlDEvaluationDepuisUneConventionSansContactEstVide() {
        assertThat(service.buildEvaluationTuteurUrl((Convention) null)).isEmpty();
        assertThat(service.buildEvaluationTuteurUrl(new Convention())).isEmpty();
    }

    @Test
    void urlDEvaluationReutiliseUnTokenActifExistant() {
        Convention convention = conventionAvecContact();
        EvaluationTuteurToken actif = token(false, false, dans(10));
        when(tokenRepository.findByConventionIdAndTuteurId(42, 5)).thenReturn(List.of(actif));

        String url = service.buildEvaluationTuteurUrl(convention);

        assertThat(url).contains("token=TOKEN-1");
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void urlDEvaluationEstVideSiLeServiceJwtEstIndisponible() {
        Convention convention = conventionAvecContact();
        when(tokenRepository.findByConventionIdAndTuteurId(42, 5)).thenReturn(List.of());
        when(jwtServiceProvider.getIfAvailable()).thenReturn(null);

        assertThat(service.buildEvaluationTuteurUrl(convention)).isEmpty();
    }

    @Test
    void urlDEvaluationCreeUnTokenSiAucunActif() {
        Convention convention = conventionAvecContact();
        when(tokenRepository.findByConventionIdAndTuteurId(42, 5)).thenReturn(List.of(token(true, false, dans(10))));
        when(jwtServiceProvider.getIfAvailable()).thenReturn(jwtService);
        when(jwtService.createToken(any(), any(), any(), any())).thenReturn("JWT-GENERE");
        when(tokenRepository.save(any(EvaluationTuteurToken.class))).thenAnswer(inv -> inv.getArgument(0));

        String url = service.buildEvaluationTuteurUrl(convention);

        assertThat(url).contains("token=JWT-GENERE");
        verify(tokenRepository).save(any(EvaluationTuteurToken.class));
    }

    @Test
    void createTokenReutiliseUnTokenActifExistant() {
        Convention convention = conventionAvecContact();
        EvaluationTuteurToken actif = token(false, false, dans(10));
        when(jwtServiceProvider.getIfAvailable()).thenReturn(jwtService);
        when(tokenRepository.findByConventionIdAndTuteurId(42, 5)).thenReturn(List.of(actif));

        assertThat(service.createToken(convention)).isSameAs(actif);
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void createTokenSansConventionOuContactEstNull() {
        assertThat(service.createToken(null)).isNull();
        assertThat(service.createToken(new Convention())).isNull();
    }

    private Convention conventionAvecContact() {
        Convention convention = new Convention();
        convention.setId(42);
        Contact contact = new Contact();
        contact.setId(5);
        convention.setContact(contact);
        return convention;
    }

    // ------------------------------------------------------------------
    // fiches d'évaluation et réponses
    // ------------------------------------------------------------------

    @Test
    void getByCentreGestionRenvoieLaFicheExistante() {
        org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository ficheRepo =
                mock(org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository.class);
        ReflectionTestUtils.setField(service, "ficheEvaluationJpaRepository", ficheRepo);
        org.esup_portail.esup_stage.model.FicheEvaluation fiche = new org.esup_portail.esup_stage.model.FicheEvaluation();
        when(ficheRepo.findByCentreGestion(3)).thenReturn(fiche);

        assertThat(service.getByCentreGestion(3)).isSameAs(fiche);
    }

    @Test
    void getByCentreGestionCreeLaFicheManquante() {
        org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository ficheRepo =
                mock(org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository.class);
        org.esup_portail.esup_stage.repository.CentreGestionJpaRepository centreRepo =
                mock(org.esup_portail.esup_stage.repository.CentreGestionJpaRepository.class);
        ReflectionTestUtils.setField(service, "ficheEvaluationJpaRepository", ficheRepo);
        ReflectionTestUtils.setField(service, "centreGestionJpaRepository", centreRepo);
        when(ficheRepo.findByCentreGestion(3)).thenReturn(null);
        org.esup_portail.esup_stage.model.CentreGestion centre = new org.esup_portail.esup_stage.model.CentreGestion();
        when(centreRepo.findById((Integer) 3)).thenReturn(java.util.Optional.of(centre));
        when(ficheRepo.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        org.esup_portail.esup_stage.model.FicheEvaluation fiche = service.getByCentreGestion(3);

        assertThat(fiche.getCentreGestion()).isSameAs(centre);
    }

    @Test
    void getByCentreGestionEchoueSiCentreInconnu() {
        org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository ficheRepo =
                mock(org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository.class);
        org.esup_portail.esup_stage.repository.CentreGestionJpaRepository centreRepo =
                mock(org.esup_portail.esup_stage.repository.CentreGestionJpaRepository.class);
        ReflectionTestUtils.setField(service, "ficheEvaluationJpaRepository", ficheRepo);
        ReflectionTestUtils.setField(service, "centreGestionJpaRepository", centreRepo);
        when(ficheRepo.findByCentreGestion(9)).thenReturn(null);
        when(centreRepo.findById((Integer) 9)).thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.getByCentreGestion(9))
                .isInstanceOf(org.esup_portail.esup_stage.exception.AppException.class);
    }

    @Test
    void initReponseEvaluationAssembleLesIdentifiants() {
        org.esup_portail.esup_stage.repository.ConventionJpaRepository conventionRepo =
                mock(org.esup_portail.esup_stage.repository.ConventionJpaRepository.class);
        org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository ficheRepo =
                mock(org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionRepo);
        ReflectionTestUtils.setField(service, "ficheEvaluationJpaRepository", ficheRepo);

        Convention convention = new Convention();
        convention.setId(42);
        org.esup_portail.esup_stage.model.CentreGestion centre = new org.esup_portail.esup_stage.model.CentreGestion();
        centre.setId(3);
        convention.setCentreGestion(centre);
        when(conventionRepo.findById(42)).thenReturn(convention);
        org.esup_portail.esup_stage.model.FicheEvaluation fiche = new org.esup_portail.esup_stage.model.FicheEvaluation();
        fiche.setId(8);
        when(ficheRepo.findByCentreGestion(3)).thenReturn(fiche);

        org.esup_portail.esup_stage.model.ReponseEvaluation reponse = service.initReponseEvaluation(42);

        assertThat(reponse.getConvention()).isSameAs(convention);
        assertThat(reponse.getFicheEvaluation()).isSameAs(fiche);
        assertThat(reponse.getReponseEvaluationId().getIdConvention()).isEqualTo(42);
        assertThat(reponse.getReponseEvaluationId().getIdFicheEvaluation()).isEqualTo(8);
    }

    @Test
    void initReponseEvaluationEchoueSiConventionInconnue() {
        org.esup_portail.esup_stage.repository.ConventionJpaRepository conventionRepo =
                mock(org.esup_portail.esup_stage.repository.ConventionJpaRepository.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionRepo);
        when(conventionRepo.findById(99)).thenReturn(null);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.initReponseEvaluation(99))
                .isInstanceOf(org.esup_portail.esup_stage.exception.AppException.class);
    }

    @Test
    void lesReponsesDesFormulairesSontCopieesDansLEntite() throws Exception {
        org.esup_portail.esup_stage.model.ReponseEvaluation reponse = new org.esup_portail.esup_stage.model.ReponseEvaluation();

        org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto etudiantForm =
                rempli(new org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto());
        service.setReponseEvaluationEtudiantData(reponse, etudiantForm);
        assertThat(reponse.getReponseEtuI1()).isEqualTo(2);

        org.esup_portail.esup_stage.dto.ReponseEnseignantFormDto enseignantForm =
                rempli(new org.esup_portail.esup_stage.dto.ReponseEnseignantFormDto());
        service.setReponseEvaluationEnseignantData(reponse, enseignantForm);
        assertThat(reponse.getReponseEnsII1()).isEqualTo(2);

        org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto entrepriseForm =
                rempli(new org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto());
        service.setReponseEvaluationEntrepriseData(reponse, entrepriseForm);
        assertThat(reponse.getReponseEnt1()).isEqualTo(2);

        org.esup_portail.esup_stage.model.ReponseSupplementaire reponseSupplementaire =
                new org.esup_portail.esup_stage.model.ReponseSupplementaire();
        org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto supplementaireForm =
                rempli(new org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto());
        service.setReponseSupplementaireData(reponseSupplementaire, supplementaireForm);
        assertThat(reponseSupplementaire.getReponseTxt()).isEqualTo("texte");
    }

    /** Remplit toutes les propriétés simples du DTO via réflexion. */
    private <T> T rempli(T dto) throws Exception {
        for (java.lang.reflect.Method method : dto.getClass().getMethods()) {
            if (!method.getName().startsWith("set") || method.getParameterCount() != 1) {
                continue;
            }
            Class<?> type = method.getParameterTypes()[0];
            if (type == Integer.class) {
                method.invoke(dto, 2);
            } else if (type == Boolean.class) {
                method.invoke(dto, true);
            } else if (type == String.class) {
                method.invoke(dto, "texte");
            }
        }
        return dto;
    }

    // ------------------------------------------------------------------
    // paramétrage des fiches, questions supplémentaires et exports
    // ------------------------------------------------------------------

    /** Coche toutes les cases (booléens primitifs ou wrappers) du DTO. */
    private <T> T coche(T dto) throws Exception {
        for (java.lang.reflect.Method method : dto.getClass().getMethods()) {
            if (method.getName().startsWith("set") && method.getParameterCount() == 1
                    && (method.getParameterTypes()[0] == boolean.class || method.getParameterTypes()[0] == Boolean.class)) {
                method.invoke(dto, true);
            }
        }
        return dto;
    }

    @Test
    void lesTroisFichesCopientLeursCasesACocher() throws Exception {
        org.esup_portail.esup_stage.model.FicheEvaluation fiche = new org.esup_portail.esup_stage.model.FicheEvaluation();

        service.setFicheEtudiantData(fiche, coche(new org.esup_portail.esup_stage.dto.FicheEtudiantDto()));
        assertThat(fiche.getQuestionEtuI1()).isTrue();
        assertThat(fiche.getQuestionEtuIII16()).isTrue();

        service.setFicheEnseignantData(fiche, coche(new org.esup_portail.esup_stage.dto.FicheEnseignantDto()));
        assertThat(fiche.getQuestionEnsI1()).isTrue();
        assertThat(fiche.getQuestionEnsII11()).isTrue();

        service.setFicheEntrepriseData(fiche, coche(new org.esup_portail.esup_stage.dto.FicheEntrepriseDto()));
        assertThat(fiche.getQuestionEnt1()).isTrue();
        assertThat(fiche.getQuestionEnt19()).isTrue();
    }

    @Test
    void laQuestionSupplementaireEstCopieeDepuisLeDto() {
        org.esup_portail.esup_stage.dto.QuestionSupplementaireDto dto = new org.esup_portail.esup_stage.dto.QuestionSupplementaireDto();
        dto.setQuestion("Question bonus ?");
        dto.setTypeQuestion("txt");
        dto.setIdPlacement(1);
        org.esup_portail.esup_stage.model.QuestionSupplementaire question = new org.esup_portail.esup_stage.model.QuestionSupplementaire();

        service.setQuestionSupplementaireData(question, dto);

        assertThat(question.getQuestion()).isEqualTo("Question bonus ?");
        assertThat(question.getTypeQuestion()).isEqualTo("txt");
        assertThat(question.getIdPlacement()).isEqualTo(1);
    }

    @Test
    void initReponseSupplementaireAssembleLesIdentifiants() {
        var conventionJpaRepository = mock(org.esup_portail.esup_stage.repository.ConventionJpaRepository.class);
        var questionSupplementaireJpaRepository = mock(org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "questionSupplementaireJpaRepository", questionSupplementaireJpaRepository);

        Convention convention = new Convention();
        convention.setId(42);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        org.esup_portail.esup_stage.model.QuestionSupplementaire question = new org.esup_portail.esup_stage.model.QuestionSupplementaire();
        question.setId(7);
        when(questionSupplementaireJpaRepository.findById(7)).thenReturn(question);

        var reponse = service.initReponseSupplementaire(42, 7);

        assertThat(reponse.getId().getIdConvention()).isEqualTo(42);
        assertThat(reponse.getId().getIdQuestionSupplementaire()).isEqualTo(7);
        assertThat(reponse.getConvention()).isSameAs(convention);
        assertThat(reponse.getQuestionSupplementaire()).isSameAs(question);

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.initReponseSupplementaire(99, 7))
                .isInstanceOf(org.esup_portail.esup_stage.exception.AppException.class);
        when(questionSupplementaireJpaRepository.findById(8)).thenReturn(null);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.initReponseSupplementaire(42, 8))
                .isInstanceOf(org.esup_portail.esup_stage.exception.AppException.class);
    }

    @Test
    void lExportExcelChoisitLeTypeSelonLaFiche() {
        EvaluationExcelExporter exporter = mock(EvaluationExcelExporter.class);
        ReflectionTestUtils.setField(service, "evaluationExcelExporter", exporter);
        when(exporter.export(any(), any(), any())).thenReturn(new byte[]{1});

        assertThat(service.getEvaluationToExcel(List.of(), 0, null)).containsExactly((byte) 1);
        assertThat(service.getEvaluationToExcel(List.of(), 1, null)).containsExactly((byte) 1);
        assertThat(service.getEvaluationToExcel(List.of(), 2, null)).containsExactly((byte) 1);
        assertThat(service.getEvaluationToExcel(List.of(), 3, null)).containsExactly((byte) 1);

        verify(exporter).export(any(), org.mockito.ArgumentMatchers.eq(org.esup_portail.esup_stage.enums.ExportType.ETUDIANT), any());
        verify(exporter).export(any(), org.mockito.ArgumentMatchers.eq(org.esup_portail.esup_stage.enums.ExportType.ENSEIGNANT), any());
        verify(exporter).export(any(), org.mockito.ArgumentMatchers.eq(org.esup_portail.esup_stage.enums.ExportType.ENTREPRISE), any());
        verify(exporter).export(any(), org.mockito.ArgumentMatchers.eq(org.esup_portail.esup_stage.enums.ExportType.ALL_IN_ONE), any());
    }
}
