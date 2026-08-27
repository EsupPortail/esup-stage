package org.esup_portail.esup_stage.controller.admin;

import org.esup_portail.esup_stage.dto.AppPropertyDto;
import org.esup_portail.esup_stage.dto.ConfigTestResultDto;
import org.esup_portail.esup_stage.dto.MailerTestRequestDto;
import org.esup_portail.esup_stage.model.AppProperty;
import org.esup_portail.esup_stage.service.AdminService;
import org.esup_portail.esup_stage.service.proprety.AppProperyService;
import org.esup_portail.esup_stage.service.proprety.ConfigMissingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur d'administration de la configuration. On vérifie la
 * délégation systématique après {@code requireAdmin()}, et surtout la projection en DTO
 * qui masque la valeur des propriétés secrètes.
 */
class AppPropertyControllerTest {

    private AppPropertyController controller;
    private ConfigMissingService configMissingService;
    private AppProperyService appProperyService;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        controller = new AppPropertyController();
        configMissingService = mock(ConfigMissingService.class);
        appProperyService = mock(AppProperyService.class);
        adminService = mock(AdminService.class);
        ReflectionTestUtils.setField(controller, "configMissingService", configMissingService);
        ReflectionTestUtils.setField(controller, "appProperyService", appProperyService);
        ReflectionTestUtils.setField(controller, "adminService", adminService);
    }

    private AppProperty prop(String key, String value, boolean secret, String valueEncrypted) {
        AppProperty prop = new AppProperty();
        prop.setKey(key);
        prop.setValue(value);
        prop.setIsSecret(secret);
        prop.setValueEncrypted(valueEncrypted);
        return prop;
    }

    @Test
    void getMissingRenvoieLesClesManquantes() {
        when(configMissingService.getMissingKeys()).thenReturn(List.of("smtp.host", "sirene.token"));

        var resultat = controller.getMissing();

        assertThat(resultat).containsEntry("missing", List.of("smtp.host", "sirene.token"));
        verify(adminService).requireAdmin();
    }

    @Test
    void getAllPropertiesMasqueLaValeurDesProprietesSecretes() {
        when(appProperyService.getAll()).thenReturn(List.of(
                prop("clair.key", "visible", false, null),
                prop("secret.chiffre", null, true, "xxxxx"),
                prop("secret.vide", null, true, null)
        ));

        List<AppPropertyDto> dtos = controller.getAllProperties();

        AppPropertyDto clair = dtos.get(0);
        assertThat(clair.getValue()).isEqualTo("visible");
        assertThat(clair.getHasValue()).isTrue();

        AppPropertyDto secretRenseigne = dtos.get(1);
        assertThat(secretRenseigne.getValue()).as("valeur secrète jamais exposée").isNull();
        assertThat(secretRenseigne.getHasValue()).isTrue();

        AppPropertyDto secretVide = dtos.get(2);
        assertThat(secretVide.getValue()).isNull();
        assertThat(secretVide.getHasValue()).isFalse();
    }

    @Test
    void updatePropertiesIgnoreLesEntreesNullesOuSansCle() {
        AppPropertyDto valide = new AppPropertyDto();
        valide.setKey("smtp.host");
        valide.setValue("localhost");
        AppPropertyDto sansCle = new AppPropertyDto();
        sansCle.setKey("  ");
        List<AppPropertyDto> requete = java.util.Arrays.asList(valide, sansCle, null);
        when(appProperyService.getAll()).thenReturn(List.of());

        controller.updateProperties(requete);

        verify(appProperyService).save("smtp.host", "localhost");
        verify(appProperyService, never()).save(eqBlank(), any());
    }

    @Test
    void updatePropertiesToléreUneListeNulle() {
        when(appProperyService.getAll()).thenReturn(List.of());

        assertThat(controller.updateProperties(null)).isEmpty();
        verify(appProperyService, never()).save(any(), any());
    }

    @Test
    void testMailerDelegueAuService() {
        ConfigTestResultDto attendu = new ConfigTestResultDto();
        MailerTestRequestDto requete = new MailerTestRequestDto();
        when(appProperyService.testMailer(requete)).thenReturn(attendu);

        assertThat(controller.testMailer(requete)).isSameAs(attendu);
        verify(adminService).requireAdmin();
    }

    private static String eqBlank() {
        return org.mockito.ArgumentMatchers.eq("  ");
    }
}
