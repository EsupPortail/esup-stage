package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.TemplateMailFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ParamMail;
import org.esup_portail.esup_stage.model.TemplateMail;
import org.esup_portail.esup_stage.repository.ParamMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateMailControllerTest {

    private TemplateMailController controller;
    private TemplateMailRepository templateMailRepository;
    private TemplateMailJpaRepository templateMailJpaRepository;
    private ParamMailJpaRepository paramMailJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new TemplateMailController();
        templateMailRepository = mock(TemplateMailRepository.class);
        templateMailJpaRepository = mock(TemplateMailJpaRepository.class);
        paramMailJpaRepository = mock(ParamMailJpaRepository.class);
        controller.templateMailRepository = templateMailRepository;
        controller.templateMailJpaRepository = templateMailJpaRepository;
        controller.paramMailJpaRepository = paramMailJpaRepository;

        when(templateMailJpaRepository.saveAndFlush(any(TemplateMail.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ParamMail param(String code) {
        ParamMail paramMail = new ParamMail();
        paramMail.setCode(code);
        return paramMail;
    }

    @Test
    void rechercheExportsEtLectureDeleguent() {
        when(templateMailRepository.count(anyString())).thenReturn(1L);
        when(templateMailRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new TemplateMail()));
        when(templateMailRepository.exportExcel(any(), any(), any(), any())).thenReturn("xls".getBytes());
        when(templateMailRepository.exportCsv(any(), any(), any(), any())).thenReturn(new StringBuilder("csv"));
        TemplateMail templateMail = new TemplateMail();
        when(templateMailJpaRepository.findById(7)).thenReturn(templateMail);
        when(paramMailJpaRepository.findAll()).thenReturn(List.of(param("etudiant.nom")));

        assertThat(controller.search(1, 50, "id", "asc", "{}").getTotal()).isEqualTo(1L);
        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isNotEmpty();
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");
        assertThat(controller.getById(7)).isSameAs(templateMail);
        assertThat(controller.getParams()).hasSize(1);
    }

    @Test
    void updateAccepteLesChampsPersonnalisesConnus() {
        when(paramMailJpaRepository.findAll()).thenReturn(List.of(param("etudiant.nom"), param("convention.numero")));
        TemplateMail templateMail = new TemplateMail();
        when(templateMailJpaRepository.findById(7)).thenReturn(templateMail);

        TemplateMailFormDto dto = new TemplateMailFormDto();
        dto.setLibelle("Validation");
        dto.setObjet("Convention ${convention.numero}");
        dto.setTexte("Bonjour ${etudiant.nom}");

        TemplateMail resultat = controller.update(7, dto);

        assertThat(resultat.getLibelle()).isEqualTo("Validation");
        assertThat(resultat.getObjet()).contains("convention.numero");
        assertThat(resultat.getTexte()).contains("etudiant.nom");
    }

    @Test
    void updateRefuseLesChampsPersonnalisesInconnus() {
        when(paramMailJpaRepository.findAll()).thenReturn(List.of(param("etudiant.nom")));

        TemplateMailFormDto dto = new TemplateMailFormDto();
        dto.setLibelle("Validation");
        dto.setObjet("Objet ${champ.inconnu}");
        dto.setTexte("Texte");

        assertThatThrownBy(() -> controller.update(7, dto)).isInstanceOf(AppException.class);
    }

    @Test
    void updateExigeUnTemplateExistant() {
        when(paramMailJpaRepository.findAll()).thenReturn(List.of());
        when(templateMailJpaRepository.findById(99)).thenReturn(null);

        TemplateMailFormDto dto = new TemplateMailFormDto();
        dto.setLibelle("x");
        dto.setObjet("objet simple");
        dto.setTexte("texte simple");

        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
    }
}
