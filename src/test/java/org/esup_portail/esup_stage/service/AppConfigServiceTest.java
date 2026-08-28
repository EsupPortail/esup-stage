package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.ConfigSignatureDto;
import org.esup_portail.esup_stage.dto.ConfigThemeDto;
import org.esup_portail.esup_stage.enums.AppConfigCodeEnum;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.AppConfig;
import org.esup_portail.esup_stage.repository.AppConfigJpaRepository;
import org.esup_portail.esup_stage.repository.AffectationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AppConfigServiceTest {

    private AppConfigService service;
    private AppConfigJpaRepository appConfigJpaRepository;
    private AffectationRepository affectationRepository;
    private SignatureProperties signatureProperties;

    @BeforeEach
    void setUp() {
        service = new AppConfigService();
        appConfigJpaRepository = mock(AppConfigJpaRepository.class);
        affectationRepository = mock(AffectationRepository.class);
        signatureProperties = mock(SignatureProperties.class);
        ReflectionTestUtils.setField(service, "appConfigJpaRepository", appConfigJpaRepository);
        ReflectionTestUtils.setField(service, "affectationRepository", affectationRepository);
        ReflectionTestUtils.setField(service, "signatureProperties", signatureProperties);
    }

    private AppConfig config(String parametres) {
        AppConfig appConfig = new AppConfig();
        appConfig.setParametres(parametres);
        return appConfig;
    }

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    @Test
    void configGeneraleAbsenteRenvoieLesDefauts() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL)).thenReturn(null);

        ConfigGeneraleDto config = service.getConfigGenerale();

        assertThat(config).isNotNull();
        // La fonctionnalité annuaire doit être désactivée tant qu'on ne l'active pas.
        assertThat(config.isActiverAnnuaireEtudiants()).isFalse();
    }

    @Test
    void configGeneraleEstDeserialiseeEtEnrichie() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL))
                .thenReturn(config("{\"typeCentre\":\"MIXTE\",\"codeUniversite\":\"UL\"}"));
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.values()[0]);

        ConfigGeneraleDto config = service.getConfigGenerale();

        assertThat(config.getTypeCentre()).isEqualTo(TypeCentreEnum.MIXTE);
        assertThat(config.getCodeUniversite()).isEqualTo("UL");
        assertThat(config.isSignatureEnabled()).isTrue();
        assertThat(config.getSignatureType()).isEqualTo(AppSignatureEnum.values()[0]);
    }

    @Test
    void configGeneraleInvalideEstRejetee() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL))
                .thenReturn(config("pas du json"));

        assertThatThrownBy(() -> service.getConfigGenerale()).isInstanceOf(AppException.class);
    }

    @Test
    void configAlerteMailEstDeserialisee() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE)).thenReturn(null);
        assertThat(service.getConfigAlerteMail()).isNotNull();

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE))
                .thenReturn(config("{\"alerteEtudiant\":{\"evalEtuRemplie\":true}}"));
        ConfigAlerteMailDto config = service.getConfigAlerteMail();
        assertThat(config.getAlerteEtudiant().isEvalEtuRemplie()).isTrue();

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.ALERTE)).thenReturn(config("<xml>"));
        assertThatThrownBy(() -> service.getConfigAlerteMail()).isInstanceOf(AppException.class);
    }

    @Test
    void configThemeSansImagesEstDeserialisee() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME)).thenReturn(null);
        assertThat(service.getConfigTheme()).isNotNull();

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.THEME))
                .thenReturn(config("{\"primaryColor\":\"#123456\"}"));
        ConfigThemeDto theme = service.getConfigTheme();
        assertThat(theme.getPrimaryColor()).isEqualTo("#123456");
    }

    @Test
    void configSignatureEstDeserialisee() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.SIGNATURE)).thenReturn(null);
        assertThat(service.getConfigSignature()).isNotNull();

        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.SIGNATURE)).thenReturn(config("{invalide"));
        assertThatThrownBy(() -> service.getConfigSignature()).isInstanceOf(AppException.class);
    }

    @Test
    void anneeUniversitaireBasculeSelonLaConfiguration() {
        // bascule au 1er septembre
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL))
                .thenReturn(config("{\"anneeBasculeJour\":1,\"anneeBasculeMois\":9}"));

        assertThat(service.getAnneeUniv(date(2026, 5, 15))).isEqualTo("2025");
        assertThat(service.getAnneeUniv(date(2026, 10, 15))).isEqualTo("2026");
        assertThat(service.getAnneeUniv()).isNotBlank();
    }

    @Test
    void libellesDAnneesUniversitaires() {
        assertThat(service.getAnneeUnivLibelle("2025")).isEqualTo("2025/2026");
        assertThat(service.getAnneeUnivFromLibelle("2025/2026")).isEqualTo("2025");
    }

    @Test
    void dateDeBasculeEstConstruiteDepuisLaConfig() {
        when(appConfigJpaRepository.findByCode(AppConfigCodeEnum.GENERAL))
                .thenReturn(config("{\"anneeBasculeJour\":15,\"anneeBasculeMois\":8}"));

        Calendar bascule = service.getDateBascule(2026);

        assertThat(bascule.get(Calendar.YEAR)).isEqualTo(2026);
        assertThat(bascule.get(Calendar.MONTH)).isEqualTo(Calendar.AUGUST);
        assertThat(bascule.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }
}
