package org.esup_portail.esup_stage.service.evaluation;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.model.EvaluationTuteurToken;
import org.esup_portail.esup_stage.repository.EvaluationTuteurTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class EvaluationTuteurService {

    private static final Logger logger = LogManager.getLogger(EvaluationTuteurService.class);

    @Autowired
    private EvaluationTuteurTokenRepository tokenRepository;

    /**
     * Valide un token et le marque comme utilisé si valide
     * @param tokenValue le token à valider
     * @return le token valide ou null si invalide
     */
    @Transactional
    public EvaluationTuteurToken validateAndUseToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            logger.warn("Tentative d'accès avec token null ou vide");
            return null;
        }

        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);

            if (token == null) {
                logger.warn("Token non trouvé: {}", tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...");
                return null;
            }

            if (!token.isActive()) {
                logger.warn("Token inactif (expiré/utilisé/révoqué) pour tuteur ID: {}", token.getContact().getId());
                return null;
            }

            // Marquer le token comme utilisé
            token.setUsedAt(new Date());
            tokenRepository.save(token);

            logger.info("Token validé avec succès pour tuteur ID: {}", token.getContact().getId());
            return token;

        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return null;
        }
    }
    /**
     * Valide un token sans le marquer comme utilisé (pour usage multiple)
     * @param tokenValue le token à valider
     * @return le token valide ou null si invalide
     */
    public EvaluationTuteurToken validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return null;
        }

        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            return (token != null && token.isActive()) ? token : null;

        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return null;
        }
    }

    /**
     * Révoque un token
     * @param tokenValue le token à révoquer
     * @return true si révoqué avec succès
     */
    @Transactional
    public boolean revokeToken(String tokenValue) {
        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            if (token != null && !token.isRevoked()) {
                token.setRevokedAt(new Date());
                tokenRepository.save(token);
                logger.info("Token révoqué pour tuteur ID: {}", token.getContact().getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Erreur lors de la révocation du token", e);
            return false;
        }
    }
}
