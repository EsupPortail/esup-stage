package org.esup_portail.esup_stage.service.proprety;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyCryptoServiceTest {

    private static final String CLE_16_OCTETS =
            Base64.getEncoder().encodeToString("0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private PropertyCryptoService serviceAvecCle(String cle) {
        AppliProperties properties = new AppliProperties();
        properties.setConfigEncryptionKey(cle);
        return new PropertyCryptoService(properties);
    }

    @Test
    void chiffreEtDechiffreEnAllerRetour() {
        PropertyCryptoService service = serviceAvecCle(CLE_16_OCTETS);

        String chiffre = service.encrypt("mot-de-passe-secret");

        assertThat(chiffre).isNotNull().isNotEqualTo("mot-de-passe-secret");
        assertThat(service.decrypt(chiffre)).isEqualTo("mot-de-passe-secret");
    }

    @Test
    void deuxChiffrementsDuMemeTexteDiffèrent() {
        PropertyCryptoService service = serviceAvecCle(CLE_16_OCTETS);

        // IV aléatoire : le même texte ne produit jamais deux fois le même chiffré
        assertThat(service.encrypt("texte")).isNotEqualTo(service.encrypt("texte"));
    }

    @Test
    void valeursVidesRenvoientNull() {
        PropertyCryptoService service = serviceAvecCle(CLE_16_OCTETS);

        assertThat(service.encrypt(null)).isNull();
        assertThat(service.encrypt("  ")).isNull();
        assertThat(service.decrypt(null)).isNull();
        assertThat(service.decrypt("")).isNull();
    }

    @Test
    void chiffrementSansCleEchoueExplicitement() {
        PropertyCryptoService service = serviceAvecCle(null);

        assertThatThrownBy(() -> service.encrypt("secret"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void dechiffrementSansCleRenvoieNull() {
        PropertyCryptoService service = serviceAvecCle(null);

        assertThat(service.decrypt("bm9pbXBvcnRl")).isNull();
    }

    @Test
    void cleBase64InvalideEstTraiteeCommeAbsente() {
        PropertyCryptoService service = serviceAvecCle("%%%pas-du-base64%%%");

        assertThat(service.decrypt("bm9pbXBvcnRl")).isNull();
    }

    @Test
    void cleDeTailleInvalideEstTraiteeCommeAbsente() {
        String cleTropCourte = Base64.getEncoder().encodeToString("court".getBytes(StandardCharsets.UTF_8));
        PropertyCryptoService service = serviceAvecCle(cleTropCourte);

        assertThat(service.decrypt("bm9pbXBvcnRl")).isNull();
    }

    @Test
    void payloadTropCourtEstRejete() {
        PropertyCryptoService service = serviceAvecCle(CLE_16_OCTETS);
        String payloadTropCourt = Base64.getEncoder().encodeToString(new byte[5]);

        assertThatThrownBy(() -> service.decrypt(payloadTropCourt))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("invalide");
    }

    @Test
    void payloadAltereEstRejete() {
        PropertyCryptoService service = serviceAvecCle(CLE_16_OCTETS);
        byte[] payload = Base64.getDecoder().decode(service.encrypt("secret"));
        payload[payload.length - 1] ^= 0x01; // altération du tag GCM

        assertThatThrownBy(() -> service.decrypt(Base64.getEncoder().encodeToString(payload)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void cleDe32OctetsEstAcceptee() {
        String cle32 = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
        PropertyCryptoService service = serviceAvecCle(cle32);

        assertThat(service.decrypt(service.encrypt("aes-256"))).isEqualTo("aes-256");
    }
}
