package org.esup_portail.esup_stage.service.signature;

import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.dto.MetadataSignataireDto;
import org.esup_portail.esup_stage.enums.SignataireEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.AvenantJpaRepository;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.esup_portail.esup_stage.service.ldap.LdapService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.esup_portail.esup_stage.service.signature.model.Historique;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SignatureServiceMetadataTest {

    private SignatureService service;
    private ConventionJpaRepository conventionJpaRepository;
    private AvenantJpaRepository avenantJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private ImpressionService impressionService;
    private LdapService ldapService;

    @BeforeEach
    void setUp() {
        service = new SignatureService(WebClient.builder());
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        ldapService = mock(LdapService.class);
        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "avenantJpaRepository", avenantJpaRepository);
        ReflectionTestUtils.setField(service, "centreGestionJpaRepository", centreGestionJpaRepository);
        ReflectionTestUtils.setField(service, "impressionService", impressionService);
        ReflectionTestUtils.setField(service, "ldapService", ldapService);

        // les transformations OTP restituent la valeur telle quelle
        when(impressionService.getOtpDataEmail(any())).thenAnswer(inv -> inv.getArgument(0));
        when(impressionService.getOtpDataPhoneNumber(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Convention conventionComplete() {
        Convention convention = new Convention();
        convention.setId(42);
        convention.setNomEtabRef("Université de Lorraine");
        convention.setLoginEnvoiSignature("gestionnaire1");
        convention.setTelPortableEtudiant("0611223344");

        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        etudiant.setMail("alice@univ.fr");
        convention.setEtudiant(etudiant);

        Etape etape = new Etape();
        etape.setLibelle("L3 Informatique");
        convention.setEtape(etape);

        Enseignant enseignant = new Enseignant();
        enseignant.setNom("Professeur");
        enseignant.setPrenom("Paul");
        enseignant.setMail("paul@univ.fr");
        enseignant.setTel("0311111111");
        convention.setEnseignant(enseignant);

        Contact tuteur = new Contact();
        tuteur.setNom("Tuteur");
        tuteur.setPrenom("Tom");
        tuteur.setMail("tom@acme.fr");
        tuteur.setTel("0322222222");
        convention.setContact(tuteur);

        Contact signataire = new Contact();
        signataire.setNom("Signataire");
        signataire.setPrenom("Sam");
        signataire.setMail("sam@acme.fr");
        signataire.setTel("0333333333");
        convention.setSignataire(signataire);

        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);
        centreGestion.setCircuitSignature("WF-1");
        centreGestion.setNomViseur("Viseur");
        centreGestion.setPrenomViseur("Vera");
        centreGestion.setMailViseur("vera@univ.fr");
        centreGestion.setTelephone("0344444444");
        centreGestion.setSignataires(List.of(
                new CentreGestionSignataire(centreGestion, SignataireEnum.etudiant, 1),
                new CentreGestionSignataire(centreGestion, SignataireEnum.enseignant, 2),
                new CentreGestionSignataire(centreGestion, SignataireEnum.tuteur, 3),
                new CentreGestionSignataire(centreGestion, SignataireEnum.signataire, 4),
                new CentreGestionSignataire(centreGestion, SignataireEnum.viseur, 5)
        ));
        convention.setCentreGestion(centreGestion);
        return convention;
    }

    @Test
    void lesMetadonneesDecriventTousLesSignataires() {
        Convention convention = conventionComplete();
        LdapUser envoyeur = new LdapUser();
        envoyeur.setMail("envoyeur@univ.fr");
        when(ldapService.searchByLogin("gestionnaire1")).thenReturn(envoyeur);

        MetadataDto metadata = service.getPublicMetadata(convention);

        assertThat(metadata.getTitle()).isEqualTo("Convention_42_Durand_Alice");
        assertThat(metadata.getCompanyname()).isEqualTo("Université de Lorraine");
        assertThat(metadata.getSchool()).isEqualTo("L3 Informatique");
        assertThat(metadata.getWorkflowId()).isEqualTo("WF-1");
        assertThat(metadata.getWatchers()).hasSize(1);
        assertThat(metadata.getSignatory()).hasSize(5);
        assertThat(metadata.getSignatory())
                .extracting(MetadataSignataireDto::getName)
                .containsExactlyInAnyOrder("Durand", "Professeur", "Tuteur", "Signataire", "Viseur");
        assertThat(metadata.getSignatory())
                .extracting(MetadataSignataireDto::getMail)
                .contains("alice@univ.fr", "paul@univ.fr", "tom@acme.fr", "sam@acme.fr", "vera@univ.fr");
    }

    @Test
    void leDelegataireViseurPrimeSurLeViseur() {
        Convention convention = conventionComplete();
        CentreGestion centreGestion = convention.getCentreGestion();
        centreGestion.setMailDelegataireViseur("delegataire@univ.fr");
        centreGestion.setNomDelegataireViseur("Delegataire");
        centreGestion.setPrenomDelegataireViseur("Dede");

        MetadataDto metadata = service.getPublicMetadata(convention);

        assertThat(metadata.getSignatory())
                .extracting(MetadataSignataireDto::getMail)
                .contains("delegataire@univ.fr")
                .doesNotContain("vera@univ.fr");
    }

    @Test
    void unAvenantChangeLeTitreEtSesIntervenants() {
        Convention convention = conventionComplete();
        Avenant avenant = new Avenant();
        avenant.setId(9);
        avenant.setLoginEnvoiSignature("gestionnaire2");
        Enseignant enseignantAvenant = new Enseignant();
        enseignantAvenant.setNom("Remplacant");
        enseignantAvenant.setPrenom("Rene");
        enseignantAvenant.setMail("rene@univ.fr");
        avenant.setEnseignant(enseignantAvenant);
        when(avenantJpaRepository.findById((Integer) 9)).thenReturn(Optional.of(avenant));

        MetadataDto metadata = service.getPublicMetadata(convention, 9);

        assertThat(metadata.getTitle()).isEqualTo("Avenant_9_Durand_Alice");
        assertThat(metadata.getSignatory())
                .extracting(MetadataSignataireDto::getName)
                .contains("Remplacant")
                .doesNotContain("Professeur");
    }

    @Test
    void lHistoriqueDeSignatureRemplitLesDatesConvention() {
        Convention convention = conventionComplete();
        Date depot = date(2026, 3, 1);
        Date signature = date(2026, 3, 2);

        Historique historiqueEtudiant = new Historique();
        historiqueEtudiant.setTypeSignataire(SignataireEnum.etudiant);
        historiqueEtudiant.setDateDepot(depot);
        historiqueEtudiant.setDateSignature(signature);

        Historique historiqueTuteur = new Historique();
        historiqueTuteur.setTypeSignataire(SignataireEnum.tuteur);
        historiqueTuteur.setDateSignature(signature); // pas de dépôt : repli sur la date de signature

        service.setSignatureHistorique(convention, List.of(historiqueEtudiant, historiqueTuteur));

        assertThat(convention.getDateSignatureEtudiant()).isEqualTo(signature);
        assertThat(convention.getDateDepotEtudiant()).isEqualTo(depot);
        assertThat(convention.getDateSignatureTuteur()).isEqualTo(signature);
        assertThat(convention.getDateDepotTuteur()).isEqualTo(signature);
        assertThat(convention.getDateActualisationSignature()).isNotNull();
        verify(conventionJpaRepository).save(convention);
    }

    @Test
    void lHistoriqueCouvreTousLesTypesDeSignataires() {
        Convention convention = conventionComplete();
        Date signature = date(2026, 4, 1);
        List<Historique> historiques = List.of(
                historique(SignataireEnum.enseignant, signature),
                historique(SignataireEnum.signataire, signature),
                historique(SignataireEnum.viseur, signature)
        );

        service.setSignatureHistorique(convention, historiques);

        assertThat(convention.getDateSignatureEnseignant()).isEqualTo(signature);
        assertThat(convention.getDateSignatureSignataire()).isEqualTo(signature);
        assertThat(convention.getDateSignatureViseur()).isEqualTo(signature);
    }

    @Test
    void getPublicPdfCompleteLEtablissementManquant() {
        Convention convention = conventionComplete();
        convention.setNomEtabRef(null);
        CentreGestion etablissement = new CentreGestion();
        etablissement.setNomCentre("Etablissement");
        etablissement.setVoie("1 rue");
        etablissement.setCodePostal("54000");
        etablissement.setCommune("Nancy");
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);

        byte[] pdf = service.getPublicPdf(convention, null);

        assertThat(convention.getNomEtabRef()).isEqualTo("Etablissement");
        assertThat(convention.getAdresseEtabRef()).isEqualTo("1 rue 54000 Nancy");
        assertThat(pdf).isNotNull();
        verify(impressionService).generateConventionAvenantPDF(any(), any(), any(), org.mockito.ArgumentMatchers.eq(false));
    }

    @Test
    void getPublicPdfEchoueSansCentreEtablissement() {
        Convention convention = conventionComplete();
        convention.setNomEtabRef(null);
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(null);

        assertThatThrownBy(() -> service.getPublicPdf(convention, null))
                .isInstanceOf(AppException.class);
    }

    private Historique historique(SignataireEnum type, Date signature) {
        Historique historique = new Historique();
        historique.setTypeSignataire(type);
        historique.setDateSignature(signature);
        return historique;
    }

    private static Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day);
        return calendar.getTime();
    }
}
