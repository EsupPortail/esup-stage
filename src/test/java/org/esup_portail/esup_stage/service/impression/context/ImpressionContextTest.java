package org.esup_portail.esup_stage.service.impression.context;

import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.esup_portail.esup_stage.enums.TypeQuestionEvaluation;
import org.esup_portail.esup_stage.model.*;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests du mapping entités → contexte d'impression (données injectées dans
 * les templates de convention/avenant PDF).
 */
class ImpressionContextTest {

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private CentreGestion centreEtablissement() {
        CentreGestion centre = new CentreGestion();
        centre.setNomCentre("Université de Lorraine");
        centre.setVoie("34 cours Léopold");
        centre.setCodePostal("54000");
        centre.setCommune("Nancy");
        centre.setNomViseur("Presidente");
        centre.setPrenomViseur("Paula");
        centre.setQualiteViseur("Présidente");
        return centre;
    }

    private Convention conventionMinimale() {
        Convention convention = new Convention();
        convention.setId(7);
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle("Stage obligatoire");
        convention.setTypeConvention(typeConvention);
        convention.setEtudiant(new Etudiant());
        convention.setService(new Service());
        convention.setStructure(new Structure());
        convention.setCentreGestion(new CentreGestion());
        convention.setPeriodeStage(List.of());
        return convention;
    }

    @Test
    void conventionMinimaleUtiliseLesValeursParDefautEtLesReplis() {
        Convention convention = conventionMinimale();
        CentreGestion etablissement = centreEtablissement();

        ImpressionContext contexte = new ImpressionContext(convention, null, etablissement, null, null);
        ImpressionContext.ConventionContext ctx = contexte.getConvention();

        assertThat(ctx.getId()).isEqualTo("7");
        // repli sur le centre établissement quand la convention ne porte pas l'adresse
        assertThat(ctx.getAdresseEtabRef()).isEqualTo("34 cours Léopold 54000 Nancy");
        assertThat(ctx.getNomEtabRef()).isEqualTo("Université de Lorraine");
        assertThat(ctx.getInterruptionStage()).isEqualTo("Non");
        assertThat(ctx.getConfidentiel()).isEqualTo("Non");
        assertThat(ctx.getConventionValidee()).isEqualTo("Non");
        assertThat(ctx.getDateDebutStage()).isNull();
        assertThat(ctx.getMontantGratification()).as("pas de gratification").isNull();
        assertThat(ctx.getTypeConventionLibelle()).isEqualTo("Stage obligatoire");
        assertThat(ctx.getLangueConvention()).isEqualTo("");

        // contacts absents : contextes vides, pas d'erreur
        assertThat(contexte.getContact().getNom()).isEmpty();
        assertThat(contexte.getSignataire().getNom()).isEmpty();
        assertThat(contexte.getEnseignant().getNom()).isEmpty();
        // pas d'avenant : contexte vierge
        assertThat(contexte.getAvenant().getId()).isNull();
        // pas de réponse d'évaluation : contexte vierge
        assertThat(contexte.getReponse().getReponseEnt1()).isNull();
    }

    @Test
    void conventionCompleteEstEntierementProjetee() {
        Convention convention = conventionMinimale();
        convention.setAdresseEtabRef("1 rue de la Paix");
        convention.setNomEtabRef("Etablissement X");
        convention.setAnnee("2025/2026");
        convention.setDateDebutStage(date(2026, 3, 2));
        convention.setDateFinStage(date(2026, 8, 31));
        convention.setInterruptionStage(true);
        convention.setConfidentiel(true);
        convention.setGratificationStage(true);
        convention.setMontantGratification("600");
        Devise devise = new Devise();
        devise.setLibelle("Euro");
        convention.setDevise(devise);
        convention.setNbJoursHebdo(NbJoursHebdoEnum.CINQ);
        convention.setDureeStage(120);
        convention.setCreditECTS(new java.math.BigDecimal("30"));
        convention.setSujetStage("Développement d'une application");
        convention.setValidationPedagogique(true);
        convention.setValidationConvention(true);

        ConventionNomenclature nomenclature = new ConventionNomenclature();
        nomenclature.setModeValidationStage("Soutenance");
        nomenclature.setModeVersGratification("Virement");
        nomenclature.setUniteGratification("€/mois");
        nomenclature.setUniteDureeGratification("mois");
        nomenclature.setNatureTravail("Développement");
        nomenclature.setOrigineStage("Proposé");
        nomenclature.setTempsTravail("Temps plein");
        nomenclature.setTheme("Informatique");
        convention.setNomenclature(nomenclature);

        Etape etape = new Etape();
        EtapeId etapeId = new EtapeId();
        etapeId.setCode("L3INFO");
        etape.setId(etapeId);
        etape.setLibelle("Licence 3 Informatique");
        convention.setEtape(etape);

        Ufr ufr = new Ufr();
        UfrId ufrId = new UfrId();
        ufrId.setCode("SCI");
        ufr.setId(ufrId);
        ufr.setLibelle("Sciences");
        convention.setUfr(ufr);

        LangueConvention langue = new LangueConvention();
        langue.setLibelle("Français");
        convention.setLangueConvention(langue);

        PeriodeInterruptionStage interruption = new PeriodeInterruptionStage();
        interruption.setDateDebutInterruption(date(2026, 4, 1));
        interruption.setDateFinInterruption(date(2026, 4, 15));
        convention.getPeriodeInterruptionStages().add(interruption);

        PeriodeStage periode = new PeriodeStage();
        periode.setDateDebut(date(2026, 3, 2));
        periode.setDateFin(date(2026, 3, 31));
        periode.setNbHeuresJournalieres(7);
        convention.setPeriodeStage(List.of(periode));

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        etudiant.setDateNais(date(2004, 5, 12));
        convention.setEtudiant(etudiant);

        Pays pays = new Pays();
        pays.setLib("France");
        Service service = new Service();
        service.setNom("Service R&D");
        service.setCommune("Nancy");
        service.setPays(pays);
        convention.setService(service);

        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        Effectif effectif = new Effectif();
        effectif.setLibelle("50-100");
        structure.setEffectif(effectif);
        structure.setPays(pays);
        StatutJuridique statutJuridique = new StatutJuridique();
        statutJuridique.setLibelle("SAS");
        structure.setStatutJuridique(statutJuridique);
        TypeStructure typeStructure = new TypeStructure();
        typeStructure.setLibelle("Entreprise privée");
        structure.setTypeStructure(typeStructure);
        convention.setStructure(structure);

        Civilite civilite = new Civilite();
        civilite.setLibelle("Madame");
        Contact contact = new Contact();
        contact.setNom("Martin");
        contact.setPrenom("Claire");
        contact.setCivilite(civilite);
        convention.setContact(contact);
        convention.setSignataire(contact);

        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Professeur");
        enseignant.setTypePersonne("Enseignant-chercheur");
        Affectation affectation = new Affectation();
        affectation.setLibelle("IUT Charlemagne");
        enseignant.setAffectation(affectation);
        convention.setEnseignant(enseignant);

        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setNomCentre("Centre Sciences");
        centreGestion.setValidationPedagogique(true);
        centreGestion.setValidationConvention(true);
        FicheEvaluation ficheEvaluation = new FicheEvaluation();
        ficheEvaluation.setQuestionEnt1(true);
        centreGestion.setFicheEvaluation(ficheEvaluation);
        convention.setCentreGestion(centreGestion);

        ReponseEvaluation reponseEvaluation = new ReponseEvaluation();
        reponseEvaluation.setReponseEnt1(4);
        reponseEvaluation.setReponseEnt19("Très bon stage");
        convention.setReponseEvaluation(reponseEvaluation);

        ReponseSupplementaire reponseSupplementaire = new ReponseSupplementaire();
        ReponseSupplementaireId reponseSupplementaireId = new ReponseSupplementaireId();
        reponseSupplementaireId.setIdQuestionSupplementaire(3);
        reponseSupplementaireId.setIdConvention(7);
        reponseSupplementaire.setId(reponseSupplementaireId);
        reponseSupplementaire.setReponseTxt("Oui bien sûr");
        convention.getReponseSupplementaires().add(reponseSupplementaire);

        QuestionSupplementaire questionSupplementaire = new QuestionSupplementaire();
        questionSupplementaire.setId(3);
        questionSupplementaire.setQuestion("Un tuteur est-il désigné ?");
        questionSupplementaire.setTypeQuestion("txt");

        QuestionEvaluation questionEvaluation = new QuestionEvaluation();
        questionEvaluation.setCode("Q1");
        questionEvaluation.setTexte("Qualité de l'accueil");
        questionEvaluation.setType(TypeQuestionEvaluation.values()[0]);

        // la validation "convention validée" est pilotée par le centre établissement
        CentreGestion etablissement = centreEtablissement();
        etablissement.setValidationPedagogique(true);
        etablissement.setValidationConvention(true);

        ImpressionContext contexte = new ImpressionContext(
                convention, null, etablissement,
                List.of(questionSupplementaire), List.of(questionEvaluation));

        ImpressionContext.ConventionContext ctx = contexte.getConvention();
        assertThat(ctx.getAdresseEtabRef()).isEqualTo("1 rue de la Paix");
        assertThat(ctx.getNomEtabRef()).isEqualTo("Etablissement X");
        assertThat(ctx.getDateDebutStage()).isEqualTo("02/03/2026");
        assertThat(ctx.getDateFinStage()).isEqualTo("31/08/2026");
        assertThat(ctx.getInterruptionStage()).isEqualTo("Oui");
        assertThat(ctx.getConfidentiel()).isEqualTo("Oui");
        assertThat(ctx.getConventionValidee()).isEqualTo("Oui");
        assertThat(ctx.getMontantGratification()).isEqualTo("600");
        assertThat(ctx.getDeviseGratification()).isEqualTo("Euro");
        assertThat(ctx.getModeVersGratificationLibelle()).isEqualTo("Virement");
        assertThat(ctx.getUniteGratificationLibelle()).isEqualTo("€/mois");
        assertThat(ctx.getNbJoursHebdo()).isEqualTo("5");
        assertThat(ctx.getDureeStage()).isEqualTo("120");
        assertThat(ctx.getCreditECTS()).isEqualTo("30");
        assertThat(ctx.getEtapeLibelle()).isEqualTo("Licence 3 Informatique");
        assertThat(ctx.getEtapeCode()).isEqualTo("L3INFO");
        assertThat(ctx.getUfrLibelle()).isEqualTo("Sciences");
        assertThat(ctx.getUfrCode()).isEqualTo("SCI");
        assertThat(ctx.getLangueConvention()).isEqualTo("Français");
        assertThat(ctx.getModeValidationStageLibelle()).isEqualTo("Soutenance");
        assertThat(ctx.getPeriodesInterruptions()).hasSize(1);
        assertThat(ctx.getPeriodesInterruptions().get(0).getDateDebutInterruption()).isEqualTo("01/04/2026");
        assertThat(ctx.getHoraireIrregulier()).hasSize(1);
        assertThat(ctx.getHoraireIrregulier().get(0).getNbHeuresJournalieres()).isEqualTo(7);

        assertThat(contexte.getEtudiant().getNom()).isEqualTo("Durand");
        assertThat(contexte.getEtudiant().getDateNais()).isEqualTo("12/05/2004");
        assertThat(contexte.getService().getPaysLibelle()).isEqualTo("France");
        assertThat(contexte.getStructure().getRaisonSociale()).isEqualTo("ACME");
        assertThat(contexte.getStructure().getEffectifLibelle()).isEqualTo("50-100");
        assertThat(contexte.getStructure().getStatutJuridiqueLibelle()).isEqualTo("SAS");
        assertThat(contexte.getStructure().getTypeStructureLibelle()).isEqualTo("Entreprise privée");
        assertThat(contexte.getContact().getCiviliteLibelle()).isEqualTo("Madame");
        assertThat(contexte.getSignataire().getNom()).isEqualTo("Martin");
        assertThat(contexte.getEnseignant().getAffectationLibelle()).isEqualTo("IUT Charlemagne");
        assertThat(contexte.getEnseignant().getFonction()).isEqualTo("Enseignant-chercheur");
        assertThat(contexte.getCentreGestion().getNomCentre()).isEqualTo("Centre Sciences");
        assertThat(contexte.getCentreGestion().getNomPresidentEtab()).isEqualTo("Presidente");

        assertThat(contexte.getReponse().getReponseEnt1()).isEqualTo(4);
        assertThat(contexte.getReponse().getReponseEnt19()).isEqualTo("Très bon stage");
        assertThat(contexte.getFicheEvaluation().getQuestionEnt1()).isTrue();
        assertThat(contexte.getQuestionsSupplementaires()).hasSize(1);
        assertThat(contexte.getQuestionsSupplementaires().get(0).getQuestion()).isEqualTo("Un tuteur est-il désigné ?");
        assertThat(contexte.getReponsesSupplementaires()).hasSize(1);
        assertThat(contexte.getReponsesSupplementaires().get(0).getIdQuestionSupplementaire()).isEqualTo(3);
        assertThat(contexte.getQuestionEvaluations()).hasSize(1);
        assertThat(contexte.getQuestionEvaluations().get(0).getCode()).isEqualTo("Q1");
    }

    @Test
    void validationPedagogiqueSeuleRefuseeRendNon() {
        Convention convention = conventionMinimale();
        convention.setValidationPedagogique(false);
        convention.setValidationConvention(true);
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setValidationPedagogique(true);
        centreGestion.setValidationConvention(true);
        // le centre de la convention ne pilote pas la validation : c'est le centre établissement
        ImpressionContext contexte = new ImpressionContext(convention, null, centreGestion, null, null);

        assertThat(contexte.getConvention().getConventionValidee()).isEqualTo("Non");
    }

    @Test
    void avenantCompletAvecToutesLesModifications() {
        Avenant avenant = new Avenant();
        avenant.setId(12);
        avenant.setSujetStage("Nouveau sujet");
        avenant.setMotifAvenant("Prolongation");
        avenant.setDateDebutStage(date(2026, 9, 1));
        avenant.setDateFinStage(date(2026, 12, 31));
        avenant.setRupture(true);
        avenant.setDateRupture(date(2026, 10, 15));
        avenant.setCommentaireRupture("Rupture anticipée");
        avenant.setModificationSujet(true);
        avenant.setModificationPeriode(true);
        avenant.setModificationMontantGratification(true);
        avenant.setMontantGratification("800");
        UniteGratification uniteGratification = new UniteGratification();
        uniteGratification.setLibelle("€/mois");
        avenant.setUniteGratification(uniteGratification);
        UniteDuree uniteDuree = new UniteDuree();
        uniteDuree.setLibelle("mois");
        avenant.setUniteDuree(uniteDuree);
        Devise devise = new Devise();
        devise.setLibelle("Euro");
        avenant.setDevise(devise);
        ModeVersGratification modeVersGratification = new ModeVersGratification();
        modeVersGratification.setLibelle("Chèque");
        avenant.setModeVersGratification(modeVersGratification);
        avenant.setModificationLieu(true);
        Service service = new Service();
        service.setNom("Nouveau service");
        avenant.setService(service);
        avenant.setModificationSalarie(true);
        Contact contact = new Contact();
        contact.setNom("Tuteur2");
        avenant.setContact(contact);
        avenant.setModificationEnseignant(true);
        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Enseignant2");
        avenant.setEnseignant(enseignant);
        PeriodeInterruptionAvenant interruption = new PeriodeInterruptionAvenant();
        interruption.setDateDebutInterruption(date(2026, 11, 1));
        interruption.setDateFinInterruption(date(2026, 11, 15));
        avenant.getPeriodeInterruptionAvenants().add(interruption);

        ImpressionContext contexte = new ImpressionContext(null, avenant, null, null, null);
        ImpressionContext.AvenantContext ctx = contexte.getAvenant();

        assertThat(ctx.getId()).isEqualTo("12");
        assertThat(ctx.getSujetStage()).isEqualTo("Nouveau sujet");
        assertThat(ctx.getDateDebutStage()).isEqualTo("01/09/2026");
        assertThat(ctx.getDateFinStage()).isEqualTo("31/12/2026");
        assertThat(ctx.isRupture()).isTrue();
        assertThat(ctx.getDateRupture()).isEqualTo("15/10/2026");
        assertThat(ctx.getMontantGratification()).isEqualTo("800");
        assertThat(ctx.getUniteGratificationLibelle()).isEqualTo("€/mois");
        assertThat(ctx.getUniteDureeGratificationLibelle()).isEqualTo("mois");
        assertThat(ctx.getDeviseGratification()).isEqualTo("Euro");
        assertThat(ctx.getModeVersGratificationLibelle()).isEqualTo("Chèque");
        assertThat(ctx.getService().getNom()).isEqualTo("Nouveau service");
        assertThat(ctx.getContact().getNom()).isEqualTo("Tuteur2");
        assertThat(ctx.getEnseignant().getNom()).isEqualTo("Enseignant2");
        assertThat(ctx.getPeriodesInterruptions()).hasSize(1);
        assertThat(ctx.getPeriodesInterruptions().get(0).getDateFinInterruption()).isEqualTo("15/11/2026");

        // pas de convention : contexte convention vierge
        assertThat(contexte.getConvention().getId()).isNull();
    }

    @Test
    void avenantSansModificationLaisseLesSousContextesVides() {
        Avenant avenant = new Avenant();
        avenant.setId(13);

        ImpressionContext contexte = new ImpressionContext(null, avenant, null, null, null);
        ImpressionContext.AvenantContext ctx = contexte.getAvenant();

        assertThat(ctx.getId()).isEqualTo("13");
        assertThat(ctx.getDateDebutStage()).isEmpty();
        assertThat(ctx.getDateRupture()).isEmpty();
        assertThat(ctx.isRupture()).isFalse();
        assertThat(ctx.getMontantGratification()).isNull();
        assertThat(ctx.getService()).isNull();
        assertThat(ctx.getContact()).isNull();
        assertThat(ctx.getEnseignant()).isNull();
    }

    @Test
    void leMailDuDpoEstRepriseDansLeContexteDeConfiguration() {
        ConfigGeneraleDto configGenerale = new ConfigGeneraleDto();
        configGenerale.setMailDpo("dpo@univ-example.fr");

        assertThat(new ImpressionContext.ConfigContext(configGenerale).getMailDpo()).isEqualTo("dpo@univ-example.fr");
    }

    @Test
    void leMailDuDpoEstVideSiLaConfigurationEstAbsenteOuNonRenseignee() {
        assertThat(new ImpressionContext.ConfigContext(null).getMailDpo()).isEmpty();
        assertThat(new ImpressionContext.ConfigContext(new ConfigGeneraleDto()).getMailDpo()).isEmpty();
        assertThat(new ImpressionContext().getConfig().getMailDpo()).isNull();
    }
}
