package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;
import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.repository.CentreGestionRepository;
import org.esup_portail.esup_stage.repository.NiveauCentreJpaRepository;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NiveauCentreControllerTest {

    private NiveauCentreController controller;
    private NiveauCentreJpaRepository niveauCentreJpaRepository;
    private CentreGestionRepository centreGestionRepository;
    private AppConfigService appConfigService;

    @BeforeEach
    void setUp() {
        controller = new NiveauCentreController();
        niveauCentreJpaRepository = mock(NiveauCentreJpaRepository.class);
        centreGestionRepository = mock(CentreGestionRepository.class);
        appConfigService = mock(AppConfigService.class);

        controller.niveauCentreJpaRepository = niveauCentreJpaRepository;
        controller.centreGestionRepository = centreGestionRepository;
        controller.appConfigService = appConfigService;
    }

    @Test
    void findListReturnsConfiguredLevelsExceptEtablissementWhenEtablissementCentreAlreadyExists() {
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setTypeCentre(TypeCentreEnum.MIXTE);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        when(niveauCentreJpaRepository.getListMixte()).thenReturn(List.of(
                niveau(2, "ETABLISSEMENT"),
                niveau(3, "ETAPE"),
                niveau(4, "UFR")
        ));
        when(centreGestionRepository.etablissementExists()).thenReturn(true);

        List<NiveauCentre> result = controller.findList();

        assertThat(result).extracting(NiveauCentre::getLibelle)
                .containsExactly("ETAPE", "UFR");
    }

    @Test
    void findListReturnsOnlyEtablissementWhenNoEtablissementCentreExists() {
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setTypeCentre(TypeCentreEnum.MIXTE);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        when(niveauCentreJpaRepository.getListMixte()).thenReturn(List.of(
                niveau(2, "ETABLISSEMENT"),
                niveau(3, "ETAPE"),
                niveau(4, "UFR")
        ));
        when(centreGestionRepository.etablissementExists()).thenReturn(false);

        List<NiveauCentre> result = controller.findList();

        assertThat(result).extracting(NiveauCentre::getLibelle)
                .containsExactly("ETABLISSEMENT");
    }

    private NiveauCentre niveau(int id, String libelle) {
        NiveauCentre niveauCentre = new NiveauCentre();
        niveauCentre.setId(id);
        niveauCentre.setLibelle(libelle);
        niveauCentre.setTemEnServ("O");
        return niveauCentre;
    }
}
