package org.esup_portail.esup_stage.service.evaluation;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.enums.ExportType;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.EvaluationJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;

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

    @Autowired
    private EvaluationJwtService evaluationJwtService;

    @Autowired
    private EvaluationExcelExporter evaluationExcelExporter;

    /* ===================== Tokens: logique métier BDD ===================== */

    /**
     * Récupère un token non expiré et non révoqué (peut être déjà utilisé).
     * @param tokenValue valeur brute du token
     * @return le token ou null s'il est absent/expiré/révoqué
     */
    public EvaluationTuteurToken getToken(String tokenValue) {
        if (isBlank(tokenValue)) return null;
        EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
        if (token == null) return null;
        if (token.isExpired() || Boolean.TRUE.equals(token.getRevoque())) return null;
        return token;
    }

    /**
     * Valide qu'un token est actif (non expiré, non révoqué, non utilisé), puis le marque utilisé.
     * @param tokenValue valeur brute du token
     * @return le token si succès, sinon null
     */
    @Transactional
    public EvaluationTuteurToken validateAndUseToken(String tokenValue) {
        if (isBlank(tokenValue)) {
            logger.warn("Tentative d'accès avec token null ou vide");
            return null;
        }
        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            if (token == null) {
                warnInvalid(tokenValue);
                return null;
            }
            if (!token.isActive()) {
                logger.warn("Token inactif (expiré/utilisé/révoqué) pour tuteur ID: {}", token.getContact().getId());
                return null;
            }
            token.setUtilise(true);
            tokenRepository.save(token);
            logger.info("Token validé et marqué utilisé pour tuteur ID: {}", token.getContact().getId());
            return token;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return null;
        }
    }

    /**
     * Valide un token actif (non expiré, non révoqué, non utilisé) sans le marquer utilisé.
     * @param tokenValue valeur brute du token
     * @return le token actif ou null
     */
    public EvaluationTuteurToken validateToken(String tokenValue) {
        if (isBlank(tokenValue)) return null;
        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            return (token != null && token.isActive()) ? token : null;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return null;
        }
    }

    /**
     * Valide qu'un token existe, est non expiré, non révoqué ET déjà utilisé.
     * @param tokenValue valeur brute du token
     * @return le token ou null
     */
    public EvaluationTuteurToken validateUsedToken(String tokenValue) {
        if (isBlank(tokenValue)) return null;
        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            return (token != null
                    && !token.isExpired()
                    && Boolean.TRUE.equals(token.getUtilise())
                    && !Boolean.TRUE.equals(token.getRevoque())) ? token : null;
        } catch (Exception e) {
            logger.error("Erreur lors de la validation du token", e);
            return null;
        }
    }

    /**
     * Révoque un token s'il existe et n'est pas déjà révoqué.
     * @param tokenValue valeur brute du token
     * @return true si une révocation a eu lieu
     */
    @Transactional
    public boolean revokeToken(String tokenValue) {
        if (isBlank(tokenValue)) return false;
        try {
            EvaluationTuteurToken token = tokenRepository.findByToken(tokenValue);
            if (token != null && !Boolean.TRUE.equals(token.getRevoque())) {
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
     * Construit l'URL d'évaluation pour une convention.
     * @param tokenValue token pour la construction de l'url
     * @return String url vers la convention
     */
    public String buildEvaluationTuteurUrl(String tokenValue) {
        if (isBlank(tokenValue)) {
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
     * Construit l'URL d'évaluation pour une convention.
     * Crée automatiquement un token si aucun token actif n'existe pour le tuteur.
     * @param convention Convention de l'évaluation
     * @return String url vers la convention
     */
    @Transactional
    public String buildEvaluationTuteurUrl(Convention convention) {
        if (convention == null || convention.getContact() == null) return "";

        // Chercher un token actif existant
        List<EvaluationTuteurToken> tokens = tokenRepository.findByTuteurId(convention.getContact().getId());
        EvaluationTuteurToken tokenActif = null;
        if (tokens != null) {
            for (EvaluationTuteurToken t : tokens) {
                if (t != null && t.isActive()) {
                    tokenActif = t;
                    break;
                }
            }
        }

        // Sinon en créer un
        if (tokenActif == null) {
            tokenActif = createToken(convention);
        }

        if (tokenActif == null) {
            logger.warn("Impossible de créer ou trouver un token actif pour tuteur ID: {}", convention.getContact().getId());
            return "";
        }

        return buildEvaluationTuteurUrl(tokenActif.getToken());
    }

    /**
     * Création d'un token pour une convention (si aucun actif n'existe déjà).
     * @param convention convention à laquelle est relié le token
     * @return evaluationTuteurToken token créé
     */
    @Transactional
    public EvaluationTuteurToken createToken(Convention convention) {
        if (convention == null || convention.getContact() == null) {
            logger.warn("Impossible de créer un token : convention ou contact manquant");
            return null;
        }

        try {
            // Ne pas dupliquer si un actif existe déjà
            List<EvaluationTuteurToken> existingTokens = tokenRepository.findByTuteurId(convention.getContact().getId());
            for (EvaluationTuteurToken existingToken : existingTokens) {
                if (existingToken.isActive()) {
                    logger.info("Token actif existant trouvé pour tuteur ID: {}, pas de token créé.", convention.getContact().getId());
                    return existingToken;
                }
            }

            EvaluationTuteurToken newToken = new EvaluationTuteurToken(
                    convention,
                    convention.getContact(),
                    Duration.ofDays(appliProperties.getNbJoursValideToken()),
                    evaluationJwtService
            );
            EvaluationTuteurToken savedToken = tokenRepository.save(newToken);
            logger.info("Nouveau token créé pour le tuteur ID: {}", convention.getContact().getId());
            return savedToken;

        } catch (Exception e) {
            logger.error("Erreur lors de la création du token pour le tuteur ID: " + convention.getContact().getId(), e);
            return null;
        }
    }

    /* ===================== Fiche / Réponses ===================== */

    /**
     * Récupère la fiche d'évaluation d'un centre de gestion
     * @param id identifiant du centre de gestion
     * @return FicheEvaluation du centre de gestion
     */
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

    /**
     * initialise les objects qui stocks les réponses de l'utilisateur aux formulaires d'évaluation
     * @param id identifiant de la convention
     * @return ReponseEvaluation
     */
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

    /**
     * initialise les objects qui stocks les réponses supplémentaires de l'utilisateur aux formulaires d'évaluation
     * @param idConvention identifiant de la convention
     * @param idQuestion identifiant de la question
     * @return ReponseSupplementaire
     */
    public ReponseSupplementaire initReponseSupplementaire(int idConvention, int idQuestion) {
        Convention convention = conventionJpaRepository.findById(idConvention);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        QuestionSupplementaire questionSupplementaire = questionSupplementaireJpaRepository.findById(idQuestion);
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

    public void setReponseSupplementaireData(ReponseSupplementaire reponseSupplementaire, ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        reponseSupplementaire.setReponseTxt(reponseSupplementaireFormDto.getReponseTxt());
        reponseSupplementaire.setReponseBool(reponseSupplementaireFormDto.getReponseBool());
        reponseSupplementaire.setReponseInt(reponseSupplementaireFormDto.getReponseInt());
    }

    public void setReponseEvaluationEntrepriseData(ReponseEvaluation reponseEvaluation,
                                                   ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
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

    /* ===================== Export Excel ===================== */

    public byte[] getEvaluationToExcel(List<EvaluationDto> evaluationDtos, Integer typeFiche) {
        byte[] file = null;
        switch(typeFiche){
            case 0:{
                file = evaluationExcelExporter.export(evaluationDtos, ExportType.ETUDIANT);
            }
            case 1:{
                file = evaluationExcelExporter.export(evaluationDtos,ExportType.ENSEIGNANT);
            }
            case 2:{
                file = evaluationExcelExporter.export(evaluationDtos,ExportType.ENTREPRISE);
            }
            default:{
                file = evaluationExcelExporter.export(evaluationDtos,ExportType.ALL_IN_ONE);
            }
        }
        return file;
    }


    /* ===================== Helpers ===================== */

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void warnInvalid(String tokenValue) {
        String shortTok = tokenValue == null ? "null" : tokenValue.substring(0, Math.min(8, tokenValue.length())) + "...";
        logger.warn("Token non trouvé: {}", shortTok);
    }
}
