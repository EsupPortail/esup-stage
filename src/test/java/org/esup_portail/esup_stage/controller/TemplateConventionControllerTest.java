package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.TemplateConventionDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.model.ParamConvention;
import org.esup_portail.esup_stage.model.TemplateConvention;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.ParamConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateConventionRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemplateConventionControllerTest {

    private TemplateConventionController controller;
    private TemplateConventionRepository templateConventionRepository;
    private TemplateConventionJpaRepository templateConventionJpaRepository;
    private ParamConventionJpaRepository paramConventionJpaRepository;
    private ImpressionService impressionService;

    @BeforeEach
    void setUp() {
        controller = new TemplateConventionController();
        templateConventionRepository = mock(TemplateConventionRepository.class);
        templateConventionJpaRepository = mock(TemplateConventionJpaRepository.class);
        paramConventionJpaRepository = mock(ParamConventionJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        controller.templateConventionRepository = templateConventionRepository;
        controller.templateConventionJpaRepository = templateConventionJpaRepository;
        controller.paramConventionJpaRepository = paramConventionJpaRepository;
        controller.impressionService = impressionService;

        ParamConvention nomEtudiant = new ParamConvention();
        nomEtudiant.setCode("nomEtudiant");
        when(paramConventionJpaRepository.findAll()).thenReturn(List.of(nomEtudiant));
    }

    private TemplateConvention template(String texte, String texteAvenant) {
        TemplateConvention templateConvention = new TemplateConvention();
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setId(7);
        LangueConvention langueConvention = new LangueConvention();
        langueConvention.setCode("fr");
        templateConvention.setTypeConvention(typeConvention);
        templateConvention.setLangueConvention(langueConvention);
        templateConvention.setTexte(texte);
        templateConvention.setTexteAvenant(texteAvenant);
        return templateConvention;
    }

    @Test
    void searchRetourneTotalEtDonnees() {
        when(templateConventionRepository.count("{}")).thenReturn(2L);
        when(templateConventionRepository.findPaginated(1, 50, "id", "asc", "{}"))
                .thenReturn(List.of(new TemplateConvention()));

        PaginatedResponse<TemplateConvention> reponse = controller.search(1, 50, "id", "asc", "{}");

        assertThat(reponse.getTotal()).isEqualTo(2L);
        assertThat(reponse.getData()).hasSize(1);
    }

    @Test
    void lesExportsDeleguentAuRepository() {
        when(templateConventionRepository.exportExcel("{}", "id", "asc", "{}")).thenReturn(new byte[]{1, 2});
        when(templateConventionRepository.exportCsv("{}", "id", "asc", "{}")).thenReturn(new StringBuilder("export-csv"));

        ResponseEntity<byte[]> excel = controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse());
        ResponseEntity<String> csv = controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(excel.getBody()).containsExactly((byte) 1, (byte) 2);
        assertThat(csv.getBody()).isEqualTo("export-csv");
    }

    @Test
    void createEnregistreUnTemplateAvecChampsValides() {
        // suffixe FreeMarker "?html" autorisé : seule la partie avant le '?' est vérifiée
        TemplateConvention templateConvention = template("Bonjour ${nomEtudiant?html}", "Avenant ${nomEtudiant}");
        when(templateConventionJpaRepository.findByTypeAndLangue(7, "fr")).thenReturn(null);
        when(templateConventionJpaRepository.saveAndFlush(any(TemplateConvention.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TemplateConvention enregistre = controller.create(templateConvention);

        assertThat(enregistre).isSameAs(templateConvention);
        verify(templateConventionJpaRepository).saveAndFlush(templateConvention);
    }

    @Test
    void createRefuseUnDoublonTypeLangue() {
        when(templateConventionJpaRepository.findByTypeAndLangue(7, "fr")).thenReturn(new TemplateConvention());

        assertThatThrownBy(() -> controller.create(template("texte", "avenant")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(templateConventionJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void createRefuseLesDirectivesFreemarkerNatives() {
        when(templateConventionJpaRepository.findByTypeAndLangue(7, "fr")).thenReturn(null);

        assertThatThrownBy(() -> controller.create(template("<#if x>oui</#if>", "avenant")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(templateConventionJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void createRefuseUnChampPersonnaliseInconnu() {
        when(templateConventionJpaRepository.findByTypeAndLangue(7, "fr")).thenReturn(null);

        assertThatThrownBy(() -> controller.create(template("Bonjour ${inconnu}", "avenant")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(templateConventionJpaRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateModifieLesTextes() {
        TemplateConvention existant = template("ancien", "ancien");
        when(templateConventionJpaRepository.findById(3)).thenReturn(existant);
        when(templateConventionJpaRepository.saveAndFlush(existant)).thenReturn(existant);

        TemplateConvention maj = controller.update(3, new TemplateConventionDto("Nouveau ${nomEtudiant}", "Avenant sans champ"));

        assertThat(maj.getTexte()).contains("Nouveau");
        assertThat(maj.getTexteAvenant()).isEqualTo("Avenant sans champ");
        verify(templateConventionJpaRepository).saveAndFlush(existant);
    }

    @Test
    void updateRejetteUnIdInconnu() {
        when(templateConventionJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.update(99, new TemplateConventionDto("texte", "avenant")))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteSupprimeEtFlush() {
        controller.delete(5);

        verify(templateConventionJpaRepository).deleteById(5);
        verify(templateConventionJpaRepository).flush();
    }

    @Test
    void lesTextesParDefautViennentDeLImpressionService() {
        when(impressionService.getDefaultText(true)).thenReturn("texte convention");
        when(impressionService.getDefaultText(false)).thenReturn("texte avenant");

        assertThat(controller.getDefaultTemplateConvention()).isEqualTo("texte convention");
        assertThat(controller.getDefaultTemplateAvenant()).isEqualTo("texte avenant");
    }

    @Test
    void getAllTemplatesRetourneToutesLesEntrees() {
        when(templateConventionJpaRepository.findAll())
                .thenReturn(List.of(new TemplateConvention(), new TemplateConvention()));

        assertThat(controller.getAllTemplates()).hasSize(2);
    }
}
