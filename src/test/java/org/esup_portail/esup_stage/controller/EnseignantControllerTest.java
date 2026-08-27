package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.EnseignantDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Enseignant;
import org.esup_portail.esup_stage.repository.EnseignantJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnseignantControllerTest {

    private EnseignantController controller;
    private EnseignantJpaRepository enseignantJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new EnseignantController();
        enseignantJpaRepository = mock(EnseignantJpaRepository.class);
        AppConfigService appConfigService = mock(AppConfigService.class);
        controller.enseignantJpaRepository = enseignantJpaRepository;
        controller.appConfigService = appConfigService;
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setCodeUniversite("UL");
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        when(enseignantJpaRepository.saveAndFlush(any(Enseignant.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private EnseignantDto dto() {
        EnseignantDto dto = new EnseignantDto();
        dto.setNom("Durand");
        dto.setPrenom("Paul");
        dto.setMail("paul@univ.fr");
        dto.setTel("0311111111");
        dto.setTypePersonne("Enseignant");
        dto.setUidEnseignant("pdurand");
        return dto;
    }

    @Test
    void getByIdEtGetByUid() {
        Enseignant enseignant = new Enseignant();
        when(enseignantJpaRepository.findById(7)).thenReturn(enseignant);
        assertThat(controller.getById(7)).isSameAs(enseignant);

        when(enseignantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);

        when(enseignantJpaRepository.findByUid("pdurand")).thenReturn(enseignant);
        assertThat(controller.getByUid("pdurand")).isSameAs(enseignant);
    }

    @Test
    void createRemplitLEnseignantDepuisLeDto() {
        Enseignant enseignant = controller.create(dto());

        assertThat(enseignant.getNom()).isEqualTo("Durand");
        assertThat(enseignant.getPrenom()).isEqualTo("Paul");
        assertThat(enseignant.getUidEnseignant()).isEqualTo("pdurand");
        assertThat(enseignant.getAffectation().getId().getCodeUniversite()).isEqualTo("UL");
    }

    @Test
    void updateModifieLExistant() {
        Enseignant enseignant = new Enseignant();
        when(enseignantJpaRepository.findById(7)).thenReturn(enseignant);

        Enseignant modifie = controller.update(7, dto());

        assertThat(modifie.getMail()).isEqualTo("paul@univ.fr");
        assertThatThrownBy(() -> controller.update(99, dto())).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLExistant() {
        Enseignant enseignant = new Enseignant();
        when(enseignantJpaRepository.findById(7)).thenReturn(enseignant);

        assertThat(controller.delete(7)).isTrue();
        verify(enseignantJpaRepository).delete(enseignant);

        when(enseignantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }
}
