package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Contrôles appliqués aux fichiers de libellés déposés par un administrateur :
 * ces valeurs finissent telles quelles dans le HTML converti en PDF.
 */
class FileValidationServicePropertiesTest {

    private static final Set<String> CLES = Set.of("protectionSociale.oui.libelle", "protectionSociale.non.texte");

    private final FileValidationService service = new FileValidationService();

    private MockMultipartFile fichier(String nom, byte[] contenu) {
        return new MockMultipartFile("fichier", nom, "text/plain", contenu);
    }

    private MockMultipartFile fichier(String contenu) {
        return fichier("impression_en.properties", contenu.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void accepteUnFichierValide() {
        FileValidationService.ValidatedProperties valide = service.validateProperties(
                fichier("protectionSociale.oui.libelle=YES\nprotectionSociale.non.texte=If no box is ticked, 6.3-1 applies.\n"), CLES);

        assertThat(valide.proprietes().getProperty("protectionSociale.oui.libelle")).isEqualTo("YES");
        assertThat(valide.bytes()).isNotEmpty();
    }

    @Test
    void accepteLesValeursVides() {
        FileValidationService.ValidatedProperties valide = service.validateProperties(
                fichier("protectionSociale.oui.libelle=\nprotectionSociale.non.texte=\n"), CLES);

        assertThat(valide.proprietes().getProperty("protectionSociale.non.texte")).isEmpty();
    }

    @Test
    void accepteLesAccentsEnUtf8() {
        FileValidationService.ValidatedProperties valide = service.validateProperties(
                fichier("protectionSociale.non.texte=La cobertura se deriva entonces exclusivamente del régimen francés.\n"), CLES);

        assertThat(valide.proprietes().getProperty("protectionSociale.non.texte")).isEqualTo("La cobertura se deriva entonces exclusivamente del régimen francés.");
    }

    @Test
    void refuseUnFichierAbsentOuVide() {
        assertThatThrownBy(() -> service.validateProperties(null, CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("aucun fichier");
        assertThatThrownBy(() -> service.validateProperties(fichier(""), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("vide");
    }

    @Test
    void refuseUneAutreExtension() {
        assertThatThrownBy(() -> service.validateProperties(
                fichier("impression_en.txt", "protectionSociale.non.texte=x".getBytes(StandardCharsets.UTF_8)), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(".properties");
    }

    @Test
    void refuseUnFichierTropVolumineux() {
        byte[] trop = new byte[65 * 1024];
        java.util.Arrays.fill(trop, (byte) '#');

        assertThatThrownBy(() -> service.validateProperties(fichier("impression_en.properties", trop), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("limite autorisée");
    }

    @Test
    void refuseUnFichierQuiNestPasEnUtf8() {
        byte[] latin1 = "protectionSociale.non.texte=appliquée à l'étranger\n".getBytes(StandardCharsets.ISO_8859_1);

        assertThatThrownBy(() -> service.validateProperties(fichier("impression_en.properties", latin1), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("UTF-8");
    }

    @Test
    void refuseUneCleInconnue() {
        assertThatThrownBy(() -> service.validateProperties(fichier("protectionSociale.oui=YES\n"), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("protectionSociale.oui");
    }

    @Test
    void refuseUneValeurContenantDuHtml() {
        assertThatThrownBy(() -> service.validateProperties(fichier("protectionSociale.non.texte=<b>x</b>\n"), CLES))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("texte pur");
    }

    @Test
    void renvoieUneErreurFonctionnelle() {
        AppException exception = (AppException) org.assertj.core.api.Assertions.catchThrowable(
                () -> service.validateProperties(fichier("cle.inconnue=x\n"), CLES));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).startsWith("Fichier de libellés refusé :");
    }
}
