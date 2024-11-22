package org.esup_portail.esup_stage.controller;

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
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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
    FicheEvaluationController ficheEvaluationController;

    @Autowired
    ReponseSupplementaireJpaRepository reponseSupplementaireJpaRepository;

    @Autowired
    QuestionSupplementaireJpaRepository questionSupplementaireJpaRepository;

    @Autowired
    ImpressionService impressionService;

    @Autowired
    MailerService mailerService;

    @Autowired
    AppConfigService appConfigService;

    @GetMapping("/getByConvention/{id}")
    public ReponseEvaluation getByConvention(@PathVariable("id") int id) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        return reponseEvaluation;
    }

    @PostMapping("/{id}/etudiant/valid/{valid}")
    public ReponseEvaluation createReponseEtudiant(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = initReponseEvaluation(id);
        reponseEvaluation.setValidationEtudiant(valid);
        setReponseEvaluationEtudiantData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/etudiant/valid/{valid}")
    public ReponseEvaluation updateReponseEtudiant(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEtudiantFormDto reponseEtudiantFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEtudiant(valid);
        setReponseEvaluationEtudiantData(reponseEvaluation, reponseEtudiantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PostMapping("/{id}/enseignant/valid/{valid}")
    public ReponseEvaluation createReponseEnseignant(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEnseignantFormDto reponseEnseignantFormDto) {
        ReponseEvaluation reponseEvaluation = initReponseEvaluation(id);
        reponseEvaluation.setValidationEnseignant(valid);
        setReponseEvaluationEnseignantData(reponseEvaluation, reponseEnseignantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/enseignant/valid/{valid}")
    public ReponseEvaluation updateReponseEnseignant(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEnseignantFormDto reponseEnseignantFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEnseignant(valid);
        setReponseEvaluationEnseignantData(reponseEvaluation, reponseEnseignantFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PostMapping("/{id}/entreprise/valid/{valid}")
    public ReponseEvaluation createReponseEntreprise(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        ReponseEvaluation reponseEvaluation = initReponseEvaluation(id);
        reponseEvaluation.setValidationEntreprise(valid);
        setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }

    @PutMapping("/{id}/entreprise/valid/{valid}")
    public ReponseEvaluation updateReponseEntreprise(@PathVariable("id") int id,@PathVariable("valid") boolean valid, @Valid @RequestBody ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        reponseEvaluation.setValidationEntreprise(valid);
        setReponseEvaluationEntrepriseData(reponseEvaluation, reponseEntrepriseFormDto);
        return reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);
    }
    @PostMapping("/{id}/getFichePDF/typeFiche/{typeFiche}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.LECTURE})
    public ResponseEntity<byte[]> getFichePDF(@PathVariable("id") int id,@PathVariable("typeFiche") int typeFiche,@RequestBody String htmlTexte) {
        ReponseEvaluation reponseEvaluation = reponseEvaluationJpaRepository.findByConvention(id);
        if (reponseEvaluation == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseEvaluation non trouvé");
        }
        ByteArrayOutputStream ou = new ByteArrayOutputStream();

        if(typeFiche == 0) {
            impressionService.generateFichePDF(htmlTexte, ou);
            reponseEvaluation.setImpressionEtudiant(true);
        }
        if(typeFiche == 1) {
            impressionService.generateFichePDF(htmlTexte, ou);
            reponseEvaluation.setImpressionEnseignant(true);
        }
        if(typeFiche == 2) {
            impressionService.generateFichePDF(htmlTexte, ou);
            reponseEvaluation.setImpressionEntreprise(true);
        }
        reponseEvaluationJpaRepository.saveAndFlush(reponseEvaluation);

        byte[] pdf = ou.toByteArray();
        return ResponseEntity.ok().body(pdf);
    }

    @GetMapping("/{id}/sendMailEvaluation/typeFiche/{typeFiche}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public void sendMailEvaluation(@PathVariable("id") int id,@PathVariable("typeFiche") int typeFiche) {
        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        Utilisateur utilisateur = ServiceContext.getUtilisateur();

        String mailEtudiant = convention.getCourrielPersoEtudiant();
        if(mailEtudiant == null || !appConfigService.getConfigGenerale().isUtiliserMailPersoEtudiant())
            mailEtudiant = convention.getEtudiant().getMail();

        String template = "";
        if(typeFiche == 0) {
            template = TemplateMail.CODE_FICHE_EVAL_ETU;
            if(convention.getEnvoiMailEtudiant() != null && convention.getEnvoiMailEtudiant())
                template = TemplateMail.CODE_RAPPEL_FICHE_EVAL_ETU;
            if (convention.getCentreGestion().isOnlyMailCentreGestion()) {
                mailerService.sendAlerteValidation(convention.getCentreGestion().getMail(), convention, null, utilisateur, template);
            } else {
                mailerService.sendAlerteValidation(mailEtudiant, convention, null, utilisateur, template);
                convention.setEnvoiMailEtudiant(true);
                convention.setDateEnvoiMailEtudiant(new Date());
            }
        }else if(typeFiche == 1){
            template = TemplateMail.CODE_FICHE_EVAL_ENSEIGNANT;
            if(convention.getEnvoiMailTuteurPedago() != null && convention.getEnvoiMailTuteurPedago())
                template = TemplateMail.CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT;
            if (convention.getCentreGestion().isOnlyMailCentreGestion()) {
                mailerService.sendAlerteValidation(convention.getCentreGestion().getMail(), convention, null, utilisateur, template);
            } else {
                mailerService.sendAlerteValidation(convention.getEnseignant().getMail(), convention, null, utilisateur, template);
                convention.setEnvoiMailTuteurPedago(true);
                convention.setDateEnvoiMailTuteurPedago(new Date());
            }
        }else if(typeFiche == 2){
            template = TemplateMail.CODE_FICHE_EVAL_TUTEUR;
            if(convention.getEnvoiMailTuteurPro() != null && convention.getEnvoiMailTuteurPro())
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
        return reponseSupplementaireJpaRepository.findByQuestionAndConvention(idConvention,idQestion);
    }

    @PostMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire createReponseSupplementaire(@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        ReponseSupplementaire reponseSupplementaire = initReponseSupplementaire(idConvention,idQestion);
        setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }

    @PutMapping("/{idConvention}/reponseSupplementaire/{idQestion}")
    public ReponseSupplementaire updateReponseSupplementaire(@PathVariable("idConvention") int idConvention, @PathVariable("idQestion") int idQestion, @Valid @RequestBody ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        ReponseSupplementaire reponseSupplementaire = reponseSupplementaireJpaRepository.findByQuestionAndConvention(idConvention,idQestion);
        if (reponseSupplementaire == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "ReponseSupplementaire non trouvé");
        }
        setReponseSupplementaireData(reponseSupplementaire, reponseSupplementaireFormDto);
        return reponseSupplementaireJpaRepository.saveAndFlush(reponseSupplementaire);
    }

    private ReponseEvaluation initReponseEvaluation(int id) {

        Convention convention = conventionJpaRepository.findById(id);
        if (convention == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "Convention non trouvée");
        }
        FicheEvaluation ficheEvaluation = ficheEvaluationController.getByCentreGestion(convention.getCentreGestion().getId());

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
    private ReponseSupplementaire initReponseSupplementaire(int idConvention,int idQestion) {

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

    private void setReponseEvaluationEtudiantData(ReponseEvaluation reponseEvaluation, ReponseEtudiantFormDto reponseEtudiantFormDto) {

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

    private void setReponseEvaluationEnseignantData(ReponseEvaluation reponseEvaluation, ReponseEnseignantFormDto reponseEnseignantFormDto) {

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

    private void setReponseEvaluationEntrepriseData(ReponseEvaluation reponseEvaluation, ReponseEntrepriseFormDto reponseEntrepriseFormDto) {
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
    private void setReponseSupplementaireData(ReponseSupplementaire reponseSupplementaire, ReponseSupplementaireFormDto reponseSupplementaireFormDto) {
        reponseSupplementaire.setReponseTxt(reponseSupplementaireFormDto.getReponseTxt());
        reponseSupplementaire.setReponseBool(reponseSupplementaireFormDto.getReponseBool());
        reponseSupplementaire.setReponseInt(reponseSupplementaireFormDto.getReponseInt());
    }
}