package org.esup_portail.esup_stage.controller;

import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.esup_portail.esup_stage.dto.*;
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
import java.util.*;
import java.util.stream.Collectors;

@ApiController
@RequestMapping("/reponseEvaluation")
public class ReponseEvaluationController {

    private static final Logger logger = LogManager.getLogger(ReponseEvaluationController.class);

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
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(), reponseEvaluation.getConvention().getAvenants().getLast(), ou, typeFiche);
            reponseEvaluation.setImpressionEtudiant(true);
        }
        if (typeFiche == 1) {
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(), reponseEvaluation.getConvention().getAvenants().getLast(), ou, typeFiche);
            reponseEvaluation.setImpressionEnseignant(true);
        }
        if (typeFiche == 2) {
            impressionService.generateEvaluationPDF(reponseEvaluation.getConvention(), reponseEvaluation.getConvention().getAvenants().getLast(), ou, typeFiche);
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

    @PostMapping("/sendMailEvaluationEnMasse/typeFiche/{typeFiche}")
    @Secure(fonctions = {AppFonctionEnum.CONVENTION}, droits = {DroitEnum.MODIFICATION})
    public  ResponseEntity<SendMailEvaluationEnMasseResponseDto> sendMailEvaluationEnMasse(@PathVariable("typeFiche") int typeFiche, @RequestBody List<Integer> idConventions) {
        if (idConventions == null || idConventions.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Aucune convention fournie");
        }
        if (typeFiche < 0 || typeFiche > 2) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Type fiche invalide");
        }

        Utilisateur utilisateur = ServiceContext.getUtilisateur();

        List<Convention> conventions = conventionJpaRepository.findAllById(idConventions);

        SendMailEvaluationEnMasseResponseDto resp = new SendMailEvaluationEnMasseResponseDto();
        resp.setConventions(new ArrayList<>());
        SendMailEvaluationEnMasseResponseDto.Summary sum = new SendMailEvaluationEnMasseResponseDto.Summary();
        sum.requested = idConventions.size();
        sum.found = (int) conventions.stream().filter(Objects::nonNull).count();
        sum.typeFiche = typeFiche;

        // IDs introuvables => ERROR
        Set<Integer> foundIds = conventions.stream()
                .filter(Objects::nonNull)
                .map(Convention::getId)
                .collect(Collectors.toSet());

        idConventions.stream()
                .filter(id -> !foundIds.contains(id))
                .forEach(missingId -> {
                    SendMailEvaluationEnMasseResponseDto.Row r = new SendMailEvaluationEnMasseResponseDto.Row();
                    r.conventionId = missingId;
                    r.status = "ERROR";
                    r.reason = "not_found";
                    resp.getConventions().add(r);
                    sum.failed++;
                });

        for (Convention convention : conventions) {
            if (convention == null) continue;

            SendMailEvaluationEnMasseResponseDto.Row row = new SendMailEvaluationEnMasseResponseDto.Row();
            row.conventionId = convention.getId();

            try {
                boolean centreOnly = convention.getCentreGestion() != null
                        && convention.getCentreGestion().isOnlyMailCentreGestion();
                String centreMail = convention.getCentreGestion() != null
                        ? convention.getCentreGestion().getMail() : null;

                boolean rappel;
                String to;
                String template;

                switch (typeFiche) {
                    case 0: // Étudiant
                        rappel = Boolean.TRUE.equals(convention.getEnvoiMailEtudiant());
                        String mailEtudiant = convention.getCourrielPersoEtudiant();
                        if (mailEtudiant == null || !appConfigService.getConfigGenerale().isUtiliserMailPersoEtudiant()) {
                            mailEtudiant = (convention.getEtudiant() != null) ? convention.getEtudiant().getMail() : null;
                        }
                        to = centreOnly ? centreMail : mailEtudiant;
                        template = rappel ? TemplateMail.CODE_RAPPEL_FICHE_EVAL_ETU : TemplateMail.CODE_FICHE_EVAL_ETU;
                        break;

                    case 1: // Enseignant
                        rappel = Boolean.TRUE.equals(convention.getEnvoiMailTuteurPedago());
                        String mailEns = (convention.getEnseignant() != null) ? convention.getEnseignant().getMail() : null;
                        to = centreOnly ? centreMail : mailEns;
                        template = rappel ? TemplateMail.CODE_RAPPEL_FICHE_EVAL_ENSEIGNANT : TemplateMail.CODE_FICHE_EVAL_ENSEIGNANT;
                        break;

                    case 2: // Tuteur pro
                        rappel = Boolean.TRUE.equals(convention.getEnvoiMailTuteurPro());
                        String mailTuteur = (convention.getContact() != null) ? convention.getContact().getMail() : null;
                        to = centreOnly ? centreMail : mailTuteur;
                        template = rappel ? TemplateMail.CODE_RAPPEL_FICHE_EVAL_TUTEUR : TemplateMail.CODE_FICHE_EVAL_TUTEUR;
                        break;

                    default:
                        throw new AppException(HttpStatus.BAD_REQUEST, "Type fiche invalide");
                }

                row.template = template;
                row.rappel = rappel;
                row.toCentreGestion = centreOnly;
                row.to = to;

                // Email manquant => ERROR
                if (to == null || to.isBlank()) {
                    row.status = "ERROR";
                    row.reason = "no_email";
                    resp.getConventions().add(row);
                    sum.failed++;
                    continue;
                }

                // Envoi
                mailerService.sendAlerteValidation(to, convention, null, utilisateur, template);

                // MAJ flags si envoi au destinataire final (pas au centre)
                if (!centreOnly) {
                    switch (typeFiche) {
                        case 0 -> {
                            convention.setEnvoiMailEtudiant(true);
                            convention.setDateEnvoiMailEtudiant(new Date());
                        }
                        case 1 -> {
                            convention.setEnvoiMailTuteurPedago(true);
                            convention.setDateEnvoiMailTuteurPedago(new Date());
                        }
                        case 2 -> {
                            convention.setEnvoiMailTuteurPro(true);
                            convention.setDateEnvoiMailTuteurPro(new Date());
                        }
                    }
                }

                conventionJpaRepository.saveAndFlush(convention);

                row.status = "SENT";
                resp.getConventions().add(row);
                sum.sent++;

            } catch (Exception ex) {
                row.status = "ERROR";
                row.reason = "exception";
                row.error = ex.getMessage();
                resp.getConventions().add(row);
                sum.failed++;
            }
        }

        resp.setResume(sum);
        return ResponseEntity.ok(resp);
    }
}