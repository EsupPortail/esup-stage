package org.esup_portail.esup_stage.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.model.helper.UtilisateurHelper;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.EtudiantSecurityService;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ApiController
@RequestMapping("/ficheEvaluation")
public class FicheEvaluationController {

    @Autowired
    FicheEvaluationRepository ficheEvaluationRepository;

    @Autowired
    FicheEvaluationJpaRepository ficheEvaluationJpaRepository;

    @Autowired
    QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;

    @Autowired
    CentreGestionJpaRepository centreGestionJpaRepository;

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    EtudiantSecurityService etudiantSecurityService;

    @Autowired
    PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;

    @GetMapping
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public PaginatedResponse<FicheEvaluation> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters, HttpServletResponse response) {
        PaginatedResponse<FicheEvaluation> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(ficheEvaluationRepository.count(filters));
        paginatedResponse.setData(ficheEvaluationRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public FicheEvaluation getById(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        return ficheEvaluation;
    }

    @GetMapping("/getByCentreGestion/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION,AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public FicheEvaluation getByCentreGestion(@PathVariable("id") int id) {
        CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
        checkCanViewCentreGestion(centreGestion);

        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findByCentreGestion(id);
        if (ficheEvaluation == null) {
            ficheEvaluation = new FicheEvaluation();
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
            }
            ficheEvaluation.setCentreGestion(centreGestion);
            return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
        }
        return ficheEvaluation;
    }

    private void checkCanViewCentreGestion(CentreGestion centreGestion) {
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        if (utilisateur == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Vous n'etes pas autorise");
        }
        if (centreGestion == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
        }
        if (UtilisateurHelper.isRole(utilisateur, Role.ADM)) {
            return;
        }

        List<String> identifiants = getUserIdentifiers(utilisateur);
        boolean canView = false;
        if (UtilisateurHelper.isRole(utilisateur, Role.GES) || UtilisateurHelper.isRole(utilisateur, Role.RESP_GES)) {
            canView = !identifiants.isEmpty()
                    && personnelCentreGestionJpaRepository.countByCentreGestionAndUidPersonnel(centreGestion.getId(), identifiants) > 0;
        }
        if (!canView && UtilisateurHelper.isRole(utilisateur, Role.ETU)) {
            canView = etudiantSecurityService.isEtudiantInCentreGestion(utilisateur, centreGestion.getId());
        }
        if (!canView && UtilisateurHelper.isRole(utilisateur, Role.ENS)) {
            canView = !identifiants.isEmpty()
                    && conventionJpaRepository.countConventionByEnseignantAndCentreGestion(identifiants, centreGestion.getId()) > 0;
        }

        if (!canView) {
            throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouve");
        }
    }

    private List<String> getUserIdentifiers(Utilisateur utilisateur) {
        return Stream.of(utilisateur.getUid(), utilisateur.getLogin())
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    @PutMapping("/saveAndValidateFicheEtudiant/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public FicheEvaluation saveAndValidateFicheEtudiant(@PathVariable("id") int id, @Valid @RequestBody FicheEtudiantDto ficheEtudiantDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        evaluationService.setFicheEtudiantData(ficheEvaluation, ficheEtudiantDto);
        ficheEvaluation.setValidationEtudiant(true);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @PutMapping("/saveAndValidateFicheEnseignant/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public FicheEvaluation saveAndValidateFicheEnseignant(@PathVariable("id") int id, @Valid @RequestBody FicheEnseignantDto ficheEnseignantDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        evaluationService.setFicheEnseignantData(ficheEvaluation, ficheEnseignantDto);
        ficheEvaluation.setValidationEnseignant(true);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @PutMapping("/saveAndValidateFicheEntreprise/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public FicheEvaluation saveAndValidateFicheEntreprise(@PathVariable("id") int id, @Valid @RequestBody FicheEntrepriseDto ficheEntrepriseDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        evaluationService.setFicheEntrepriseData(ficheEvaluation, ficheEntrepriseDto);
        ficheEvaluation.setValidationEntreprise(true);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @GetMapping("/getQuestionsSupplementaires/{id}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION,AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
    public List<QuestionSupplementaire> getQuestionsSupplementaires(@PathVariable("id") int id) {
        return questionSupplementaireJpaRepository.findByFicheEvaluation(id);
    }

    @PutMapping("/addQuestionSupplementaire/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public QuestionSupplementaire addQuestionSupplementaire(@PathVariable("id") int id, @Valid @RequestBody QuestionSupplementaireDto questionSupplementaireDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        QuestionSupplementaire questionSupplementaire = new QuestionSupplementaire();
        questionSupplementaire.setFicheEvaluation(ficheEvaluation);
        evaluationService.setQuestionSupplementaireData(questionSupplementaire, questionSupplementaireDto);
        return questionSupplementaireJpaRepository.saveAndFlush(questionSupplementaire);
    }

    @PutMapping("/editQuestionSupplementaire/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public QuestionSupplementaire editQuestionSupplementaire(@PathVariable("id") int id, @Valid @RequestBody QuestionSupplementaireDto questionSupplementaireDto) {
        QuestionSupplementaire questionSupplementaire = questionSupplementaireJpaRepository.findById(id);
        if (questionSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "QuestionSupplementaire non trouvée");
        }
        evaluationService.setQuestionSupplementaireData(questionSupplementaire, questionSupplementaireDto);
        return questionSupplementaireJpaRepository.saveAndFlush(questionSupplementaire);
    }

    @DeleteMapping("/deleteQuestionSupplementaire/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public boolean deleteQuestionSupplementaire(@PathVariable("id") int id) {
        QuestionSupplementaire questionSupplementaire = questionSupplementaireJpaRepository.findById(id);
        if (questionSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "QuestionSupplementaire non trouvée");
        }
        questionSupplementaireJpaRepository.delete(questionSupplementaire);
        questionSupplementaireJpaRepository.flush();
        return true;
    }

    @DeleteMapping("/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.SUPPRESSION})
    public boolean delete(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        ficheEvaluationJpaRepository.delete(ficheEvaluation);
        ficheEvaluationJpaRepository.flush();
        return true;
    }
}