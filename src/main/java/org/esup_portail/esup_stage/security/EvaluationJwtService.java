package org.esup_portail.esup_stage.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class EvaluationJwtService {


    private final AppliProperties appliProperties;
    private volatile SecretKey key;
    private volatile JwtParser parser;

    public EvaluationJwtService(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
    }

    /**
        * Initialise le parser JWT de manière thread-safe et paresseuse
     */
    private void ensureInitialized() {
        if (parser != null) return;

        synchronized (this) {
            if (parser != null) return;

            String secret = appliProperties.getJwtSecret();
            if (!StringUtils.hasText(secret)) {
                throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "JWT evaluation non configuré (appli.jwtSecret manquant)");
            }

            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
            } catch (IllegalArgumentException e) {
                throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "JWT evaluation mal configuré (Base64 invalide)");
            }

            this.key = Keys.hmacShaKeyFor(decoded);
            this.parser = Jwts.parser().verifyWith(key).build();
        }
    }

    /**
     * Crée un token JWT signé
     * @param conventionId : id de la convention
     * @param contactId : id du contact
     * @param issuedAt : date de creation
     * @param expiresAt : date d'expiration
     * @return String Token JWT
     */
    public String createToken(Integer conventionId, Integer contactId, Instant issuedAt, Instant expiresAt) {
        ensureInitialized();
        return Jwts.builder()
                .header().type("JWT").and()
                .id(UUID.randomUUID().toString())
                .subject("evaluation-tuteur")
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .claims(Map.of(
                        "conventionId", conventionId,
                        "contactId", contactId
                ))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Vérifie la validité du token
     * @param token
     * @return Claims Liste des élément du jwt
     */
    public Claims parseAndValidate(String token) {
        ensureInitialized();
        if (token == null || token.isBlank()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token manquant");
        }
        try {
            Jws<Claims> jws = parser.parseSignedClaims(token);
            Claims claims = jws.getPayload();

            return claims;
        } catch (ExpiredJwtException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token expiré");
        } catch (PrematureJwtException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token pas encore valide");
        } catch (MalformedJwtException | UnsupportedJwtException | SecurityException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide");
        } catch (JwtException e) {
            throw new AppException(HttpStatus.FORBIDDEN, "Token invalide");
        }
    }
}
