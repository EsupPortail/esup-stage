package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigSignatureDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;
import org.esup_portail.esup_stage.model.Affectation;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.repository.AffectationJpaRepository;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.FileValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppConfigControllerTest {

    private AppConfigController controller;
    private AppConfigJpaRepository appConfigJpaRepository;
    private AffectationJpaRepository affectationJpaRepository;
    private AppConfigService appConfigService;
    private FileValidationService fileValidationService;

    @BeforeEach
    void setUp() {
        controller = new AppConfigController();
        appConfigJpaRepository = mock(AppConfigJpaRepository.class);
        affectationJpaRepository = mock(AffectationJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        fileValidationService = mock(FileValidationService.class);
        controller.appConfigJpaRepository = appConfigJpaRepository;
        controller.affectationJpaRepository = affectationJpaRepository;
        controller.appConfigService = appConfigService;
        controller.fileValidationService = fileValidationService;

        when(appConfigJpaRepository.saveAndFlush(any(AppConfig.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void laConfigGeneraleEstLueDepuisLeService() {
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        when(appConfigService.getConfigGenerale()).thenReturn(config);

        assertThat(controller.getConfigGenerale()).isSameAs(config);
        assertThat(controller.getConfigGeneraleEtu()).isSameAs(config);
    }

    @Test
    void updateGeneraleCreeLaConfigEtLAffectationSiAbsentes() throws Exception {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL)).thenReturn(null);
        when(affectationJpaRepository.findByCodeUniversite("UL")).thenReturn(List.of());
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());

        ConfigGeneraleDto dto = new ConfigGeneraleDto();
        dto.setCodeUniversite("UL");
        dto.setTypeCentre(TypeCentreEnum.VIDE);

        controller.updateGenerale(dto);

        assertThat(dto.getTypeCentre()).isNull();
        ArgumentCaptor<AppConfig> config = ArgumentCaptor.forClass(AppConfig.class);
        verify(appConfigJpaRepository).saveAndFlush(config.capture());
        assertThat(config.getValue().getCode()).isEqualTo(AppConfigCodeEnum.GENERAL);
        verify(affectationJpaRepository).saveAndFlush(any(Affectation.class));
    }

    @Test
    void updateGeneraleReutiliseLaConfigExistante() throws Exception {
        AppConfig existante = new AppConfig();
        existante.setCode(AppConfigCodeEnum.GENERAL);
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL)).thenReturn(existante);
        when(affectationJpaRepository.findByCodeUniversite("UL")).thenReturn(List.of(new Affectation()));
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());

        ConfigGeneraleDto dto = new ConfigGeneraleDto();
        dto.setCodeUniversite("UL");
        controller.updateGenerale(dto);

        verify(appConfigJpaRepository).saveAndFlush(existante);
        verify(affectationJpaRepository, never()).saveAndFlush(any(Affectation.class));
    }

    @Test
    void lAlerteMailEstLueEtEnregistree() throws Exception {
        ConfigAlerteMailDto config = new ConfigAlerteMailDto();
        when(appConfigService.getConfigAlerteMail()).thenReturn(config);
        assertThat(controller.getConfigAlerteMail()).isSameAs(config);

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE)).thenReturn(null);
        assertThat(controller.updateAlerteMail(new ConfigAlerteMailDto())).isSameAs(config);
        verify(appConfigJpaRepository).saveAndFlush(any(AppConfig.class));
    }

    @Test
    void laSignatureEstLueEtEnregistree() throws Exception {
        ConfigSignatureDto config = new ConfigSignatureDto();
        when(appConfigService.getConfigSignature()).thenReturn(config);
        assertThat(controller.getSignature()).isSameAs(config);

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.SIGNATURE)).thenReturn(null);
        assertThat(controller.updateSignature(new ConfigSignatureDto())).isSameAs(config);
        verify(appConfigJpaRepository).saveAndFlush(any(AppConfig.class));
    }

    @Test
    void leThemeEstMisAJourAvecLeLogoValide() throws Exception {
        when(appConfigService.getConfigTheme()).thenReturn(new ConfigThemeDto());
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME)).thenReturn(null);
        MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", "image/png", "PNG".getBytes(StandardCharsets.UTF_8));
        when(fileValidationService.validateImage(logo)).thenReturn(
                new FileValidationService.ValidatedImage("PNG".getBytes(StandardCharsets.UTF_8), "image/png", "png"));

        assertThat(controller.updateTheme("{}", logo, null)).isNotNull();

        verify(fileValidationService).validateImage(logo);
        verify(appConfigService).writeImageIntoFile(any(ConfigThemeDto.class));
        verify(appConfigService).updateTheme();
    }

    @Test
    void leThemeEstLuEtRestaurable() throws Exception {
        ConfigThemeDto theme = new ConfigThemeDto();
        when(appConfigService.getConfigTheme()).thenReturn(theme);
        assertThat(controller.getConfigTheme()).isSameAs(theme);

        AppConfig existante = new AppConfig();
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME)).thenReturn(existante);
        assertThat(controller.rollbackTheme()).isSameAs(theme);
        verify(appConfigJpaRepository).delete(existante);

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME)).thenReturn(null);
        assertThat(controller.rollbackTheme()).isSameAs(theme);
    }
}
