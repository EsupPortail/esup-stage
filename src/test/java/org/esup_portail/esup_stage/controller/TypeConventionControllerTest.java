package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.RegimeInscriptionDto;
import org.esup_portail.esup_stage.dto.TypeConventionFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contenu;
import org.esup_portail.esup_stage.model.RegimeInscriptionApogee;
import org.esup_portail.esup_stage.model.TypeConvention;
import org.esup_portail.esup_stage.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TypeConventionControllerTest {

    private TypeConventionController controller;
    private TypeConventionRepository typeConventionRepository;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private ContenuJpaRepository contenuJpaRepository;
    private TemplateConventionJpaRepository templateConventionJpaRepository;
    private RegimeInscriptionApogeeJpaRepository regimeInscriptionApogeeJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new TypeConventionController();
        typeConventionRepository = mock(TypeConventionRepository.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        contenuJpaRepository = mock(ContenuJpaRepository.class);
        templateConventionJpaRepository = mock(TemplateConventionJpaRepository.class);
        regimeInscriptionApogeeJpaRepository = mock(RegimeInscriptionApogeeJpaRepository.class);
        controller.typeConventionRepository = typeConventionRepository;
        controller.typeConventionJpaRepository = typeConventionJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.contenuJpaRepository = contenuJpaRepository;
        controller.templateConventionJpaRepository = templateConventionJpaRepository;
        controller.regimeInscriptionApogeeJpaRepository = regimeInscriptionApogeeJpaRepository;

        when(typeConventionJpaRepository.saveAndFlush(any(TypeConvention.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void searchEtExportsDeleguentAuRepository() {
        when(typeConventionRepository.count("{}")).thenReturn(1L);
        when(typeConventionRepository.findPaginated(1, 50, "id", "asc", "{}")).thenReturn(List.of(new TypeConvention()));
        assertThat(controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse()).getTotal()).isEqualTo(1L);

        when(typeConventionRepository.exportExcel("{}", "id", "asc", "{}")).thenReturn(new byte[]{1});
        when(typeConventionRepository.exportCsv("{}", "id", "asc", "{}")).thenReturn(new StringBuilder("csv"));
        assertThat(controller.exportExcel("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).containsExactly((byte) 1);
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", new MockHttpServletResponse()).getBody()).isEqualTo("csv");
    }

    @Test
    void createRefuseLesCodesExistants() {
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setCodeCtrl("STAGE");

        when(typeConventionRepository.exists("STAGE", 0)).thenReturn(false);
        TypeConvention cree = controller.create(typeConvention);
        assertThat(cree.getTemEnServ()).isEqualTo("O");
        assertThat(cree.getModifiable()).isTrue();

        when(typeConventionRepository.exists("STAGE", 0)).thenReturn(true);
        Contenu contenu = new Contenu();
        contenu.setTexte("Code déjà existant");
        when(contenuJpaRepository.findByCode("NOMENCLATURE_CODE_EXISTANT")).thenReturn(contenu);
        assertThatThrownBy(() -> controller.create(typeConvention))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Code déjà existant");
    }

    @Test
    void updateResoutLesRegimesDInscription() {
        TypeConvention typeConvention = new TypeConvention();
        when(typeConventionJpaRepository.findById(7)).thenReturn(typeConvention);

        RegimeInscriptionApogee regime = new RegimeInscriptionApogee();
        regime.setCode("FI");
        when(regimeInscriptionApogeeJpaRepository.findAllById(any())).thenReturn(List.of(regime));

        TypeConventionFormDto dto = new TypeConventionFormDto();
        dto.setLibelle("Stage initial");
        dto.setTemEnServ("O");
        RegimeInscriptionDto regimeDto = new RegimeInscriptionDto();
        regimeDto.setCode(" FI ");
        dto.setRegimesInscription(List.of(regimeDto));

        TypeConvention maj = controller.update(7, dto);

        assertThat(maj.getLibelle()).isEqualTo("Stage initial");
        assertThat(maj.getTemEnServ()).isEqualTo("O");
        assertThat(maj.getRegimesInscription()).containsExactly(regime);
    }

    @Test
    void updateAccepteLeFallbackTypeInscriptionEtRefuseLesRegimesInconnus() {
        TypeConvention typeConvention = new TypeConvention();
        when(typeConventionJpaRepository.findById(7)).thenReturn(typeConvention);

        // fallback sur typeInscription quand regimesInscription est null
        RegimeInscriptionApogee regime = new RegimeInscriptionApogee();
        regime.setCode("FC");
        when(regimeInscriptionApogeeJpaRepository.findAllById(any())).thenReturn(List.of(regime));
        TypeConventionFormDto fallback = new TypeConventionFormDto();
        fallback.setLibelle("Stage FC");
        RegimeInscriptionDto regimeDto = new RegimeInscriptionDto();
        regimeDto.setCode("FC");
        fallback.setTypeInscription(List.of(regimeDto));
        assertThat(controller.update(7, fallback).getRegimesInscription()).containsExactly(regime);

        // régime inconnu en base
        when(regimeInscriptionApogeeJpaRepository.findAllById(any())).thenReturn(List.of());
        TypeConventionFormDto inconnu = new TypeConventionFormDto();
        inconnu.setLibelle("Stage");
        RegimeInscriptionDto inconnuDto = new RegimeInscriptionDto();
        inconnuDto.setCode("XX");
        inconnu.setRegimesInscription(List.of(inconnuDto));
        assertThatThrownBy(() -> controller.update(7, inconnu))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("inconnu");

        // sans régime : libellé seul
        TypeConventionFormDto simple = new TypeConventionFormDto();
        simple.setLibelle("Renommé");
        assertThat(controller.update(7, simple).getLibelle()).isEqualTo("Renommé");
    }

    @Test
    void deleteRefuseSiDesConventionsOuTemplatesExistent() {
        when(conventionJpaRepository.countConventionWithTypeConvention(7)).thenReturn(1L);
        assertThatThrownBy(() -> controller.delete(7))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("conventions");

        when(conventionJpaRepository.countConventionWithTypeConvention(7)).thenReturn(0L);
        when(templateConventionJpaRepository.countTemplateWithTypeConvention(7)).thenReturn(1L);
        assertThatThrownBy(() -> controller.delete(7))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("templates");

        when(templateConventionJpaRepository.countTemplateWithTypeConvention(7)).thenReturn(0L);
        controller.delete(7);
        verify(typeConventionJpaRepository).deleteById(7);
        verify(typeConventionJpaRepository).flush();
    }
}
