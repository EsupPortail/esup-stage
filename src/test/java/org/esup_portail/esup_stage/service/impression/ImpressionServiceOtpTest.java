package org.esup_portail.esup_stage.service.impression;

import org.esup_portail.esup_stage.config.properties.AppliProperties;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.enums.TypeSignatureEnum;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.service.ConventionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests des données OTP (signature électronique) produites par ImpressionService.
 */
class ImpressionServiceOtpTest {

    private ImpressionService service;
    private AppliProperties appliProperties;

    @BeforeEach
    void setUp() {
        service = new ImpressionService();
        appliProperties = new AppliProperties();
        appliProperties.setMailer(new AppliProperties.MailerProperties());
        ConventionService conventionService = new ConventionService();
        ReflectionTestUtils.setField(service, "appliProperties", appliProperties);
        ReflectionTestUtils.setField(service, "conventionService", conventionService);
    }

    @Test
    void leNumeroOtpEstNormaliseAuFormatE164() {
        assertThat(service.getOtpDataPhoneNumber("06 11 22 33 44")).isEqualTo("+33611223344");
        assertThat(service.getOtpDataPhoneNumber("pas un numero")).isEmpty();
        assertThat(service.getOtpDataPhoneNumber(null)).isEmpty();
    }

    @Test
    void leNumeroOtpEstVideQuandLaRedirectionMailEstActive() {
        appliProperties.getMailer().setDeliveryAddress("redirection@univ.fr");

        assertThat(service.getOtpDataPhoneNumber("0611223344")).isEmpty();
        assertThat(service.getOtpDataEmail("vrai@univ.fr")).isEqualTo("redirection@univ.fr");
    }

    @Test
    void lEmailOtpNeutraliseLesValeursAbsentes() {
        assertThat(service.getOtpDataEmail("alice@univ.fr")).isEqualTo("alice@univ.fr");
        assertThat(service.getOtpDataEmail(null)).isEmpty();
        assertThat(service.getOtpDataEmail("null")).isEmpty();
    }

    private Convention conventionOtp(TypeSignatureEnum type) {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        etudiant.setMail("alice@univ.fr");
        convention.setEtudiant(etudiant);
        convention.setTelPortableEtudiant("0611223344");
        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Prof");
        enseignant.setPrenom("Paul");
        enseignant.setMail("paul@univ.fr");
        convention.setEnseignant(enseignant);
        Contact tuteur = new Contact();
        tuteur.setNom("Tuteur");
        tuteur.setPrenom("Tom");
        tuteur.setMail("tom@acme.fr");
        convention.setContact(tuteur);
        Contact signataire = new Contact();
        signataire.setNom("Signataire");
        signataire.setPrenom("Sam");
        signataire.setMail("sam@acme.fr");
        convention.setSignataire(signataire);

        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setNomViseur("Viseur");
        centreGestion.setPrenomViseur("Vera");
        centreGestion.setMail("centre@univ.fr");
        centreGestion.setTelephone("0311111111");
        List<CentreGestionSignataire> signataires = List.of(
                signataire(centreGestion, SignataireEnum.etudiant, type),
                signataire(centreGestion, SignataireEnum.enseignant, type),
                signataire(centreGestion, SignataireEnum.tuteur, type),
                signataire(centreGestion, SignataireEnum.signataire, type),
                signataire(centreGestion, SignataireEnum.viseur, type)
        );
        centreGestion.setSignataires(signataires);
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    private CentreGestionSignataire signataire(CentreGestion centreGestion, SignataireEnum signataireEnum, TypeSignatureEnum type) {
        CentreGestionSignataire signataire = new CentreGestionSignataire(centreGestion, signataireEnum, signataireEnum.ordinal() + 1);
        signataire.setType(type);
        return signataire;
    }

    @Test
    void lesDonneesXmlOtpListentTousLesSignatairesOtp() {
        Convention convention = conventionOtp(TypeSignatureEnum.otp);

        String xml = service.generateXmlData(convention, TypeSignatureEnum.otp);

        assertThat(xml).isNotNull()
                .contains("Durand").contains("Alice")
                .contains("Prof").contains("Tuteur").contains("Signataire").contains("Viseur")
                .contains("+33611223344");
    }

    @Test
    void leDelegataireViseurRemplaceLeViseurDansLesDonneesOtp() {
        Convention convention = conventionOtp(TypeSignatureEnum.otp);
        convention.getCentreGestion().setPrenomDelegataireViseur("Dede");
        convention.getCentreGestion().setNomDelegataireViseur("Delegataire");

        String xml = service.generateXmlData(convention, TypeSignatureEnum.otp);

        assertThat(xml).contains("Delegataire").doesNotContain("Viseur<");
    }

    @Test
    void sansSignataireOtpLesDonneesXmlSontNulles() {
        Convention convention = conventionOtp(TypeSignatureEnum.serveur);

        assertThat(service.generateXmlData(convention, TypeSignatureEnum.otp)).isNull();
    }
}
