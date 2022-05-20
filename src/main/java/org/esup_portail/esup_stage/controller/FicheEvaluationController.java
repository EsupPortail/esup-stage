package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.*;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.FicheEvaluationRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

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
    public FicheEvaluation getByCentreGestion(@PathVariable("id") int id) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findByCentreGestion(id);
        if (ficheEvaluation == null) {
            ficheEvaluation = new FicheEvaluation();
            CentreGestion centreGestion = centreGestionJpaRepository.findById(id);
            if (centreGestion == null) {
                throw new AppException(HttpStatus.NOT_FOUND, "CentreGestion non trouvé");
            }
            ficheEvaluation.setCentreGestion(centreGestion);
            return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
        }
        return ficheEvaluation;
    }

    @PutMapping("/saveAndValidateFicheEtudiant/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public FicheEvaluation saveAndValidateFicheEtudiant(@PathVariable("id") int id, @Valid @RequestBody FicheEtudiantDto ficheEtudiantDto) {
        FicheEvaluation ficheEvaluation = ficheEvaluationJpaRepository.findById(id);
        if (ficheEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "FicheEvaluation non trouvée");
        }
        setFicheEtudiantData(ficheEvaluation, ficheEtudiantDto);
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
        setFicheEnseignantData(ficheEvaluation, ficheEnseignantDto);
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
        setFicheEntrepriseData(ficheEvaluation, ficheEntrepriseDto);
        ficheEvaluation.setValidationEntreprise(true);
        return ficheEvaluationJpaRepository.saveAndFlush(ficheEvaluation);
    }

    @GetMapping("/getQuestionsSupplementaires/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.LECTURE})
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
        setQuestionSupplementaireData(questionSupplementaire, questionSupplementaireDto);
        return questionSupplementaireJpaRepository.saveAndFlush(questionSupplementaire);
    }

    @PutMapping("/editQuestionSupplementaire/{id}")
    @Secure(fonctions = {AppFonctionEnum.PARAM_CENTRE}, droits = {DroitEnum.MODIFICATION})
    public QuestionSupplementaire editQuestionSupplementaire(@PathVariable("id") int id, @Valid @RequestBody QuestionSupplementaireDto questionSupplementaireDto) {
        QuestionSupplementaire questionSupplementaire = questionSupplementaireJpaRepository.findById(id);
        if (questionSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "QuestionSupplementaire non trouvée");
        }
        setQuestionSupplementaireData(questionSupplementaire, questionSupplementaireDto);
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

    private void setFicheEtudiantData(FicheEvaluation ficheEvaluation, FicheEtudiantDto ficheEtudiantDto) {
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


    private void setFicheEnseignantData(FicheEvaluation ficheEvaluation, FicheEnseignantDto ficheEnseignantDto) {

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


    private void setFicheEntrepriseData(FicheEvaluation ficheEvaluation, FicheEntrepriseDto ficheEntreprisetDto) {
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

    private void setQuestionSupplementaireData(QuestionSupplementaire questionSupplementaire, QuestionSupplementaireDto questionSupplementairedto) {
        questionSupplementaire.setQuestion(questionSupplementairedto.getQuestion());
        questionSupplementaire.setTypeQuestion(questionSupplementairedto.getTypeQuestion());
        questionSupplementaire.setIdPlacement(questionSupplementairedto.getIdPlacement());
    }

}