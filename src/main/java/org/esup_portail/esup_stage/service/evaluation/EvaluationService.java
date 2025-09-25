package org.esup_portail.esup_stage.service.evaluation;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.List;
import java.time.Duration;

@Service
public class EvaluationService {

    private static final Logger logger = LogManager.getLogger(EvaluationService.class);

    @Autowired
    private EvaluationTuteurTokenJpaRepository tokenRepository;

    @Autowired
    private ConventionJpaRepository conventionJpaRepository;

    @Autowired
    private QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;

    @Autowired
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    private FicheEvaluationJpaRepository ficheEvaluationJpaRepository;

    @Autowired
    private AppliProperties appliProperties;

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
            logger.info("oui oui oui !");
            if (token == null) {
                logger.warn("Token non trouvé: {}", tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...");
                return null;
            }

            if (!token.isActive()) {
                logger.warn("Token inactif (expiré/utilisé/révoqué) pour tuteur ID: {}", token.getContact().getId());
                return null;
            }

            // Marquer le token comme utilisé
            token.setUtilise(true);
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
     * Valide que le token existe et est déjà utilisé
     * @param tokenValue le token à valider
     * @return le token valide ou null si invalide
     */
    public EvaluationTuteurToken validateUsedToken(String tokenValue) {
        if (tokenValue == null || tokenValue.trim().isEmpty()) {
            return null;
        }

        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            return (token != null && !token.isExpired() && token.getUtilise() && !token.getRevoque()) ? token : null;

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
            if (token != null && !token.getRevoque()) {
                token.setRevoque(true);
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

    /**
     * Crée l'url de l'évaluation pour le tuteur à partir du token
     * @param tokenValue String
     * @return l'url de l'évaluation pour le tuteur
     */
    public String buildEvaluationTuteurUrl(String tokenValue) {

        if (tokenValue == null || tokenValue.isBlank()) {
            logger.warn("Impossible de construire l'URL d'évaluation : token manquant");
            return "";
        }
        return UriComponentsBuilder
                .fromHttpUrl(appliProperties.getPrefix())
                .path("frontend/#/evaluation-tuteur")
                .queryParam("token", tokenValue)
                .build()
                .toUriString();
    }

     /**
     * Crée l'url de l'évaluation pour le tuteur à partir d'une convention
     * Crée automatiquement un token si aucun token actif n'existe
     * @param convention Convention
     * @return l'url de l'évaluation pour le tuteur
     */
    @Transactional
    public String buildEvaluationTuteurUrl(Convention convention) {
        if (convention == null || convention.getContact() == null) {
            return "";
        }

        // Chercher un token actif existant
        List<EvaluationTuteurToken> tokens = tokenRepository.findByTuteurId(convention.getContact().getId());
        EvaluationTuteurToken tokenActif = null;

        if (tokens != null && !tokens.isEmpty()) {
            for (EvaluationTuteurToken t : tokens) {
                if (t != null && t.isActive()) {
                    tokenActif = t;
                    break;
                }
            }
        }

        // Si aucun token actif trouvé, en créer un nouveau
        if (tokenActif == null) {
            tokenActif = createToken(convention);
        }

        // Si on n'arrive toujours pas à avoir un token (erreur de création), retourner vide
        if (tokenActif == null) {
            logger.warn("Impossible de créer ou trouver un token actif pour tuteur ID: {}", convention.getContact().getId());
            return "";
        }

        return buildEvaluationTuteurUrl(tokenActif.getToken());
    }


    /**
     * Creation d'un token pour une convention
     * @param convention Convention
     * @return token EvaluationTuteurToken
     */
    @Transactional
    public EvaluationTuteurToken createToken(Convention convention) {
        if (convention == null || convention.getContact() == null) {
            logger.warn("Impossible de créer un token : convention ou contact manquant");
            return null;
        }

        try {
            // Vérifier s'il existe déjà un token actif pour ce tuteur
            List<EvaluationTuteurToken> existingTokens = tokenRepository.findByTuteurId(convention.getContact().getId());
            for (EvaluationTuteurToken existingToken : existingTokens) {
                if (existingToken.isActive()) {
                    logger.info("Token actif existant trouvé pour tuteur ID: {}", convention.getContact().getId());
                    return existingToken;
                }
            }

            EvaluationTuteurToken newToken = new EvaluationTuteurToken(convention,convention.getContact(),Duration.ofDays(3));
            EvaluationTuteurToken savedToken = tokenRepository.save(newToken);
            logger.info("Nouveau token créé pour tuteur ID: {}", convention.getContact().getId());

            return savedToken;

        } catch (Exception e) {
            logger.error("Erreur lors de la création du token pour tuteur ID: " + convention.getContact().getId(), e);
            return null;
        }
    }


    public FicheEvaluation getByCentreGestion(Integer id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findByCentreGestion(id);
        if (ficheEvaluation == null) {
            ficheEvaluation = new FicheEvaluation();
            CentreGestion centreGestion = centreGestionJpaRepository.findById(id).orElse(null);
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
            }
            ficheEvaluation.setCentreGestion(centreGestion);
            return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
        }
        return ficheEvaluation;
    }

    public ReponseEvaluation initReponseEvaluation(int id) {

        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        FicheEvaluation ficheEvaluation = getByCentreGestion(convention.getCentreGestion().getId());

        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }

        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();

        ReponseEvaluationId reponseEvaluationId = new ReponseEvaluationId();
        reponseEvaluationId.setIdConvention(convention.getId());
        reponseEvaluationId.setIdFicheEvaluation(ficheEvaluation.getId());

        reponseEvaluation.setReponseEvaluationId(reponseEvaluationId);
        reponseEvaluation.setFicheEvaluation(ficheEvaluation);
        reponseEvaluation.setConvention(convention);

        return reponseEvaluation;
    }

    public ReponseSupplementaire initReponseSupplementaire(int idConvention, int idQestion) {

        Convention convention = conventionJpaRepository.findById(idConvention);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        QuestionSupplementaire questionSupplementaire = questionSupplementaireJpaRepository.findById(idQestion);

        if (questionSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "QuestionSupplementaire non trouvée");
        }

        ReponseSupplementaire reponseSupplementaire = new ReponseSupplementaire();

        ReponseSupplementaireId reponseSupplementaireId = new ReponseSupplementaireId();
        reponseSupplementaireId.setIdConvention(convention.getId());
        reponseSupplementaireId.setIdQuestionSupplementaire(questionSupplementaire.getId());

        reponseSupplementaire.setId(reponseSupplementaireId);
        reponseSupplementaire.setQuestionSupplementaire(questionSupplementaire);
        reponseSupplementaire.setConvention(convention);

        return reponseSupplementaire;
    }

    public void setFicheEtudiantData(FicheEvaluation ficheEvaluation, FicheEtudiantDto ficheEtudiantDto) {
        ficheEvaluation.setQuestionEtuI1(ficheEtudiantDto.isQuestionEtuI1());
        ficheEvaluation.setQuestionEtuI2(ficheEtudiantDto.isQuestionEtuI2());
        ficheEvaluation.setQuestionEtuI3(ficheEtudiantDto.isQuestionEtuI3());
        ficheEvaluation.setQuestionEtuI4(ficheEtudiantDto.isQuestionEtuI4());
        ficheEvaluation.setQuestionEtuI5(ficheEtudiantDto.isQuestionEtuI5());
        ficheEvaluation.setQuestionEtuI6(ficheEtudiantDto.isQuestionEtuI6());
        ficheEvaluation.setQuestionEtuI7(ficheEtudiantDto.isQuestionEtuI7());
        ficheEvaluation.setQuestionEtuI8(ficheEtudiantDto.isQuestionEtuI8());
        ficheEvaluation.setQuestionEtuII1(ficheEtudiantDto.isQuestionEtuII1());
        ficheEvaluation.setQuestionEtuII2(ficheEtudiantDto.isQuestionEtuII2());
        ficheEvaluation.setQuestionEtuII3(ficheEtudiantDto.isQuestionEtuII3());
        ficheEvaluation.setQuestionEtuII4(ficheEtudiantDto.isQuestionEtuII4());
        ficheEvaluation.setQuestionEtuII5(ficheEtudiantDto.isQuestionEtuII5());
        ficheEvaluation.setQuestionEtuII6(ficheEtudiantDto.isQuestionEtuII6());
        ficheEvaluation.setQuestionEtuIII1(ficheEtudiantDto.isQuestionEtuIII1());
        ficheEvaluation.setQuestionEtuIII2(ficheEtudiantDto.isQuestionEtuIII2());
        ficheEvaluation.setQuestionEtuIII3(ficheEtudiantDto.isQuestionEtuIII3());
        ficheEvaluation.setQuestionEtuIII4(ficheEtudiantDto.isQuestionEtuIII4());
        ficheEvaluation.setQuestionEtuIII5(ficheEtudiantDto.isQuestionEtuIII5());
        ficheEvaluation.setQuestionEtuIII6(ficheEtudiantDto.isQuestionEtuIII6());
        ficheEvaluation.setQuestionEtuIII7(ficheEtudiantDto.isQuestionEtuIII7());
        ficheEvaluation.setQuestionEtuIII8(ficheEtudiantDto.isQuestionEtuIII8());
        ficheEvaluation.setQuestionEtuIII9(ficheEtudiantDto.isQuestionEtuIII9());
        ficheEvaluation.setQuestionEtuIII10(ficheEtudiantDto.isQuestionEtuIII10());
        ficheEvaluation.setQuestionEtuIII11(ficheEtudiantDto.isQuestionEtuIII11());
        ficheEvaluation.setQuestionEtuIII12(ficheEtudiantDto.isQuestionEtuIII12());
        ficheEvaluation.setQuestionEtuIII13(ficheEtudiantDto.isQuestionEtuIII13());
        ficheEvaluation.setQuestionEtuIII14(ficheEtudiantDto.isQuestionEtuIII14());
        ficheEvaluation.setQuestionEtuIII15(ficheEtudiantDto.isQuestionEtuIII15());
        ficheEvaluation.setQuestionEtuIII16(ficheEtudiantDto.isQuestionEtuIII16());
    }


    public void setFicheEnseignantData(FicheEvaluation ficheEvaluation, FicheEnseignantDto ficheEnseignantDto) {

        ficheEvaluation.setQuestionEnsI1(ficheEnseignantDto.isQuestionEnsI1());
        ficheEvaluation.setQuestionEnsI2(ficheEnseignantDto.isQuestionEnsI2());
        ficheEvaluation.setQuestionEnsI3(ficheEnseignantDto.isQuestionEnsI3());
        ficheEvaluation.setQuestionEnsII1(ficheEnseignantDto.isQuestionEnsII1());
        ficheEvaluation.setQuestionEnsII2(ficheEnseignantDto.isQuestionEnsII2());
        ficheEvaluation.setQuestionEnsII3(ficheEnseignantDto.isQuestionEnsII3());
        ficheEvaluation.setQuestionEnsII4(ficheEnseignantDto.isQuestionEnsII4());
        ficheEvaluation.setQuestionEnsII5(ficheEnseignantDto.isQuestionEnsII5());
        ficheEvaluation.setQuestionEnsII6(ficheEnseignantDto.isQuestionEnsII6());
        ficheEvaluation.setQuestionEnsII7(ficheEnseignantDto.isQuestionEnsII7());
        ficheEvaluation.setQuestionEnsII8(ficheEnseignantDto.isQuestionEnsII8());
        ficheEvaluation.setQuestionEnsII9(ficheEnseignantDto.isQuestionEnsII9());
        ficheEvaluation.setQuestionEnsII10(ficheEnseignantDto.isQuestionEnsII10());
        ficheEvaluation.setQuestionEnsII11(ficheEnseignantDto.isQuestionEnsII11());
    }


    public void setFicheEntrepriseData(FicheEvaluation ficheEvaluation, FicheEntrepriseDto ficheEntreprisetDto) {
        ficheEvaluation.setQuestionEnt1(ficheEntreprisetDto.isQuestionEnt1());
        ficheEvaluation.setQuestionEnt2(ficheEntreprisetDto.isQuestionEnt2());
        ficheEvaluation.setQuestionEnt3(ficheEntreprisetDto.isQuestionEnt3());
        ficheEvaluation.setQuestionEnt4(ficheEntreprisetDto.isQuestionEnt4());
        ficheEvaluation.setQuestionEnt5(ficheEntreprisetDto.isQuestionEnt5());
        ficheEvaluation.setQuestionEnt6(ficheEntreprisetDto.isQuestionEnt6());
        ficheEvaluation.setQuestionEnt7(ficheEntreprisetDto.isQuestionEnt7());
        ficheEvaluation.setQuestionEnt8(ficheEntreprisetDto.isQuestionEnt8());
        ficheEvaluation.setQuestionEnt9(ficheEntreprisetDto.isQuestionEnt9());
        ficheEvaluation.setQuestionEnt10(ficheEntreprisetDto.isQuestionEnt10());
        ficheEvaluation.setQuestionEnt11(ficheEntreprisetDto.isQuestionEnt11());
        ficheEvaluation.setQuestionEnt12(ficheEntreprisetDto.isQuestionEnt12());
        ficheEvaluation.setQuestionEnt13(ficheEntreprisetDto.isQuestionEnt13());
        ficheEvaluation.setQuestionEnt14(ficheEntreprisetDto.isQuestionEnt14());
        ficheEvaluation.setQuestionEnt15(ficheEntreprisetDto.isQuestionEnt15());
        ficheEvaluation.setQuestionEnt16(ficheEntreprisetDto.isQuestionEnt16());
        ficheEvaluation.setQuestionEnt17(ficheEntreprisetDto.isQuestionEnt17());
        ficheEvaluation.setQuestionEnt18(ficheEntreprisetDto.isQuestionEnt18());
        ficheEvaluation.setQuestionEnt19(ficheEntreprisetDto.isQuestionEnt19());
    }

    public void setQuestionSupplementaireData(QuestionSupplementaire questionSupplementaire, QuestionSupplementaireDto questionSupplementairedto) {
        questionSupplementaire.setQuestion(questionSupplementairedto.getQuestion());
        questionSupplementaire.setTypeQuestion(questionSupplementairedto.getTypeQuestion());
        questionSupplementaire.setIdPlacement(questionSupplementairedto.getIdPlacement());
    }

    public void setReponseEvaluationEtudiantData(ReponseEvaluation reponseEvaluation, ReponseEtudiantFormDto reponseEtudiantFormDto) {

        reponseEvaluation.setReponseEtuI1(reponseEtudiantFormDto.getReponseEtuI1());
        reponseEvaluation.setReponseEtuI1bis(reponseEtudiantFormDto.getReponseEtuI1bis());
        reponseEvaluation.setReponseEtuI2(reponseEtudiantFormDto.getReponseEtuI2());
        reponseEvaluation.setReponseEtuI3(reponseEtudiantFormDto.getReponseEtuI3());
        reponseEvaluation.setReponseEtuI4a(reponseEtudiantFormDto.getReponseEtuI4a());
        reponseEvaluation.setReponseEtuI4b(reponseEtudiantFormDto.getReponseEtuI4b());
        reponseEvaluation.setReponseEtuI4c(reponseEtudiantFormDto.getReponseEtuI4c());
        reponseEvaluation.setReponseEtuI4d(reponseEtudiantFormDto.getReponseEtuI4d());
        reponseEvaluation.setReponseEtuI5(reponseEtudiantFormDto.getReponseEtuI5());
        reponseEvaluation.setReponseEtuI6(reponseEtudiantFormDto.getReponseEtuI6());
        reponseEvaluation.setReponseEtuI7(reponseEtudiantFormDto.getReponseEtuI7());
        reponseEvaluation.setReponseEtuI7bis1(reponseEtudiantFormDto.getReponseEtuI7bis1());
        reponseEvaluation.setReponseEtuI7bis1a(reponseEtudiantFormDto.getReponseEtuI7bis1a());
        reponseEvaluation.setReponseEtuI7bis1b(reponseEtudiantFormDto.getReponseEtuI7bis1b());
        reponseEvaluation.setReponseEtuI7bis2(reponseEtudiantFormDto.getReponseEtuI7bis2());
        reponseEvaluation.setReponseEtuI8(reponseEtudiantFormDto.getReponseEtuI8());
        reponseEvaluation.setReponseEtuII1(reponseEtudiantFormDto.getReponseEtuII1());
        reponseEvaluation.setReponseEtuII1bis(reponseEtudiantFormDto.getReponseEtuII1bis());
        reponseEvaluation.setReponseEtuII2(reponseEtudiantFormDto.getReponseEtuII2());
        reponseEvaluation.setReponseEtuII2bis(reponseEtudiantFormDto.getReponseEtuII2bis());
        reponseEvaluation.setReponseEtuII3(reponseEtudiantFormDto.getReponseEtuII3());
        reponseEvaluation.setReponseEtuII3bis(reponseEtudiantFormDto.getReponseEtuII3bis());
        reponseEvaluation.setReponseEtuII4(reponseEtudiantFormDto.getReponseEtuII4());
        reponseEvaluation.setReponseEtuII5(reponseEtudiantFormDto.getReponseEtuII5());
        reponseEvaluation.setReponseEtuII5a(reponseEtudiantFormDto.getReponseEtuII5a());
        reponseEvaluation.setReponseEtuII5b(reponseEtudiantFormDto.getReponseEtuII5b());
        reponseEvaluation.setReponseEtuII6(reponseEtudiantFormDto.getReponseEtuII6());
        reponseEvaluation.setReponseEtuIII1(reponseEtudiantFormDto.getReponseEtuIII1());
        reponseEvaluation.setReponseEtuIII1bis(reponseEtudiantFormDto.getReponseEtuIII1bis());
        reponseEvaluation.setReponseEtuIII2(reponseEtudiantFormDto.getReponseEtuIII2());
        reponseEvaluation.setReponseEtuIII2bis(reponseEtudiantFormDto.getReponseEtuIII2bis());
        reponseEvaluation.setReponseEtuIII3(reponseEtudiantFormDto.getReponseEtuIII3());
        reponseEvaluation.setReponseEtuIII3bis(reponseEtudiantFormDto.getReponseEtuIII3bis());
        reponseEvaluation.setReponseEtuIII4(reponseEtudiantFormDto.getReponseEtuIII4());
        reponseEvaluation.setReponseEtuIII5a(reponseEtudiantFormDto.getReponseEtuIII5a());
        reponseEvaluation.setReponseEtuIII5b(reponseEtudiantFormDto.getReponseEtuIII5b());
        reponseEvaluation.setReponseEtuIII5c(reponseEtudiantFormDto.getReponseEtuIII5c());
        reponseEvaluation.setReponseEtuIII5bis(reponseEtudiantFormDto.getReponseEtuIII5bis());
        reponseEvaluation.setReponseEtuIII6(reponseEtudiantFormDto.getReponseEtuIII6());
        reponseEvaluation.setReponseEtuIII6bis(reponseEtudiantFormDto.getReponseEtuIII6bis());
        reponseEvaluation.setReponseEtuIII7(reponseEtudiantFormDto.getReponseEtuIII7());
        reponseEvaluation.setReponseEtuIII7bis(reponseEtudiantFormDto.getReponseEtuIII7bis());
        reponseEvaluation.setReponseEtuIII8(reponseEtudiantFormDto.getReponseEtuIII8());
        reponseEvaluation.setReponseEtuIII8bis(reponseEtudiantFormDto.getReponseEtuIII8bis());
        reponseEvaluation.setReponseEtuIII9(reponseEtudiantFormDto.getReponseEtuIII9());
        reponseEvaluation.setReponseEtuIII9bis(reponseEtudiantFormDto.getReponseEtuIII9bis());
        reponseEvaluation.setReponseEtuIII10(reponseEtudiantFormDto.getReponseEtuIII10());
        reponseEvaluation.setReponseEtuIII11(reponseEtudiantFormDto.getReponseEtuIII11());
        reponseEvaluation.setReponseEtuIII12(reponseEtudiantFormDto.getReponseEtuIII12());
        reponseEvaluation.setReponseEtuIII13(reponseEtudiantFormDto.getReponseEtuIII13());
        reponseEvaluation.setReponseEtuIII14(reponseEtudiantFormDto.getReponseEtuIII14());
        reponseEvaluation.setReponseEtuIII15(reponseEtudiantFormDto.getReponseEtuIII15());
        reponseEvaluation.setReponseEtuIII15bis(reponseEtudiantFormDto.getReponseEtuIII15bis());
        reponseEvaluation.setReponseEtuIII16(reponseEtudiantFormDto.getReponseEtuIII16());
        reponseEvaluation.setReponseEtuIII16bis(reponseEtudiantFormDto.getReponseEtuIII16bis());
    }

    public void setReponseEvaluationEnseignantData(ReponseEvaluation reponseEvaluation, ReponseEnseignantFormDto reponseEnseignantFormDto) {

        reponseEvaluation.setReponseEnsI1a(reponseEnseignantFormDto.getReponseEnsI1a());
        reponseEvaluation.setReponseEnsI1b(reponseEnseignantFormDto.getReponseEnsI1b());
        reponseEvaluation.setReponseEnsI1c(reponseEnseignantFormDto.getReponseEnsI1c());
        reponseEvaluation.setReponseEnsI2a(reponseEnseignantFormDto.getReponseEnsI2a());
        reponseEvaluation.setReponseEnsI2b(reponseEnseignantFormDto.getReponseEnsI2b());
        reponseEvaluation.setReponseEnsI2c(reponseEnseignantFormDto.getReponseEnsI2c());
        reponseEvaluation.setReponseEnsI3(reponseEnseignantFormDto.getReponseEnsI3());
        reponseEvaluation.setReponseEnsII1(reponseEnseignantFormDto.getReponseEnsII1());
        reponseEvaluation.setReponseEnsII2(reponseEnseignantFormDto.getReponseEnsII2());
        reponseEvaluation.setReponseEnsII3(reponseEnseignantFormDto.getReponseEnsII3());
        reponseEvaluation.setReponseEnsII4(reponseEnseignantFormDto.getReponseEnsII4());
        reponseEvaluation.setReponseEnsII5(reponseEnseignantFormDto.getReponseEnsII5());
        reponseEvaluation.setReponseEnsII6(reponseEnseignantFormDto.getReponseEnsII6());
        reponseEvaluation.setReponseEnsII7(reponseEnseignantFormDto.getReponseEnsII7());
        reponseEvaluation.setReponseEnsII8(reponseEnseignantFormDto.getReponseEnsII8());
        reponseEvaluation.setReponseEnsII9(reponseEnseignantFormDto.getReponseEnsII9());
        reponseEvaluation.setReponseEnsII10(reponseEnseignantFormDto.getReponseEnsII10());
        reponseEvaluation.setReponseEnsII11(reponseEnseignantFormDto.getReponseEnsII11());
    }

    public void setReponseEvaluationEntrepriseData(ReponseEvaluation reponseEvaluation, ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        reponseEvaluation.setReponseEnt1(reponseEntrepriseFormDto.getReponseEnt1());
        reponseEvaluation.setReponseEnt1bis(reponseEntrepriseFormDto.getReponseEnt1bis());
        reponseEvaluation.setReponseEnt2(reponseEntrepriseFormDto.getReponseEnt2());
        reponseEvaluation.setReponseEnt2bis(reponseEntrepriseFormDto.getReponseEnt2bis());
        reponseEvaluation.setReponseEnt3(reponseEntrepriseFormDto.getReponseEnt3());
        reponseEvaluation.setReponseEnt4(reponseEntrepriseFormDto.getReponseEnt4());
        reponseEvaluation.setReponseEnt4bis(reponseEntrepriseFormDto.getReponseEnt4bis());
        reponseEvaluation.setReponseEnt5(reponseEntrepriseFormDto.getReponseEnt5());
        reponseEvaluation.setReponseEnt5bis(reponseEntrepriseFormDto.getReponseEnt5bis());
        reponseEvaluation.setReponseEnt6(reponseEntrepriseFormDto.getReponseEnt6());
        reponseEvaluation.setReponseEnt6bis(reponseEntrepriseFormDto.getReponseEnt6bis());
        reponseEvaluation.setReponseEnt7(reponseEntrepriseFormDto.getReponseEnt7());
        reponseEvaluation.setReponseEnt7bis(reponseEntrepriseFormDto.getReponseEnt7bis());
        reponseEvaluation.setReponseEnt8(reponseEntrepriseFormDto.getReponseEnt8());
        reponseEvaluation.setReponseEnt8bis(reponseEntrepriseFormDto.getReponseEnt8bis());
        reponseEvaluation.setReponseEnt9(reponseEntrepriseFormDto.getReponseEnt9());
        reponseEvaluation.setReponseEnt9bis(reponseEntrepriseFormDto.getReponseEnt9bis());
        reponseEvaluation.setReponseEnt10(reponseEntrepriseFormDto.getReponseEnt10());
        reponseEvaluation.setReponseEnt10bis(reponseEntrepriseFormDto.getReponseEnt10bis());
        reponseEvaluation.setReponseEnt11(reponseEntrepriseFormDto.getReponseEnt11());
        reponseEvaluation.setReponseEnt11bis(reponseEntrepriseFormDto.getReponseEnt11bis());
        reponseEvaluation.setReponseEnt12(reponseEntrepriseFormDto.getReponseEnt12());
        reponseEvaluation.setReponseEnt12bis(reponseEntrepriseFormDto.getReponseEnt12bis());
        reponseEvaluation.setReponseEnt13(reponseEntrepriseFormDto.getReponseEnt13());
        reponseEvaluation.setReponseEnt13bis(reponseEntrepriseFormDto.getReponseEnt13bis());
        reponseEvaluation.setReponseEnt14(reponseEntrepriseFormDto.getReponseEnt14());
        reponseEvaluation.setReponseEnt14bis(reponseEntrepriseFormDto.getReponseEnt14bis());
        reponseEvaluation.setReponseEnt15(reponseEntrepriseFormDto.getReponseEnt15());
        reponseEvaluation.setReponseEnt15bis(reponseEntrepriseFormDto.getReponseEnt15bis());
        reponseEvaluation.setReponseEnt16(reponseEntrepriseFormDto.getReponseEnt16());
        reponseEvaluation.setReponseEnt16bis(reponseEntrepriseFormDto.getReponseEnt16bis());
        reponseEvaluation.setReponseEnt17(reponseEntrepriseFormDto.getReponseEnt17());
        reponseEvaluation.setReponseEnt17bis(reponseEntrepriseFormDto.getReponseEnt17bis());
        reponseEvaluation.setReponseEnt18(reponseEntrepriseFormDto.getReponseEnt18());
        reponseEvaluation.setReponseEnt18bis(reponseEntrepriseFormDto.getReponseEnt18bis());
        reponseEvaluation.setReponseEnt19(reponseEntrepriseFormDto.getReponseEnt19());
    }

    public void setReponseSupplementaireData(ReponseSupplementaire reponseSupplementaire, ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        reponseSupplementaire.setReponseTxt(reponseSupplementaireFormDto.getReponseTxt());
        reponseSupplementaire.setReponseBool(reponseSupplementaireFormDto.getReponseBool());
        reponseSupplementaire.setReponseInt(reponseSupplementaireFormDto.getReponseInt());
    }
}
