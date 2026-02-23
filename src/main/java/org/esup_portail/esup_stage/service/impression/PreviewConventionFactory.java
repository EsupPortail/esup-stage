package org.esup_portail.esup_stage.service.impression;

import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.PaysJpaRepository;
import org.esup_portail.esup_stage.service.impression.context.ImpressionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

@Component
public class PreviewConventionFactory {

    @Autowired
    PaysJpaRepository paysJpaRepository;

    public ImpressionContext createPreviewContext(CentreGestion centreGestion, CentreGestion centreEtablissement) {
        Convention convention = createFictionalConvention(centreGestion);
        return new ImpressionContext(convention, null, centreEtablissement);
    }

    public Avenant createFictionalAvenant(Convention convention) {
        Avenant avenant = new Avenant();
        avenant.setId(12345);
        avenant.setConvention(convention);
        return avenant;
    }

    public Convention createFictionalConvention(CentreGestion centreGestion) {
        Convention convention = new Convention();
        convention.setId(999);
        convention.setCentreGestion(centreGestion);

        Etudiant etudiant = new Etudiant();
        etudiant.setId(1);
        etudiant.setPrenom("Jean");
        etudiant.setNom("Dupont");
        etudiant.setMail("jean.dupont@example.com");
        etudiant.setNumEtudiant("2025-0001");
        etudiant.setCodeUniversite(centreGestion != null ? centreGestion.getCodeUniversite() : "UNIV");
        etudiant.setIdentEtudiant("JDUP01");
        convention.setEtudiant(etudiant);

        Enseignant enseignant = new Enseignant();
        enseignant.setId(2);
        enseignant.setPrenom("Alice");
        enseignant.setNom("Martin");
        enseignant.setMail("alice.martin@univ.example");
        enseignant.setTel("+33 1 23 45 67 89");
        enseignant.setTypePersonne("Maitre de conferences");
        convention.setEnseignant(enseignant);

        Contact contact = new Contact();
        contact.setId(3);
        contact.setPrenom("Pierre");
        contact.setNom("Durand");
        contact.setMail("pierre.durand@entreprise.example");
        contact.setTel("+33 6 11 22 33 44");
        contact.setFonction("Tuteur pedagogique");
        contact.setCentreGestion(centreGestion);
        convention.setContact(contact);

        Contact signataire = new Contact();
        signataire.setId(4);
        signataire.setPrenom("Marie");
        signataire.setNom("Legrand");
        signataire.setMail("marie.legrand@univ.example");
        signataire.setTel("+33 1 44 55 66 77");
        signataire.setFonction("Responsable composante");
        signataire.setCentreGestion(centreGestion);
        convention.setSignataire(signataire);

        Pays pays = paysJpaRepository.findById(82);
        if (pays == null) {
            pays = new Pays();
            pays.setId(82);
            pays.setLib("FRANCE");
            pays.setCog(99100);
            pays.setActual(1);
            pays.setSiretObligatoire(false);
            pays.setTemEnServPays("O");
        }

        Structure structure = new Structure();
        structure.setId(10);
        structure.setRaisonSociale("ACME Solutions");
        structure.setNumeroSiret("123 456 789 00012");
        structure.setTelephone("+33 1 88 77 66 55");
        structure.setMail("contact@acme.example");
        structure.setVoie("25 boulevard de la Tech");
        structure.setBatimentResidence("Bat. A");
        structure.setCodePostal("75002");
        structure.setCommune("Paris");
        structure.setPays(pays);
        structure.setActivitePrincipale("Edition de logiciels");
        convention.setStructure(structure);

        org.esup_portail.esup_stage.model.Service service = new org.esup_portail.esup_stage.model.Service();
        service.setId(11);
        service.setNom("R&D");
        service.setVoie("25 boulevard de la Tech");
        service.setBatimentResidence("Bat. A - 3e");
        service.setCodePostal("75002");
        service.setCommune("Paris");
        service.setPays(pays);
        convention.setService(service);

        TypeConvention typeConvention = new TypeConvention();
        typeConvention.setId(1);
        typeConvention.setLibelle("Convention de stage");
        convention.setTypeConvention(typeConvention);

        LangueConvention langueConvention = new LangueConvention();
        langueConvention.setCode("FR");
        langueConvention.setLibelle("Francais");
        convention.setLangueConvention(langueConvention);

        ConventionNomenclature nomenclature = new ConventionNomenclature();
        nomenclature.setId(999);
        nomenclature.setLangueConvention("Francais");
        nomenclature.setDevise("EUR");
        nomenclature.setModeValidationStage("Validation pedagogique");
        nomenclature.setModeVersGratification("Virement");
        nomenclature.setNatureTravail("Bureau - developpement");
        nomenclature.setOrigineStage("Offre interne");
        nomenclature.setTempsTravail("Temps plein");
        nomenclature.setTheme("Informatique");
        nomenclature.setTypeConvention(typeConvention.getLibelle());
        nomenclature.setUniteDureeExceptionnelle("heures");
        nomenclature.setUniteDureeGratification("mois");
        nomenclature.setUniteGratification("EUR");
        convention.setNomenclature(nomenclature);

        convention.setSujetStage("Projet d'integration et developpement");
        convention.setCommentaireDureeTravail("Temps plein avec horaires classiques");
        convention.setCourrielPersoEtudiant("jean.dupont.perso@example.com");
        TempsTravail tempsTravail = new TempsTravail();
        tempsTravail.setId(1);
        tempsTravail.setLibelle("Temps plein");
        tempsTravail.setCodeCtrl("TP");
        tempsTravail.setTemEnServ("O");
        convention.setTempsTravail(tempsTravail);
        convention.setFonctionsEtTaches("Developpement, tests, documentation");
        Etape etape = new Etape();
        EtapeId etapeId = new EtapeId();
        etapeId.setCode("M1INFO");
        etapeId.setCodeUniversite(centreGestion != null ? centreGestion.getCodeUniversite() : "UNIV");
        etapeId.setCodeVersionEtape("2026");
        etape.setId(etapeId);
        etape.setLibelle("Master Informatique");
        convention.setEtape(etape);
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, 7);
        Date debut = c.getTime();
        c.add(Calendar.MONTH, 3);
        Date fin = c.getTime();
        convention.setDateDebutStage(debut);
        convention.setDateFinStage(fin);
        convention.setDureeStage(90);
        convention.setVilleEtudiant("Nantes");
        convention.setAdresseEtudiant("10 rue de la Republique");
        convention.setCodePostalEtudiant("44000");
        convention.setPaysEtudiant("France");
        convention.setTelEtudiant("+33 2 98 76 54 32");
        convention.setTelPortableEtudiant("+33 6 55 44 33 22");
        convention.setAnnee(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        convention.setGratificationStage(Boolean.TRUE);
        convention.setMontantGratification("500");

        PeriodeStage periode = new PeriodeStage();
        periode.setId(1);
        periode.setDateDebut(debut);
        periode.setDateFin(fin);
        periode.setNbHeuresJournalieres(7);
        periode.setConvention(convention);
        if (convention.getPeriodeStage() == null) {
            convention.setPeriodeStage(new ArrayList<>());
        }
        convention.getPeriodeStage().add(periode);

        convention.setAvenants(new ArrayList<>());

        return convention;
    }
}
