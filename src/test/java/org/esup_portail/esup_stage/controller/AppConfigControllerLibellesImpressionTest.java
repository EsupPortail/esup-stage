package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.LibelleImpressionLangueDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.LangueConvention;
import org.esup_portail.esup_stage.repository.LangueConventionJpaRepository;
import org.esup_portail.esup_stage.service.FileValidationService;
import org.esup_portail.esup_stage.service.impression.LibelleImpressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Gestion des fichiers de traduction des libellés d'impression depuis les paramètres généraux.
 */
class AppConfigControllerLibellesImpressionTest {

    private static final String CLE_OUI = "protectionSociale.oui.libelle";

    @TempDir
    Path dataDir;

    private AppConfigController controller;
    private LibelleImpressionService libelleImpressionService;

    @BeforeEach
    void setUp() {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setDataDir(dataDir.toString());

        libelleImpressionService = new LibelleImpressionService(appliProperties);

        LangueConventionJpaRepository langueConventionJpaRepository = mock(LangueConventionJpaRepository.class);
        when(langueConventionJpaRepository.findAll()).thenReturn(List.of(langue("fr", "Français", "O"), langue("en", "Anglais", "O"), langue("fo", "Francophone", "N")));
        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(langue("fr", "Français", "O"));
        when(langueConventionJpaRepository.findByCode("en")).thenReturn(langue("en", "Anglais", "O"));

        controller = new AppConfigController();
        controller.langueConventionJpaRepository = langueConventionJpaRepository;
        controller.libelleImpressionService = libelleImpressionService;
        controller.fileValidationService = new FileValidationService();
    }

    private LangueConvention langue(String code, String libelle, String temEnServ) {
        LangueConvention langue = new LangueConvention();
        langue.setCode(code);
        langue.setLibelle(libelle);
        langue.setTemEnServ(temEnServ);
        return langue;
    }

    private MockMultipartFile fichier(String nom, String contenu) {
        return new MockMultipartFile("fichier", nom, "text/plain", contenu.getBytes(StandardCharsets.UTF_8));
    }

    private Path cheminSurcharge(String code) {
        return dataDir.resolve("i18n").resolve("impression_" + code + ".properties");
    }

    @Test
    void listeLesLanguesActivesAvecLeurEtatDeTraduction() {
        List<LibelleImpressionLangueDto> langues = controller.getLibellesImpression();

        assertThat(langues).extracting(LibelleImpressionLangueDto::getCode).containsExactly("fr", "en");
        LibelleImpressionLangueDto francais = langues.get(0);
        assertThat(francais.getNbClesRenseignees()).isEqualTo(francais.getNbClesTotal()).isPositive();
        assertThat(francais.isSurcharge()).isFalse();
        assertThat(langues.get(1).getNbClesRenseignees()).isZero();
    }

    @Test
    void signaleLaPresenceDUnFichierDepose() {
        controller.updateLibellesImpression("en", fichier("impression_en.properties", CLE_OUI + "=YES\n"));

        LibelleImpressionLangueDto anglais = controller.getLibellesImpression().get(1);
        assertThat(anglais.isSurcharge()).isTrue();
        assertThat(anglais.getDateModification()).isNotNull();
        assertThat(anglais.getNbClesRenseignees()).isEqualTo(1);
    }

    @Test
    void proposeUnFichierDeTravailNommeSelonLaLangue() {
        ResponseEntity<byte[]> reponse = controller.getFichierLibellesImpression("en");

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).contains("impression_en.properties");
        assertThat(new String(reponse.getBody(), StandardCharsets.UTF_8)).contains(CLE_OUI);
    }

    @Test
    void leDepotEstPrisEnCompteImmediatement() {
        assertThat(libelleImpressionService.getLibelles("en").get(CLE_OUI)).isEqualTo("OUI");

        controller.updateLibellesImpression("en", fichier("impression_en.properties", CLE_OUI + "=YES\n"));

        assertThat(libelleImpressionService.getLibelles("en").get(CLE_OUI)).isEqualTo("YES");
    }

    @Test
    void leNomDuFichierDeposeEstIgnore() throws Exception {
        controller.updateLibellesImpression("en", fichier("../../impression_fr.properties", CLE_OUI + "=YES\n"));

        assertThat(cheminSurcharge("en")).exists();
        assertThat(cheminSurcharge("fr")).doesNotExist();
        assertThat(Files.readString(cheminSurcharge("en"), StandardCharsets.UTF_8)).isEqualTo(CLE_OUI + "=YES\n");
    }

    @Test
    void unFichierInvalideNestPasEcrit() {
        assertThatThrownBy(() -> controller.updateLibellesImpression("en", fichier("impression_en.properties", "cle.inconnue=x\n")))
                .isInstanceOf(AppException.class);

        assertThat(cheminSurcharge("en")).doesNotExist();
    }

    @Test
    void laSuppressionRetablitLeComportementLivre() {
        controller.updateLibellesImpression("en", fichier("impression_en.properties", CLE_OUI + "=YES\n"));

        controller.deleteLibellesImpression("en");

        assertThat(cheminSurcharge("en")).doesNotExist();
        assertThat(libelleImpressionService.getLibelles("en").get(CLE_OUI)).isEqualTo("OUI");
    }

    @Test
    void laSuppressionEstSansEffetSansFichierDepose() {
        assertThat(controller.deleteLibellesImpression("en")).isNotEmpty();
    }

    @Test
    void refuseUnCodeDeLangueInconnu() {
        assertThatThrownBy(() -> controller.getFichierLibellesImpression("xx"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("non trouvée");
        assertThatThrownBy(() -> controller.deleteLibellesImpression(null))
                .isInstanceOf(AppException.class);
        assertThatThrownBy(() -> controller.updateLibellesImpression("xx", fichier("impression_xx.properties", CLE_OUI + "=X\n")))
                .isInstanceOf(AppException.class);
    }
}
