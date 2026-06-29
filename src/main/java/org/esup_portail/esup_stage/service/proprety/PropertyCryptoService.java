package org.esup_portail.esup_stage.service.proprety;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PropertyCryptoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCryptoService.class);
    private static final String ALGO = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;
    private static final int TAG_LEN_BITS = 128;

    private final AppliProperties appliProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AtomicBoolean keyErrorLogged = new AtomicBoolean(false);

    public PropertyCryptoService(AppliProperties appliProperties) {
        this.appliProperties = appliProperties;
    }

    public String encrypt(String plain) {
        if (!StringUtils.hasText(plain)) {
            return null;
        }
        SecretKeySpec key = getKeyOrNull();
        if (key == null) {
            throw new AppException(HttpStatus.SERVICE_UNAVAILABLE, "Impossible de chiffrer sans appli.configEncryptionKey");
        }
        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(payload);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur chiffrement configuration");
        }
    }

    public String decrypt(String encryptedBase64) {
        if (!StringUtils.hasText(encryptedBase64)) {
            return null;
        }
        SecretKeySpec key = getKeyOrNull();
        if (key == null) {
            return null;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(encryptedBase64);
            if (payload.length <= IV_LEN) {
                throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Chiffrement configuration invalide");
            }
            byte[] iv = new byte[IV_LEN];
            byte[] ciphertext = new byte[payload.length - IV_LEN];
            System.arraycopy(payload, 0, iv, 0, IV_LEN);
            System.arraycopy(payload, IV_LEN, ciphertext, 0, ciphertext.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN_BITS, iv));
            byte[] plain = cipher.doFinal(ciphertext);
            return new String(plain, java.nio.charset.StandardCharsets.UTF_8);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur d\u00e9chiffrement configuration");
        }
    }

    private SecretKeySpec getKeyOrNull() {
        String keyBase64 = appliProperties.getConfigEncryptionKey();
        if (!StringUtils.hasText(keyBase64)) {
            logKeyErrorOnce("Cle de chiffrement absente (appli.configEncryptionKey)");
            return null;
        }
        byte[] key;
        try {
            key = Base64.getDecoder().decode(keyBase64);
        } catch (IllegalArgumentException e) {
            logKeyErrorOnce("Cle de chiffrement invalide (Base64)");
            return null;
        }
        if (!(key.length == 16 || key.length == 24 || key.length == 32)) {
            logKeyErrorOnce("Cle de chiffrement invalide (taille)");
            return null;
        }
        return new SecretKeySpec(key, ALGO);
    }

    private void logKeyErrorOnce(String reason) {
        if (keyErrorLogged.compareAndSet(false, true)) {
            LOGGER.error(reason);
        }
    }
}
