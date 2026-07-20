package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.dto.SendMailTestDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailGroupeJpaRepository;
import org.esup_portail.esup_stage.repository.TemplateMailJpaRepository;
import org.esup_portail.esup_stage.repository.UtilisateurJpaRepository;
import org.esup_portail.esup_stage.service.evaluation.EvaluationService;
import org.esup_portail.esup_stage.service.impression.PreviewConventionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MailerServiceTest {

    private MailerService service;
    private TemplateMailJpaRepository templateMailJpaRepository;
    private TemplateMailGroupeJpaRepository templateMailGroupeJpaRepository;
    private UtilisateurJpaRepository utilisateurJpaRepository;
    private EvaluationService evaluationService;
    private AppliProperties appliProperties;

    @BeforeEach
    void setUp() {
        service = new MailerService();
        templateMailJpaRepository = mock(TemplateMailJpaRepository.class);
        templateMailGroupeJpaRepository = mock(TemplateMailGroupeJpaRepository.class);
        utilisateurJpaRepository = mock(UtilisateurJpaRepository.class);
        evaluationService = mock(EvaluationService.class);

        appliProperties = new AppliProperties();
        AppliProperties.MailerProperties mailer = new AppliProperties.MailerProperties();
        // pas de host configuré : l'envoi réel est court-circuité proprement
        mailer.setDisableDelivery(false);
        appliProperties.setMailer(mailer);

        service.appliProperties = appliProperties;
        service.templateMailJpaRepository = templateMailJpaRepository;
        service.templateMailGroupeJpaRepository = templateMailGroupeJpaRepository;
        service.utilisateurJpaRepository = utilisateurJpaRepository;
        service.evaluationService = evaluationService;

        when(evaluationService.buildEvaluationTuteurUrl(any(Convention.class))).thenReturn("http://eval");
    }

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private TemplateMail template(String code) {
        TemplateMail templateMail = new TemplateMail();
        templateMail.setId(1);
        templateMail.setCode(code);
        templateMail.setObjet("Objet");
        templateMail.setTexte("Texte");
        return templateMail;
    }

    private Convention conventionComplete() {
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        etudiant.setMail("alice@univ.fr");
        convention.setEtudiant(etudiant);
        Enseignant enseignant = new Enseignant();
        enseignant.setMail("prof@univ.fr");
        convention.setEnseignant(enseignant);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setMail("centre@univ.fr");
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    // ------------------------------------------------------------------
    // MailContext : projection entités -> variables de template
    // ------------------------------------------------------------------

    @Test
    void mailContextProjetteLaConvention() {
        Convention convention = conventionComplete();
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle("Stage");
        convention.setTypeConvention(typeConvention);
        convention.setSujetStage("Sujet du stage");
        convention.setDateDebutStage(date(2026, 3, 2));
        convention.setDateFinStage(date(2026, 8, 31));
        TempsTravail tempsTravail = new TempsTravail();
        tempsTravail.setLibelle("Temps plein");
        convention.setTempsTravail(tempsTravail);
        Etape etape = new Etape();
        EtapeId etapeId = new EtapeId();
        etapeId.setCode("L3");
        etape.setId(etapeId);
        etape.setLibelle("Licence 3");
        convention.setEtape(etape);
        Contact tuteur = new Contact();
        tuteur.setNom("Martin");
        tuteur.setMail("tuteur@acme.fr");
        convention.setContact(tuteur);
        Contact signataire = new Contact();
        signataire.setNom("Signataire");
        convention.setSignataire(signataire);
        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        convention.setStructure(structure);
        Service serviceAccueil = new Service();
        serviceAccueil.setNom("R&D");
        Pays pays = new Pays();
        pays.setLib("France");
        serviceAccueil.setPays(pays);
        convention.setService(serviceAccueil);
        convention.setCourrielPersoEtudiant("perso@mail.fr");
        convention.setTelEtudiant("0102030405");

        appliProperties.setUrl("https://stage.univ.fr");
        Avenant avenant = new Avenant();
        avenant.setId(7);
        Utilisateur modificateur = new Utilisateur();
        modificateur.setNom("Admin");
        modificateur.setPrenom("Super");

        MailerService.MailContext contexte =
                new MailerService.MailContext(appliProperties, convention, avenant, modificateur, "http://eval");

        assertThat(contexte.getConvention().getNumero()).isEqualTo("42");
        assertThat(contexte.getConvention().getTypeStage()).isEqualTo("Stage");
        assertThat(contexte.getConvention().getPaysAccueil()).isEqualTo("France");
        assertThat(contexte.getConvention().getEtape()).isEqualTo("L3 - Licence 3");
        assertThat(contexte.getConvention().getDateDebut()).isEqualTo("02/03/2026");
        assertThat(contexte.getConvention().getDateFin()).isEqualTo("31/08/2026");
        assertThat(contexte.getConvention().getTempsTravail()).isEqualTo("Temps plein");
        assertThat(contexte.getConvention().getLien()).isEqualTo("https://stage.univ.fr/frontend/#/conventions/42");
        assertThat(contexte.getTuteurPro().getNom()).isEqualTo("Martin");
        assertThat(contexte.getTuteurPro().getEtabAccueil()).isEqualTo("ACME");
        assertThat(contexte.getTuteurPro().getServiceAccueil()).isEqualTo("R&D");
        assertThat(contexte.getSignataire().getNom()).isEqualTo("Signataire");
        assertThat(contexte.getEtudiant().getNom()).isEqualTo("Durand");
        assertThat(contexte.getEtudiant().getMail()).as("le mail perso prime").isEqualTo("perso@mail.fr");
        assertThat(contexte.getEtudiant().getTel()).isEqualTo("0102030405");
        assertThat(contexte.getModifiePar().getNom()).isEqualTo("Admin");
        assertThat(contexte.getAvenant().getNumero()).isEqualTo("7");
        assertThat(contexte.getLienEvaluationTuteur()).isEqualTo("http://eval");
    }

    @Test
    void mailContextSansMailPersoUtiliseLeMailInstitutionnel() {
        Convention convention = conventionComplete();

        MailerService.MailContext contexte =
                new MailerService.MailContext(appliProperties, convention, null, "http://eval");

        assertThat(contexte.getEtudiant().getMail()).isEqualTo("alice@univ.fr");
        assertThat(contexte.getModifiePar()).isNull();
    }

    @Test
    void mailContextSansConventionResteVierge() {
        MailerService.MailContext contexte =
                new MailerService.MailContext(appliProperties, null, null, "http://eval");

        assertThat(contexte.getConvention().getNumero()).isNull();
        assertThat(contexte.getLienEvaluationTuteur()).isEmpty();
    }

    // ------------------------------------------------------------------
    // isAlerteActif : matrice code template -> préférence du personnel
    // ------------------------------------------------------------------

    @Test
    void isAlerteActifCouvreTousLesCodesDeTemplate() {
        record Cas(String code, java.util.function.BiConsumer<PersonnelCentreGestion, Boolean> setter) {}
        List<Cas> cas = List.of(
                new Cas(TemplateMail.CODE_AVENANT_VALIDATION, PersonnelCentreGestion::setValidationAvenant),
                new Cas(TemplateMail.CODE_CONVENTION_VALID_ADMINISTRATIVE, PersonnelCentreGestion::setValidationAdministrativeConvention),
                new Cas(TemplateMail.CODE_CONVENTION_DEVALID_ADMINISTRATIVE, PersonnelCentreGestion::setValidationAdministrativeConvention),
                new Cas(TemplateMail.CODE_CONVENTION_VALID_PEDAGOGIQUE, PersonnelCentreGestion::setValidationPedagogiqueConvention),
                new Cas(TemplateMail.CODE_CONVENTION_DEVALID_PEDAGOGIQUE, PersonnelCentreGestion::setValidationPedagogiqueConvention),
                new Cas(TemplateMail.CODE_CONVENTION_VERIF_ADMINISTRATIVE, PersonnelCentreGestion::setVerificationAdministrativeConvention),
                new Cas(TemplateMail.CODE_CONVENTION_DEVERIF_ADMINISTRATIVE, PersonnelCentreGestion::setVerificationAdministrativeConvention),
                new Cas(TemplateMail.CODE_ETU_CREA_AVENANT, PersonnelCentreGestion::setCreationAvenantEtudiant),
                new Cas(TemplateMail.CODE_ETU_CREA_CONVENTION, PersonnelCentreGestion::setCreationConventionEtudiant),
                new Cas(TemplateMail.CODE_ETU_MODIF_AVENANT, PersonnelCentreGestion::setModificationAvenantEtudiant),
                new Cas(TemplateMail.CODE_ETU_MODIF_CONVENTION, PersonnelCentreGestion::setModificationConventionEtudiant),
                new Cas(TemplateMail.CODE_GES_CREA_AVENANT, PersonnelCentreGestion::setCreationAvenantGestionnaire),
                new Cas(TemplateMail.CODE_GES_CREA_CONVENTION, PersonnelCentreGestion::setCreationConventionGestionnaire),
                new Cas(TemplateMail.CODE_GES_MODIF_AVENANT, PersonnelCentreGestion::setModificationAvenantGestionnaire),
                new Cas(TemplateMail.CODE_GES_MODIF_CONVENTION, PersonnelCentreGestion::setModificationConventionGestionnaire),
                new Cas(TemplateMail.CODE_CONVENTION_SIGNEE, PersonnelCentreGestion::setConventionSignee),
                new Cas(TemplateMail.CODE_CHANGEMENT_ENSEIGNANT, PersonnelCentreGestion::setChangementEnseignant),
                new Cas(TemplateMail.CODE_EVAL_TUTEUR_REMPLIE, PersonnelCentreGestion::setEvalTuteurRemplie),
                new Cas(TemplateMail.CODE_EVAL_ENSEIGNANT_REMPLIE, PersonnelCentreGestion::setEvalEnsRemplie),
                new Cas(TemplateMail.CODE_EVAL_ETU_REMPLIE, PersonnelCentreGestion::setEvalEtuRemplie),
                new Cas(TemplateMail.CODE_EVAL_REMPLIES, PersonnelCentreGestion::setEvalRemplies)
        );

        for (Cas c : cas) {
            PersonnelCentreGestion actif = new PersonnelCentreGestion();
            c.setter().accept(actif, true);
            assertThat(service.isAlerteActif(actif, c.code()))
                    .as("alerte activée pour %s", c.code()).isTrue();

            PersonnelCentreGestion inactif = new PersonnelCentreGestion();
            c.setter().accept(inactif, false);
            assertThat(service.isAlerteActif(inactif, c.code()))
                    .as("alerte désactivée pour %s", c.code()).isFalse();

            PersonnelCentreGestion nonRenseigne = new PersonnelCentreGestion();
            assertThat(service.isAlerteActif(nonRenseigne, c.code()))
                    .as("alerte non renseignée pour %s", c.code()).isFalse();
        }

        assertThat(service.isAlerteActif(new PersonnelCentreGestion(), "CODE_INCONNU")).isFalse();
    }

    // ------------------------------------------------------------------
    // sendAlerteValidation / sendValidationMail / sendMailGroupe
    // ------------------------------------------------------------------

    @Test
    void sendAlerteValidationEchoueSiTemplateInconnu() {
        when(templateMailJpaRepository.findByCode("ABSENT")).thenReturn(null);

        assertThatThrownBy(() -> service.sendAlerteValidation("a@b.fr", conventionComplete(), null, "ABSENT"))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void sendAlerteValidationSansDestinataireNEnvoieRien() {
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(template("CODE"));

        assertThatCode(() -> {
            service.sendAlerteValidation(null, conventionComplete(), null, "CODE");
            service.sendAlerteValidation("", conventionComplete(), null, "CODE");
            service.sendAlerteValidation("null", conventionComplete(), null, "CODE");
        }).doesNotThrowAnyException();
    }

    @Test
    void sendAlerteValidationSansHostConfigureNeLeveRien() {
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(template("CODE"));

        assertThatCode(() -> service.sendAlerteValidation("dest@univ.fr", conventionComplete(), null, "CODE"))
                .doesNotThrowAnyException();
    }

    @Test
    void sendValidationMailRouteVersEtudiantEnseignantEtGestionnaires() {
        Convention convention = conventionComplete();
        PersonnelCentreGestion gestionnaire = new PersonnelCentreGestion();
        gestionnaire.setUidPersonnel("ges1");
        gestionnaire.setMail("ges1@univ.fr");
        gestionnaire.setAlertesMail(true);
        gestionnaire.setValidationAvenant(true);
        PersonnelCentreGestion sansAlerte = new PersonnelCentreGestion();
        sansAlerte.setUidPersonnel("ges2");
        sansAlerte.setAlertesMail(false);
        convention.getCentreGestion().setPersonnels(List.of(gestionnaire, sansAlerte));

        Utilisateur simple = new Utilisateur();
        simple.setUid("ges1");
        when(utilisateurJpaRepository.findByUids(anyList())).thenReturn(List.of(simple));
        when(templateMailJpaRepository.findByCode(TemplateMail.CODE_AVENANT_VALIDATION))
                .thenReturn(template(TemplateMail.CODE_AVENANT_VALIDATION));

        service.sendValidationMail(convention, null, TemplateMail.CODE_AVENANT_VALIDATION,
                true, true, true, false);

        // 1 gestionnaire + étudiant + enseignant = 3 recherches de template
        verify(templateMailJpaRepository, times(3)).findByCode(TemplateMail.CODE_AVENANT_VALIDATION);
    }

    @Test
    void sendValidationMailRespGestionnaireEstFiltreSelonLeFlag() {
        Convention convention = conventionComplete();
        PersonnelCentreGestion respGes = new PersonnelCentreGestion();
        respGes.setUidPersonnel("resp1");
        respGes.setMail("resp1@univ.fr");
        respGes.setAlertesMail(true);
        respGes.setValidationAvenant(true);
        convention.getCentreGestion().setPersonnels(List.of(respGes));

        Utilisateur responsable = new Utilisateur();
        responsable.setUid("resp1");
        Role roleResp = new Role();
        roleResp.setCode(Role.RESP_GES);
        responsable.setRoles(List.of(roleResp));
        when(utilisateurJpaRepository.findByUids(anyList())).thenReturn(List.of(responsable));
        when(templateMailJpaRepository.findByCode(TemplateMail.CODE_AVENANT_VALIDATION))
                .thenReturn(template(TemplateMail.CODE_AVENANT_VALIDATION));

        // resp. gestionnaire exclu : aucun envoi gestionnaire, pas d'étudiant/enseignant
        service.sendValidationMail(convention, null, TemplateMail.CODE_AVENANT_VALIDATION,
                false, false, true, false);
        verify(templateMailJpaRepository, times(0)).findByCode(TemplateMail.CODE_AVENANT_VALIDATION);

        // resp. gestionnaire inclus
        service.sendValidationMail(convention, null, TemplateMail.CODE_AVENANT_VALIDATION,
                false, false, false, true);
        verify(templateMailJpaRepository, times(1)).findByCode(TemplateMail.CODE_AVENANT_VALIDATION);
    }

    @Test
    void sendValidationMailCentreEnModeMailUniqueEnvoieAuCentre() {
        Convention convention = conventionComplete();
        convention.getCentreGestion().setOnlyMailCentreGestion(true);
        PersonnelCentreGestion gestionnaire = new PersonnelCentreGestion();
        gestionnaire.setUidPersonnel("ges1");
        gestionnaire.setAlertesMail(true);
        convention.getCentreGestion().setPersonnels(List.of(gestionnaire));
        when(utilisateurJpaRepository.findByUids(anyList())).thenReturn(List.of());
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(template("CODE"));

        service.sendValidationMail(convention, null, "CODE", false, false, true, false);

        verify(templateMailJpaRepository, times(1)).findByCode("CODE");
    }

    @Test
    void sendValidationMailAvecUtilisateurContexteSuitLesMemesRegles() {
        Convention convention = conventionComplete();
        convention.getCentreGestion().setPersonnels(List.of());
        when(utilisateurJpaRepository.findByUids(anyList())).thenReturn(List.of());
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(template("CODE"));
        Utilisateur contexte = new Utilisateur();
        contexte.setNom("Contexte");

        service.sendValidationMail(convention, null, contexte, "CODE", true, true, true, true);

        // étudiant + enseignant
        verify(templateMailJpaRepository, times(2)).findByCode("CODE");
    }

    @Test
    void sendMailGroupeEchoueSiTemplateInconnu() {
        when(templateMailGroupeJpaRepository.findByCode("ABSENT")).thenReturn(null);

        assertThatThrownBy(() -> service.sendMailGroupe("a@b.fr", conventionComplete(), null, "ABSENT", new byte[0]))
                .isInstanceOf(AppException.class);
    }

    @Test
    void sendMailGroupeSansDestinataireNEnvoieRien() {
        TemplateMailGroupe templateMailGroupe = new TemplateMailGroupe();
        templateMailGroupe.setId(1);
        templateMailGroupe.setCode("GRP");
        templateMailGroupe.setObjet("Objet");
        templateMailGroupe.setTexte("Texte");
        when(templateMailGroupeJpaRepository.findByCode("GRP")).thenReturn(templateMailGroupe);

        assertThatCode(() -> service.sendMailGroupe("", conventionComplete(), null, "GRP", new byte[0]))
                .doesNotThrowAnyException();
    }

    // ------------------------------------------------------------------
    // envoi effectif : rendu des templates et configuration SMTP
    // ------------------------------------------------------------------

    private void configureFreemarker() {
        FreeMarkerConfigurer freeMarkerConfigurer = mock(FreeMarkerConfigurer.class);
        when(freeMarkerConfigurer.getConfiguration())
                .thenReturn(new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32));
        service.freeMarkerConfigurer = freeMarkerConfigurer;
    }

    @Test
    void laLivraisonDesactiveeJournaliseSeulement() {
        appliProperties.getMailer().setHost("smtp.univ.fr");
        appliProperties.getMailer().setDisableDelivery(true);
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(template("CODE"));

        assertThatCode(() -> service.sendAlerteValidation("dest@univ.fr", conventionComplete(), null, "CODE"))
                .doesNotThrowAnyException();
    }

    @Test
    void unEnvoiReelSansServeurSmtpEstUneErreurTechnique() {
        AppliProperties.MailerProperties mailer = appliProperties.getMailer();
        mailer.setHost("localhost");
        mailer.setPort(1); // port fermé : l'envoi échoue au moment de la connexion
        mailer.setProtocol("smtp");
        mailer.setAuth(true);
        mailer.setUsername("user");
        mailer.setPassword("secret");
        mailer.setFrom("noreply@univ.fr");
        mailer.setSslEnable(true);
        mailer.setDeliveryAddress("redirect@univ.fr");
        configureFreemarker();

        TemplateMail templateMail = template("CODE");
        templateMail.setObjet("Convention ${convention.numero}");
        templateMail.setTexte("$IF etudiant.nom??$Bonjour ${etudiant.nom}$ELSE Inconnu$ENDIF");
        when(templateMailJpaRepository.findByCode("CODE")).thenReturn(templateMail);

        assertThatThrownBy(() -> service.sendAlerteValidation("dest@univ.fr", conventionComplete(), null, "CODE"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("technique");
    }

    @Test
    void lEnvoiGroupeAvecArchiveEchoueSansServeurSmtp() {
        AppliProperties.MailerProperties mailer = appliProperties.getMailer();
        mailer.setHost("localhost");
        mailer.setPort(1);
        mailer.setFrom("noreply@univ.fr");
        configureFreemarker();
        TemplateMailGroupe templateMailGroupe = new TemplateMailGroupe();
        templateMailGroupe.setId(2);
        templateMailGroupe.setCode("GRP");
        templateMailGroupe.setObjet("Objet");
        templateMailGroupe.setTexte("Texte");
        when(templateMailGroupeJpaRepository.findByCode("GRP")).thenReturn(templateMailGroupe);

        assertThatThrownBy(() -> service.sendMailGroupe("dest@univ.fr", conventionComplete(), new Utilisateur(), "GRP", new byte[]{1}))
                .isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // sendTest : mail de prévisualisation sur convention fictive
    // ------------------------------------------------------------------

    @Test
    void leMailDeTestSAppuieSurUneConventionFictive() {
        CentreGestionJpaRepository centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        PreviewConventionFactory previewConventionFactory = mock(PreviewConventionFactory.class);
        service.centreGestionJpaRepository = centreGestionJpaRepository;
        service.previewConventionFactory = previewConventionFactory;
        appliProperties.getMailer().setHost("smtp.univ.fr");
        appliProperties.getMailer().setDisableDelivery(true);

        when(templateMailJpaRepository.findByCode("TEST")).thenReturn(template("TEST"));
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(new CentreGestion());
        Convention fictive = conventionComplete();
        fictive.setSignataire(new Contact()); // sans téléphone : complété automatiquement
        when(previewConventionFactory.createFictionalConvention(any())).thenReturn(fictive);
        Avenant avenantFictif = new Avenant();
        avenantFictif.setId(1);
        when(previewConventionFactory.createFictionalAvenant(fictive)).thenReturn(avenantFictif);

        SendMailTestDto dto = new SendMailTestDto();
        dto.setTemplateMail("TEST");
        dto.setTo("dest@univ.fr");
        assertThatCode(() -> service.sendTest(dto, new Utilisateur())).doesNotThrowAnyException();
        verify(previewConventionFactory).createFictionalConvention(any());

        // destinataire vide : la convention fictive n'est même pas construite
        SendMailTestDto sansDestinataire = new SendMailTestDto();
        sansDestinataire.setTemplateMail("TEST");
        sansDestinataire.setTo("");
        service.sendTest(sansDestinataire, new Utilisateur());
        verify(previewConventionFactory, times(1)).createFictionalConvention(any());

        // template inconnu
        SendMailTestDto inconnu = new SendMailTestDto();
        inconnu.setTemplateMail("ABSENT");
        assertThatThrownBy(() -> service.sendTest(inconnu, new Utilisateur())).isInstanceOf(AppException.class);
    }
}
