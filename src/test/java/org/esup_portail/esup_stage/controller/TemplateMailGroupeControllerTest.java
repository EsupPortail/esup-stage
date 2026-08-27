package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.SendMailTestDto;
import org.esup_portail.esup_stage.dto.TemplateMailGroupeFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.ParamMail;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.TemplateMailGroupe;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.ParamMailJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailGroupeJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailGroupeRepository;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.MailerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemplateMailGroupeControllerTest {

    private TemplateMailGroupeController controller;
    private TemplateMailGroupeRepository templateMailGroupeRepository;
    private TemplateMailGroupeJpaRepository templateMailGroupeJpaRepository;
    private ParamMailJpaRepository paramMailJpaRepository;
    private MailerService mailerService;

    @BeforeEach
    void setUp() {
        controller = new TemplateMailGroupeController();
        templateMailGroupeRepository = mock(TemplateMailGroupeRepository.class);
        templateMailGroupeJpaRepository = mock(TemplateMailGroupeJpaRepository.class);
        paramMailJpaRepository = mock(ParamMailJpaRepository.class);
        mailerService = mock(MailerService.class);
        controller.templateMailGroupeRepository = templateMailGroupeRepository;
        controller.templateMailGroupeJpaRepository = templateMailGroupeJpaRepository;
        controller.paramMailJpaRepository = paramMailJpaRepository;
        controller.mailerService = mailerService;

        when(templateMailGroupeJpaRepository.saveAndFlush(any(TemplateMailGroupe.class))).thenAnswer(inv -> inv.getArgument(0));
        ParamMail param = new ParamMail();
        param.setCode("nomEtudiant");
        when(paramMailJpaRepository.findAll()).thenReturn(List.of(param));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private TemplateMailGroupeFormDto formulaire(String objet, String texte) {
        TemplateMailGroupeFormDto dto = new TemplateMailGroupeFormDto();
        dto.setCode("RELANCE");
        dto.setLibelle("Relance entreprise");
        dto.setObjet(objet);
        dto.setTexte(texte);
        return dto;
    }

    @Test
    void searchExportsEtGetByIdDeleguentAuxRepositories() {
        when(templateMailGroupeRepository.count("{}")).thenReturn(1L);
        when(templateMailGroupeRepository.findPaginated(1, 50, "id", "asc", "{}")).thenReturn(List.of(new TemplateMailGroupe()));
        assertThat(controller.search(1, 50, "id", "asc", "{}").getTotal()).isEqualTo(1L);

        when(templateMailGroupeRepository.exportExcel("{}", "id", "asc", "{}")).thenReturn(new byte[]{1});
        when(templateMailGroupeRepository.exportCsv("{}", "id", "asc", "{}")).thenReturn(new StringBuilder("csv"));
        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).containsExactly((byte) 1);
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");

        TemplateMailGroupe template = new TemplateMailGroupe();
        when(templateMailGroupeJpaRepository.findById(7)).thenReturn(template);
        assertThat(controller.getById(7)).isSameAs(template);
    }

    @Test
    void createValideLesChampsPersonnalises() {
        TemplateMailGroupe cree = controller.create(formulaire("Bonjour ${nomEtudiant}", "Corps du mail"));
        assertThat(cree.getCode()).isEqualTo("RELANCE");
        assertThat(cree.getObjet()).isEqualTo("Bonjour ${nomEtudiant}");

        assertThatThrownBy(() -> controller.create(formulaire("Bonjour ${inconnu}", "Corps")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("n'existe pas");
        assertThatThrownBy(() -> controller.create(formulaire("Objet", "Corps ${inconnu}")))
                .isInstanceOf(AppException.class);
    }

    @Test
    void updateModifieLeTemplateExistant() {
        TemplateMailGroupe template = new TemplateMailGroupe();
        when(templateMailGroupeJpaRepository.findById(7)).thenReturn(template);

        TemplateMailGroupe maj = controller.update(7, formulaire("Objet", "Texte"));
        assertThat(maj.getLibelle()).isEqualTo("Relance entreprise");
        assertThat(maj.getTexte()).isEqualTo("Texte");

        when(templateMailGroupeJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, formulaire("Objet", "Texte"))).isInstanceOf(AppException.class);
    }

    @Test
    void paramsEnvoiDeTestEtSuppression() {
        assertThat(controller.getParams()).hasSize(1);

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid("ges1");
        utilisateur.setLogin("ges1");
        Role role = new Role();
        role.setCode(Role.GES);
        utilisateur.setRoles(List.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
        SendMailTestDto test = new SendMailTestDto();
        assertThat(controller.testSendMail(test)).isTrue();
        verify(mailerService).sendTest(eq(test), any(Utilisateur.class));

        controller.delete(7);
        verify(templateMailGroupeJpaRepository).deleteById(7);
        verify(templateMailGroupeJpaRepository).flush();
    }
}
