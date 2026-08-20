package org.esup_portail.esup_stage.model;

import org.esup_portail.esup_stage.enums.NbJoursHebdoEnum;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Couvre les projections {@code getExportValue} utilisées par les exports Excel/CSV :
 * chaque clé d'export est dérivée des entités liées (étudiant, structure, service...).
 */
class ExportValueTest {

    private static final List<String> CLES_CONVENTION = List.of(
            "id", "numEtudiant", "etudiant", "etudiantNom", "etudiantPrenom", "courrielPersoEtudiant",
            "mailUniEtudiant", "telEtudiant", "telPortableEtudiant", "codeUFR", "ufr", "codeDepartement",
            "codeEtape", "etape", "dateDebutStage", "dateFinStage", "interruptionStage", "theme",
            "sujetStage", "fonctionsEtTaches", "details", "dureeExceptionnelle", "nbJoursHebdo",
            "nbHeuresHebdo", "gratification", "uniteDuree", "validationPedagogique",
            "verificationAdministrative", "validationConvention", "enseignant", "mailEnseignant",
            "signataire", "mailSignataire", "fonctionSignataire", "annee", "typeConvention",
            "commentaireStage", "competences", "commentaireDureeTravail", "codeElp", "libelleELP",
            "codeSexeEtu", "avantageNature", "adresseEtudiant", "codePostalEtudiant", "paysEtudiant",
            "villeEtudiant", "avenant", "dateCreation", "dateModif", "structure", "structureSiret",
            "structureAdresse", "structureCP", "structureCommune", "structurePays",
            "structureStatutJuridique", "structureType", "structureEffectif", "structureNAF",
            "structurePhone", "structureMail", "structureSiteWeb", "service", "serviceAdresse",
            "serviceCP", "serviceCommune", "servicePays", "tuteur", "tuteurMail", "tuteurPhone",
            "tuteurFonction", "lieuStage");

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }

    private Convention conventionComplete() {
        Convention convention = new Convention();
        convention.setId(42);

        Etudiant etudiant = new Etudiant();
        etudiant.setNumEtudiant("12345");
        etudiant.setNom("Dupont");
        etudiant.setPrenom("Marie");
        etudiant.setMail("marie@univ.fr");
        etudiant.setCodeSexe("F");
        convention.setEtudiant(etudiant);
        convention.setCourrielPersoEtudiant("perso@gmail.com");
        convention.setTelEtudiant("0301020304");
        convention.setTelPortableEtudiant("0601020304");
        convention.setAdresseEtudiant("1 rue des Lilas");
        convention.setCodePostalEtudiant("54000");
        convention.setVilleEtudiant("Nancy");
        convention.setPaysEtudiant("France");

        Ufr ufr = new Ufr();
        UfrId ufrId = new UfrId();
        ufrId.setCode("SCI");
        ufr.setId(ufrId);
        ufr.setLibelle("Sciences");
        convention.setUfr(ufr);
        convention.setCodeDepartement("54");

        Etape etape = new Etape();
        EtapeId etapeId = new EtapeId();
        etapeId.setCode("M1");
        etape.setId(etapeId);
        etape.setLibelle("Master 1");
        convention.setEtape(etape);

        convention.setDateDebutStage(date(2026, 3, 2));
        convention.setDateFinStage(date(2026, 8, 31));
        convention.setInterruptionStage(true);
        convention.setDateCreation(date(2026, 1, 15));
        convention.setDateModif(date(2026, 2, 1));

        Theme theme = new Theme();
        theme.setLibelle("Informatique");
        convention.setTheme(theme);
        convention.setSujetStage("Développement");
        convention.setFonctionsEtTaches("Coder");
        convention.setDetails("Détails");
        convention.setCommentaireStage("Commentaire");
        convention.setCompetences("Java");
        convention.setCommentaireDureeTravail("35h");
        convention.setDureeExceptionnelle("10");
        convention.setNbJoursHebdo(NbJoursHebdoEnum.values()[0]);
        convention.setNbHeuresHebdo("35");
        convention.setMontantGratification("600");
        UniteGratification uniteGratification = new UniteGratification();
        uniteGratification.setLibelle("€/mois");
        convention.setUniteGratification(uniteGratification);
        UniteDuree uniteDuree = new UniteDuree();
        uniteDuree.setLibelle("mois");
        convention.setUniteDureeGratification(uniteDuree);
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(false);
        convention.setValidationConvention(true);
        convention.setAnnee("2025/2026");
        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setLibelle("Stage");
        convention.setTypeConvention(typeConvention);
        convention.setCodeElp("ELP1");
        convention.setLibelleELP("Stage M1");
        convention.setAvantagesNature("Tickets restaurant");
        convention.setLieuStage("Nancy");

        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Martin");
        enseignant.setPrenom("Paul");
        enseignant.setMail("paul.martin@univ.fr");
        convention.setEnseignant(enseignant);

        Contact signataire = new Contact();
        signataire.setNom("Directeur");
        signataire.setPrenom("Anne");
        signataire.setMail("dir@acme.fr");
        signataire.setFonction("Directrice");
        convention.setSignataire(signataire);

        Contact tuteur = new Contact();
        tuteur.setNom("Tuteur");
        tuteur.setPrenom("Jean");
        tuteur.setMail("tuteur@acme.fr");
        tuteur.setTel("0201020304");
        tuteur.setFonction("Chef de projet");
        convention.setContact(tuteur);

        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        structure.setNumeroSiret("12345678901234");
        structure.setVoie("2 avenue du Parc");
        structure.setCodePostal("75001");
        structure.setCommune("Paris");
        Pays pays = new Pays();
        pays.setLib("France");
        structure.setPays(pays);
        StatutJuridique statutJuridique = new StatutJuridique();
        statutJuridique.setLibelle("SARL");
        structure.setStatutJuridique(statutJuridique);
        TypeStructure typeStructure = new TypeStructure();
        typeStructure.setLibelle("Entreprise");
        structure.setTypeStructure(typeStructure);
        Effectif effectif = new Effectif();
        effectif.setLibelle("10-19");
        structure.setEffectif(effectif);
        NafN5 nafN5 = new NafN5();
        nafN5.setLibelle("Programmation informatique");
        structure.setNafN5(nafN5);
        structure.setTelephone("0101020304");
        structure.setMail("contact@acme.fr");
        structure.setSiteWeb("https://acme.fr");
        convention.setStructure(structure);

        Service service = new Service();
        service.setNom("R&D");
        service.setVoie("2 avenue du Parc");
        service.setCodePostal("75001");
        service.setCommune("Paris");
        service.setPays(pays);
        convention.setService(service);

        convention.setAvenants(List.of(new Avenant()));
        return convention;
    }

    @Test
    void chaqueCleDExportEstDeriveeDeLaConventionComplete() {
        Convention convention = conventionComplete();
        String dateDebut = new SimpleDateFormat("dd/MM/yyyy").format(convention.getDateDebutStage());

        // toutes les clés s'évaluent sans erreur
        CLES_CONVENTION.forEach(cle ->
                assertThatCode(() -> convention.getExportValue(cle)).as(cle).doesNotThrowAnyException());

        assertThat(convention.getExportValue("id")).isEqualTo("42");
        assertThat(convention.getExportValue("etudiant")).isEqualTo("Marie Dupont");
        assertThat(convention.getExportValue("mailUniEtudiant")).isEqualTo("marie@univ.fr");
        assertThat(convention.getExportValue("codeUFR")).isEqualTo("SCI");
        assertThat(convention.getExportValue("etape")).isEqualTo("Master 1");
        assertThat(convention.getExportValue("dateDebutStage")).isEqualTo(dateDebut);
        assertThat(convention.getExportValue("interruptionStage")).isEqualTo("Oui");
        assertThat(convention.getExportValue("gratification")).isEqualTo("600 €/mois");
        assertThat(convention.getExportValue("validationPedagogique")).isEqualTo("Oui");
        assertThat(convention.getExportValue("verificationAdministrative")).isEqualTo("Non");
        assertThat(convention.getExportValue("enseignant")).isEqualTo("Paul Martin");
        assertThat(convention.getExportValue("signataire")).isEqualTo("Anne Directeur");
        assertThat(convention.getExportValue("avenant")).isEqualTo("Oui");
        assertThat(convention.getExportValue("structurePays")).isEqualTo("France");
        assertThat(convention.getExportValue("structureStatutJuridique")).isEqualTo("SARL");
        assertThat(convention.getExportValue("tuteur")).isEqualTo("Tuteur Jean");
        // le lieu de stage est dérivé du service d'accueil
        assertThat(convention.getExportValue("lieuStage")).isEqualTo("R&D Paris France");
        assertThat(convention.getExportValue("cleInconnue")).isEmpty();
    }

    @Test
    void uneConventionVideNeLevePasDErreur() {
        Convention convention = new Convention();

        CLES_CONVENTION.forEach(cle ->
                assertThatCode(() -> convention.getExportValue(cle)).as(cle).doesNotThrowAnyException());

        assertThat(convention.getExportValue("etudiant")).isEmpty();
        assertThat(convention.getExportValue("interruptionStage")).isEmpty();
        assertThat(convention.getExportValue("validationConvention")).isEqualTo("Non");
        assertThat(convention.getExportValue("structurePays")).isEmpty();

        // liste d'avenants vide : réponse explicite "Non"
        convention.setAvenants(List.of());
        assertThat(convention.getExportValue("avenant")).isEqualTo("Non");
    }

    @Test
    void accordAnnuaireExporteSesTroisEtats() {
        Convention convention = new Convention();
        assertThat(convention.getExportValue("accordAnnuaireEtudiant")).isEqualTo("Non renseigné");

        convention.setAccordAnnuaireEtudiant(true);
        assertThat(convention.getExportValue("accordAnnuaireEtudiant")).isEqualTo("Oui");

        convention.setAccordAnnuaireEtudiant(false);
        assertThat(convention.getExportValue("accordAnnuaireEtudiant")).isEqualTo("Non");
    }

    @Test
    void accordAnnuaireResteLisibleSurUneConventionConfidentielle() {
        Convention convention = new Convention();
        convention.setConfidentiel(true);
        convention.setAccordAnnuaireEtudiant(false);

        assertThat(convention.getExportValue("accordAnnuaireEtudiant")).isEqualTo("Non");
    }

    @Test
    void lesChampsSensiblesSontVidesQuandLaConventionEstConfidentielle() {
        Convention convention = conventionComplete();
        assertThat(convention.getExportValue("sujetStage")).isEqualTo("Développement");

        convention.setConfidentiel(true);
        assertThat(convention.getExportValue("sujetStage")).isEmpty();
        assertThat(convention.getExportValue("fonctionsEtTaches")).isEmpty();
        assertThat(convention.getExportValue("details")).isEmpty();
        assertThat(convention.getExportValue("competences")).isEmpty();
        assertThat(convention.getExportValue("commentaireStage")).isEmpty();
        // les champs non sensibles restent exportés
        assertThat(convention.getExportValue("etudiant")).isEqualTo("Marie Dupont");
    }

    @Test
    void laNomenclatureEstFigeeDepuisLesEntitesLiees() {
        Convention convention = conventionComplete();
        LangueConvention langueConvention = new LangueConvention();
        langueConvention.setLibelle("Français");
        convention.setLangueConvention(langueConvention);
        Devise devise = new Devise();
        devise.setLibelle("Euro");
        convention.setDevise(devise);
        ModeValidationStage modeValidationStage = new ModeValidationStage();
        modeValidationStage.setLibelle("Rapport");
        convention.setModeValidationStage(modeValidationStage);
        ModeVersGratification modeVersGratification = new ModeVersGratification();
        modeVersGratification.setLibelle("Virement");
        convention.setModeVersGratification(modeVersGratification);
        NatureTravail natureTravail = new NatureTravail();
        natureTravail.setLibelle("Développement");
        convention.setNatureTravail(natureTravail);
        OrigineStage origineStage = new OrigineStage();
        origineStage.setLibelle("Candidature spontanée");
        convention.setOrigineStage(origineStage);
        TempsTravail tempsTravail = new TempsTravail();
        tempsTravail.setLibelle("Temps plein");
        convention.setTempsTravail(tempsTravail);
        UniteDuree uniteDureeExceptionnelle = new UniteDuree();
        uniteDureeExceptionnelle.setLibelle("jours");
        convention.setUniteDureeExceptionnelle(uniteDureeExceptionnelle);

        convention.setValeurNomenclature();

        ConventionNomenclature nomenclature = convention.getNomenclature();
        assertThat(nomenclature.getLangueConvention()).isEqualTo("Français");
        assertThat(nomenclature.getDevise()).isEqualTo("Euro");
        assertThat(nomenclature.getModeValidationStage()).isEqualTo("Rapport");
        assertThat(nomenclature.getModeVersGratification()).isEqualTo("Virement");
        assertThat(nomenclature.getNatureTravail()).isEqualTo("Développement");
        assertThat(nomenclature.getOrigineStage()).isEqualTo("Candidature spontanée");
        assertThat(nomenclature.getTempsTravail()).isEqualTo("Temps plein");
        assertThat(nomenclature.getTheme()).isEqualTo("Informatique");
        assertThat(nomenclature.getTypeConvention()).isEqualTo("Stage");
        assertThat(nomenclature.getUniteDureeExceptionnelle()).isEqualTo("jours");
        assertThat(nomenclature.getUniteDureeGratification()).isEqualTo("mois");
        assertThat(nomenclature.getUniteGratification()).isEqualTo("€/mois");

        // un second appel réutilise la nomenclature existante
        convention.setValeurNomenclature();
        assertThat(convention.getNomenclature()).isSameAs(nomenclature);
    }

    @Test
    void laSignatureCompleteExigeToutesLesDates() {
        Convention convention = new Convention();
        assertThat(convention.isAllSignedDateSetted()).isFalse();

        Date maintenant = new Date();
        convention.setDateDepotEtudiant(maintenant);
        convention.setDateSignatureEtudiant(maintenant);
        convention.setDateDepotEnseignant(maintenant);
        convention.setDateSignatureEnseignant(maintenant);
        convention.setDateDepotTuteur(maintenant);
        convention.setDateSignatureTuteur(maintenant);
        convention.setDateDepotSignataire(maintenant);
        convention.setDateSignatureSignataire(maintenant);
        convention.setDateDepotViseur(maintenant);
        assertThat(convention.isAllSignedDateSetted()).isFalse();

        convention.setDateSignatureViseur(maintenant);
        assertThat(convention.isAllSignedDateSetted()).isTrue();
    }

    @Test
    void lExportDuPersonnelDeCentreProjetteSesDroits() {
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        Civilite civilite = new Civilite();
        civilite.setLibelle("Madame");
        personnel.setCivilite(civilite);
        personnel.setNom("Durand");
        personnel.setPrenom("Claire");
        personnel.setAlertesMail(true);
        personnel.setDroitEvaluationEtudiant(true);
        personnel.setDroitEvaluationEnseignant(false);
        personnel.setDroitEvaluationEntreprise(true);

        assertThat(personnel.getExportValue("civilite")).isEqualTo("Madame");
        assertThat(personnel.getExportValue("nom")).isEqualTo("Durand");
        assertThat(personnel.getExportValue("prenom")).isEqualTo("Claire");
        assertThat(personnel.getExportValue("alertesMail")).isEqualTo("Oui");
        assertThat(personnel.getExportValue("droitsEvaluation")).isEqualTo("Étudiant, Entreprise");
        assertThat(personnel.getExportValue("inconnu")).isEmpty();

        PersonnelCentreGestion vide = new PersonnelCentreGestion();
        assertThat(vide.getExportValue("civilite")).isEmpty();
        assertThat(vide.getExportValue("alertesMail")).isEqualTo("Non");
        assertThat(vide.getExportValue("droitsEvaluation")).isEmpty();
    }

    @Test
    void lExportDeLaStructureProjetteSesNomenclatures() {
        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        structure.setNumeroSiret("12345678901234");
        structure.setCommune("Paris");
        NafN5 nafN5 = new NafN5();
        nafN5.setLibelle("Programmation");
        structure.setNafN5(nafN5);
        Pays pays = new Pays();
        pays.setLib("France");
        structure.setPays(pays);
        TypeStructure typeStructure = new TypeStructure();
        typeStructure.setLibelle("Entreprise");
        structure.setTypeStructure(typeStructure);
        StatutJuridique statutJuridique = new StatutJuridique();
        statutJuridique.setLibelle("SARL");
        structure.setStatutJuridique(statutJuridique);

        assertThat(structure.getExportValue("raisonSociale")).isEqualTo("ACME");
        assertThat(structure.getExportValue("numeroSiret")).isEqualTo("12345678901234");
        assertThat(structure.getExportValue("nafN5")).isEqualTo("Programmation");
        assertThat(structure.getExportValue("pays")).isEqualTo("France");
        assertThat(structure.getExportValue("commune")).isEqualTo("Paris");
        assertThat(structure.getExportValue("typeStructure")).isEqualTo("Entreprise");
        assertThat(structure.getExportValue("statutJuridique")).isEqualTo("SARL");
        assertThat(structure.getExportValue("inconnu")).isEmpty();

        Structure vide = new Structure();
        assertThat(vide.getExportValue("nafN5")).isEmpty();
        assertThat(vide.getExportValue("pays")).isEmpty();
        assertThat(vide.getExportValue("typeStructure")).isEmpty();
        assertThat(vide.getExportValue("statutJuridique")).isEmpty();
    }
}
