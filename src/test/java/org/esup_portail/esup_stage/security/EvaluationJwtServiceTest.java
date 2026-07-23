package org.esup_portail.esup_stage.security;

import io.jsonwebtoken.Claims;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests unitaires du service JWT d'évaluation tuteur. On exerce l'initialisation
 * paresseuse (secret manquant / Base64 invalide), le round-trip création → validation, et
 * toutes les gardes de rejet (token manquant, expiré, malformé).
 */
class EvaluationJwtServiceTest {

    /** Secret de 32 octets (256 bits, minimum HS256) encodé en Base64 comme attendu par le service. */
    private static final String SECRET_VALIDE =
            Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    private EvaluationJwtService service(String secret) {
        AppliProperties appliProperties = new AppliProperties();
        appliProperties.setJwtSecret(secret);
        return new EvaluationJwtService(appliProperties);
    }

    private AppException expectAppException(org.assertj.core.api.ThrowableAssert.ThrowingCallable appel, HttpStatus statut) {
        AppException e = (AppException) org.assertj.core.api.Assertions.catchThrowable(appel);
        assertThat(e).isNotNull();
        assertThat(e.getHttpStatus()).isEqualTo(statut);
        return e;
    }

    @Test
    void secretAbsentRendLeServiceIndisponible() {
        EvaluationJwtService service = service("   ");

        expectAppException(
                () -> service.createToken(1, 2, Instant.now(), Instant.now().plusSeconds(60)),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void secretBase64InvalideRendLeServiceIndisponible() {
        EvaluationJwtService service = service("### pas du base64 ###");

        expectAppException(
                () -> service.parseAndValidate("peu-importe"),
                HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void tokenCreePuisValideRestitueLesClaims() {
        EvaluationJwtService service = service(SECRET_VALIDE);
        Instant emis = Instant.now();
        Instant expire = emis.plus(1, ChronoUnit.HOURS);

        String token = service.createToken(42, 7, emis, expire);
        assertThat(token).isNotBlank();

        Claims claims = service.parseAndValidate(token);
        assertThat(claims.getSubject()).isEqualTo("evaluation-tuteur");
        assertThat(claims.get("conventionId", Integer.class)).isEqualTo(42);
        assertThat(claims.get("contactId", Integer.class)).isEqualTo(7);
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void tokenManquantEstRejete() {
        EvaluationJwtService service = service(SECRET_VALIDE);

        expectAppException(() -> service.parseAndValidate(null), HttpStatus.FORBIDDEN);
        expectAppException(() -> service.parseAndValidate("  "), HttpStatus.FORBIDDEN)
                .getMessage();
    }

    @Test
    void tokenExpireEstRejete() {
        EvaluationJwtService service = service(SECRET_VALIDE);
        Instant passe = Instant.now().minus(2, ChronoUnit.HOURS);
        String tokenExpire = service.createToken(1, 2, passe, passe.plus(1, ChronoUnit.HOURS));

        AppException e = expectAppException(() -> service.parseAndValidate(tokenExpire), HttpStatus.FORBIDDEN);
        assertThat(e.getMessage()).isEqualTo("Token expiré");
    }

    @Test
    void tokenMalformeEstRejete() {
        EvaluationJwtService service = service(SECRET_VALIDE);

        AppException e = expectAppException(() -> service.parseAndValidate("abc.def.ghi"), HttpStatus.FORBIDDEN);
        assertThat(e.getMessage()).isEqualTo("Token invalide");
    }

    @Test
    void tokenSigneAvecUnAutreSecretEstRejete() {
        EvaluationJwtService emetteur = service(
                Base64.getEncoder().encodeToString("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX".getBytes(StandardCharsets.UTF_8)));
        String token = emetteur.createToken(1, 2, Instant.now(), Instant.now().plusSeconds(60));

        EvaluationJwtService verificateur = service(SECRET_VALIDE);
        expectAppException(() -> verificateur.parseAndValidate(token), HttpStatus.FORBIDDEN);
    }
}
