package org.esup_portail.esup_stage.controller;

import jakarta.validation.Valid;
import org.esup_portail.esup_stage.dto.ReponseEnseignantFormDto;
import org.esup_portail.esup_stage.dto.ReponseEntrepriseFormDto;
import org.esup_portail.esup_stage.dto.ReponseEtudiantFormDto;
import org.esup_portail.esup_stage.dto.ReponseSupplementaireFormDto;
import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.repository.QuestionSupplementaireJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseEvaluationJpaRepository;
import org.esup_portail.esup_stage.repository.ReponseSupplementaireJpaRepository;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.esup_portail.esup_stage.security.interceptor.Secure;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Date;

@ApiController
@RequestMapping("/reponseEvaluation")
public class ReponseEvaluationController {

    @Autowired
    ReponseEvaluationJpaRepository reponseEvaluationJpaRepository;

    @Autowired
    ConventionJpaRepository conventionJpaRepository;

    @Autowired
    ReponseSupplementaireJpaRepository reponseSupplementaireJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    MailerService mailerService;

    @Autowired
    AppConfigService appConfigService;

    @Autowired
    EvaluationService evaluationService;

    @GetMapping("/getByConvention/{id}")
    public ReponseEvaluation getByConvention(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        return reponseEvaluation;
    }

    @PostMapping("/{id}/etudiant/valid/{valid}")
    public ReponseEvaluation createReponseEtudiant(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = evaluationService.initReponseEvaluation(id);
        reponseEvaluation.setValidationEtudiant(valid);
        evaluationService.setReponseEvaluationEtudiantData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/etudiant/valid/{valid}")
    public ReponseEvaluation updateReponseEtudiant(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEtudiant(valid);
        evaluationService.setReponseEvaluationEtudiantData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PostMapping("/{id}/enseignant/valid/{valid}")
    public ReponseEvaluation createReponseEnseignant(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEnseignantFormDto reponseEnseignantFormDto) {
        ReponseEvaluation reponseEvaluation = evaluationService.initReponseEvaluation(id);
        reponseEvaluation.setValidationEnseignant(valid);
        evaluationService.setReponseEvaluationEnseignantData(reponseEvaluation, reponseEnseignantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/enseignant/valid/{valid}")
    public ReponseEvaluation updateReponseEnseignant(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEnseignantFormDto reponseEnseignantFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEnseignant(valid);
        evaluationService.setReponseEvaluationEnseignantData(reponseEvaluation, reponseEnseignantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PostMapping("/{id}/entreprise/valid/{valid}")
    public ReponseEvaluation createReponseEntreprise(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        ReponseEvaluation reponseEvaluation = evaluationService.initReponseEvaluation(id);
        reponseEvaluation.setValidationEntreprise(valid);
        evaluationService.setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/entreprise/valid/{valid}")
    public ReponseEvaluation updateReponseEntreprise(@PathVariable("id") int id, @PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEntreprise(valid);
        evaluationService.setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @GetMapping("/{id}/getFichePDF/typeFiche/{typeFiche}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getFichePDF(@PathVariable("id") int id, @PathVariable("typeFiche") int typeFiche) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();

        if (typeFiche == 0) {
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(),reponseEvaluation.getConvention().getAvenants().getLast(),ou,typeFiche);
            reponseEvaluation.setImpressionEtudiant(true);
        }
        if (typeFiche == 1) {
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(),reponseEvaluation.getConvention().getAvenants().getLast(),ou,typeFiche);
            reponseEvaluation.setImpressionEnseignant(true);
        }
        if (typeFiche == 2) {
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(),reponseEvaluation.getConvention().getAvenants().getLast(),ou,typeFiche);
            reponseEvaluation.setImpressionEntreprise(true);
        }
        reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    @GetMapping("/{id}/sendMailEvaluation/typeFiche/{typeFiche}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public void sendMailEvaluation(@PathVariable("id") int id, @PathVariable("typeFiche") int typeFiche) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        Utilisateur utilisateur = ServiceContext.getUtilisateur();

        String mailEtudiant = convention.getCourrielPersoEtudiant();
        if (mailEtudiant == null || !appConfigService.getConfigGenerale().isUtiliserMailPersoEtudiant())
            mailEtudiant = convention.getEtudiant().getMail();

        String template = "";
        if (typeFiche == 0) {
            template = TemplateMail.CODE_FICHE_EVAL_ETU;
            if (convention.getEnvoiMailEtudiant() != null && convention.getEnvoiMailEtudiant())
                template = TemplateMail.CODE_RAPPEL_FICHE_EVAL_ETU;
            if (convention.getCentreGestion().isOnlyMailCentreGestion()) {
                mailerService.sendAlerteValidation(convention.getCentreGestion().getMail(), convention, null, utilisateur, template);
            } else {
                mailerService.sendAlerteValidation(mailEtudiant, convention, null, utilisateur, template);
                convention.setEnvoiMailEtudiant(true);
                convention.setDateEnvoiMailEtudiant(new Date());
            }
        } else if (typeFiche == 1) {
            template = TemplateMail.CODE_FICHE_EVAL_ENSEIGNANT;
            if (convention.getEnvoiMailTuteurPedago() != null && convention.getEnvoiMailTuteurPedago())
                template = TemplateMail.CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT;
            if (convention.getCentreGestion().isOnlyMailCentreGestion()) {
                mailerService.sendAlerteValidation(convention.getCentreGestion().getMail(), convention, null, utilisateur, template);
            } else {
                mailerService.sendAlerteValidation(convention.getEnseignant().getMail(), convention, null, utilisateur, template);
                convention.setEnvoiMailTuteurPedago(true);
                convention.setDateEnvoiMailTuteurPedago(new Date());
            }
        } else if (typeFiche == 2) {
            template = TemplateMail.CODE_FICHE_EVAL_TUTEUR;
            if (convention.getEnvoiMailTuteurPro() != null && convention.getEnvoiMailTuteurPro())
                template = TemplateMail.CODE_RAPPEL_FICHE_EVAL_TUTEUR;
            mailerService.sendAlerteValidation(convention.getContact().getMail(), convention, null, utilisateur, template);
            if (convention.getCentreGestion().isOnlyMailCentreGestion()) {
                mailerService.sendAlerteValidation(convention.getCentreGestion().getMail(), convention, null, utilisateur, template);
            } else {
                convention.setEnvoiMailTuteurPro(true);
                convention.setDateEnvoiMailTuteurPro(new Date());
            }
        }
        conventionJpaRepository.saveAndFlush(convention);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findById(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluationJpaRepository.delete(reponseEvaluation);
        reponseEvaluationJpaRepository.flush();
        return true;
    }

    @GetMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire getReponseSupplementaire(@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion) {
        return reponseSupplementaireJpaRepository.findByQuestionAndConvention(idConvention, idQestion);
    }

    @PostMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire createReponseSupplementaire(@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        ReponseSupplementaire reponseSupplementaire = evaluationService.initReponseSupplementaire(idConvention, idQestion);
        evaluationService.setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }

    @PutMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire updateReponseSupplementaire(@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        ReponseSupplementaire reponseSupplementaire = reponseSupplementaireJpaRepository.findByQuestionAndConvention(idConvention, idQestion);
        if (reponseSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseSupplementaire non trouvé");
        }
        evaluationService.setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }
}