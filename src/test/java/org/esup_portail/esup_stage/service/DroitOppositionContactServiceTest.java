package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.constants.DroitOpposition;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.DroitOppositionResultDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.TemplateMail;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ConventionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Couvre le recueil du droit d'opposition des contacts en entreprise : envoi automatique des
 * sollicitations et enregistrement en masse des refus.
 */
class DroitOppositionContactServiceTest {

    private static final String MAIL_GENERIQUE = "stages@univ.fr";

    private final DroitOppositionContactService service = new DroitOppositionContactService();

    private ConventionJpaRepository conventionJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private MailerService mailerService;
    private AppConfigService appConfigService;

    @BeforeEach
    void setUp() {
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        mailerService = mock(MailerService.class);
        appConfigService = mock(AppConfigService.class);

        ReflectionTestUtils.setField(service, "conventionJpaRepository", conventionJpaRepository);
        ReflectionTestUtils.setField(service, "contactJpaRepository", contactJpaRepository);
        ReflectionTestUtils.setField(service, "mailerService", mailerService);
        ReflectionTestUtils.setField(service, "appConfigService", appConfigService);

        ConfigGeneraleDto configGenerale = new ConfigGeneraleDto();
        configGenerale.setMailOppositionContact(MAIL_GENERIQUE);
        when(appConfigService.getConfigGenerale()).thenReturn(configGenerale);

        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition()).thenReturn(List.of());
        when(conventionJpaRepository.findConventionsSignataireDroitOpposition()).thenReturn(List.of());
    }

    private Contact contact(int id, String mail) {
        Contact contact = new Contact();
        contact.setId(id);
        contact.setNom("Martin");
        contact.setPrenom("Claire");
        contact.setMail(mail);
        return contact;
    }

    private Convention conventionTuteur(int id, Contact tuteur) {
        Convention convention = new Convention();
        convention.setId(id);
        convention.setContact(tuteur);
        return convention;
    }

    private Convention conventionSignataire(int id, Contact signataire) {
        Convention convention = new Convention();
        convention.setId(id);
        convention.setSignataire(signataire);
        return convention;
    }

    /* ==== Envoi automatique ==== */

    @Test
    void chaqueRoleRecoitLeTemplateQuiLuiCorrespond() {
        Contact tuteur = contact(1, "tuteur@acme.fr");
        Contact signataire = contact(2, "signataire@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(10, tuteur)));
        when(conventionJpaRepository.findConventionsSignataireDroitOpposition())
                .thenReturn(List.of(conventionSignataire(11, signataire)));

        DroitOppositionResultDto result = service.envoyerMailsDroitOpposition();

        assertThat(result.getEnvoyes()).isEqualTo(2);
        assertThat(result.getErreurs()).isZero();
        verify(mailerService).sendDroitOpposition(eq("tuteur@acme.fr"), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_TUTEUR_PRO), anyString());
        verify(mailerService).sendDroitOpposition(eq("signataire@acme.fr"), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_SIGNATAIRE), anyString());
    }

    @Test
    void unContactPresentSurPlusieursConventionsNestSolliciteQuUneFois() {
        Contact tuteur = contact(1, "tuteur@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(20, tuteur), conventionTuteur(10, tuteur)));

        DroitOppositionResultDto result = service.envoyerMailsDroitOpposition();

        assertThat(result.getEnvoyes()).isEqualTo(1);
        verify(mailerService, times(1)).sendDroitOpposition(anyString(), any(), anyString(), anyString());
    }

    @Test
    void unContactALaFoisTuteurEtSignataireNeRecoitQueLeMailTuteur() {
        Contact contact = contact(1, "contact@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(10, contact)));
        when(conventionJpaRepository.findConventionsSignataireDroitOpposition())
                .thenReturn(List.of(conventionSignataire(11, contact)));

        DroitOppositionResultDto result = service.envoyerMailsDroitOpposition();

        assertThat(result.getEnvoyes()).isEqualTo(1);
        verify(mailerService).sendDroitOpposition(anyString(), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_TUTEUR_PRO), anyString());
        verify(mailerService, never()).sendDroitOpposition(anyString(), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_SIGNATAIRE), anyString());
    }

    @Test
    void lEnvoiHorodateLeContactPourNePlusLeSolliciter() {
        Contact tuteur = contact(1, "tuteur@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(10, tuteur)));

        service.envoyerMailsDroitOpposition();

        assertThat(tuteur.getDateEnvoiMailOpposition()).isNotNull();
        verify(contactJpaRepository).saveAll(List.of(tuteur));
    }

    @Test
    void unEchecUnitaireNinterromptPasLeLot() {
        Contact premier = contact(1, "premier@acme.fr");
        Contact second = contact(2, "second@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(10, premier), conventionTuteur(11, second)));
        doThrow(new RuntimeException("SMTP indisponible"))
                .when(mailerService).sendDroitOpposition(eq("premier@acme.fr"), any(), anyString(), anyString());

        DroitOppositionResultDto result = service.envoyerMailsDroitOpposition();

        assertThat(result.getEnvoyes()).isEqualTo(1);
        assertThat(result.getErreurs()).isEqualTo(1);
        assertThat(premier.getDateEnvoiMailOpposition()).isNull();
        assertThat(second.getDateEnvoiMailOpposition()).isNotNull();
    }

    @Test
    void leLienMailtoPointeVersLaBoiteGeneriqueEtIdentifieLeContact() {
        Contact tuteur = contact(1, "tuteur@acme.fr");
        when(conventionJpaRepository.findConventionsTuteurProDroitOpposition())
                .thenReturn(List.of(conventionTuteur(10, tuteur)));

        service.envoyerMailsDroitOpposition();

        ArgumentCaptor<String> lien = ArgumentCaptor.forClass(String.class);
        verify(mailerService).sendDroitOpposition(anyString(), any(), anyString(), lien.capture());
        assertThat(lien.getValue()).startsWith("mailto:" + MAIL_GENERIQUE + "?subject=");
        assertThat(lien.getValue()).contains("&body=");
        // l'espace ne doit pas être encodé en "+", non interprété dans un mailto:
        assertThat(lien.getValue()).doesNotContain("+");
        assertThat(lien.getValue()).contains("Claire");
    }

    @Test
    void lEnvoiEstRefuseSiLaBoiteGeneriqueNestPasParametree() {
        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());

        assertThatThrownBy(() -> service.envoyerMailsDroitOpposition())
                .isInstanceOf(AppException.class)
                .hasMessageContaining("boîte mail générique");
        verify(mailerService, never()).sendDroitOpposition(anyString(), any(), anyString(), anyString());
    }

    /* ==== Envoi manuel à un contact précis ==== */

    @Test
    void lEnvoiManuelUtiliseLeTemplateTuteurQuandLeContactEstTuteurPro() {
        Contact contact = contact(1, "contact@acme.fr");
        when(contactJpaRepository.findById(1)).thenReturn(contact);
        when(conventionJpaRepository.findConventionsValideesParTuteurPro(1))
                .thenReturn(List.of(conventionTuteur(10, contact)));

        Date datePrecedente = service.solliciterContact(1);

        assertThat(datePrecedente).isNull();
        assertThat(contact.getDateEnvoiMailOpposition()).isNotNull();
        verify(mailerService).sendDroitOpposition(eq("contact@acme.fr"), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_TUTEUR_PRO), anyString());
        verify(contactJpaRepository).saveAndFlush(contact);
    }

    @Test
    void lEnvoiManuelBasculeSurLeTemplateSignataireEnLAbsenceDeConventionCommeTuteur() {
        Contact contact = contact(1, "contact@acme.fr");
        when(contactJpaRepository.findById(1)).thenReturn(contact);
        when(conventionJpaRepository.findConventionsValideesParTuteurPro(1)).thenReturn(List.of());
        when(conventionJpaRepository.findConventionsValideesParSignataire(1))
                .thenReturn(List.of(conventionSignataire(11, contact)));

        service.solliciterContact(1);

        verify(mailerService).sendDroitOpposition(anyString(), any(),
                eq(TemplateMail.CODE_DROIT_OPPOSITION_SIGNATAIRE), anyString());
    }

    @Test
    void laRelanceEstAutoriseeEtRenvoieLaDateDuPremierEnvoi() {
        Contact contact = contact(1, "contact@acme.fr");
        Date premierEnvoi = new Date(0);
        contact.setDateEnvoiMailOpposition(premierEnvoi);
        when(contactJpaRepository.findById(1)).thenReturn(contact);
        when(conventionJpaRepository.findConventionsValideesParTuteurPro(1))
                .thenReturn(List.of(conventionTuteur(10, contact)));

        Date datePrecedente = service.solliciterContact(1);

        assertThat(datePrecedente).isEqualTo(premierEnvoi);
        assertThat(contact.getDateEnvoiMailOpposition()).isNotEqualTo(premierEnvoi);
        verify(mailerService).sendDroitOpposition(anyString(), any(), anyString(), anyString());
    }

    @Test
    void lEnvoiManuelEstRefusePourUnContactSansConventionValidee() {
        Contact contact = contact(1, "contact@acme.fr");
        when(contactJpaRepository.findById(1)).thenReturn(contact);
        when(conventionJpaRepository.findConventionsValideesParTuteurPro(1)).thenReturn(List.of());
        when(conventionJpaRepository.findConventionsValideesParSignataire(1)).thenReturn(List.of());

        assertThatThrownBy(() -> service.solliciterContact(1))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("aucune convention validée");
        assertThat(contact.getDateEnvoiMailOpposition()).isNull();
    }

    @Test
    void lEnvoiManuelEstRefusePourUnContactSansMailOuAyantDejaRefuse() {
        Contact sansMail = contact(1, null);
        when(contactJpaRepository.findById(1)).thenReturn(sansMail);
        assertThatThrownBy(() -> service.solliciterContact(1))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("adresse mail");

        Contact aRefuse = contact(2, "contact@acme.fr");
        aRefuse.setRefusEtreContacte(true);
        when(contactJpaRepository.findById(2)).thenReturn(aRefuse);
        assertThatThrownBy(() -> service.solliciterContact(2))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("ne souhaitait pas être contacté");

        verify(mailerService, never()).sendDroitOpposition(anyString(), any(), anyString(), anyString());
    }

    @Test
    void lEnvoiManuelEchoueSurUnContactInconnu() {
        when(contactJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> service.solliciterContact(99)).isInstanceOf(AppException.class);
    }

    /* ==== Saisie en masse des refus ==== */

    @Test
    void uneAdresseConnueMetAJourToutesLesFichesCorrespondantes() {
        Contact premier = contact(1, "contact@acme.fr");
        Contact second = contact(2, "contact@acme.fr");
        when(contactJpaRepository.findByMailIgnoreCase("contact@acme.fr")).thenReturn(List.of(premier, second));

        DroitOppositionResultDto result = service.enregistrerRefus(List.of("Contact@Acme.fr"));

        assertThat(result.getTraitees()).hasSize(1);
        assertThat(result.getTraitees().get(0).getMail()).isEqualTo("contact@acme.fr");
        assertThat(result.getTraitees().get(0).getNbContacts()).isEqualTo(2);
        assertThat(premier.getRefusEtreContacte()).isTrue();
        assertThat(premier.getDateRefusEtreContacte()).isNotNull();
        assertThat(premier.getOrigineRefusEtreContacte()).isEqualTo(DroitOpposition.ORIGINE_REFUS_MASSE);
        assertThat(second.getRefusEtreContacte()).isTrue();
    }

    @Test
    void lesAdressesInconnuesEtInvalidesSontRemonteesSansBloquerLesAutres() {
        Contact connu = contact(1, "connu@acme.fr");
        when(contactJpaRepository.findByMailIgnoreCase("connu@acme.fr")).thenReturn(List.of(connu));
        when(contactJpaRepository.findByMailIgnoreCase("inconnu@acme.fr")).thenReturn(List.of());

        DroitOppositionResultDto result = service.enregistrerRefus(
                List.of("connu@acme.fr", "inconnu@acme.fr", "pas-une-adresse"));

        assertThat(result.getTraitees()).hasSize(1);
        assertThat(result.getInconnues()).containsExactly("inconnu@acme.fr");
        assertThat(result.getInvalides()).containsExactly("pas-une-adresse");
        assertThat(connu.getRefusEtreContacte()).isTrue();
    }

    @Test
    void lesDoublonsEtLesLignesVidesSontIgnores() {
        Contact connu = contact(1, "connu@acme.fr");
        when(contactJpaRepository.findByMailIgnoreCase("connu@acme.fr")).thenReturn(List.of(connu));

        DroitOppositionResultDto result = service.enregistrerRefus(
                Arrays.asList(" connu@acme.fr ", "CONNU@ACME.FR", "   ", null));

        assertThat(result.getTraitees()).hasSize(1);
        verify(contactJpaRepository, times(1)).findByMailIgnoreCase("connu@acme.fr");
    }

    @Test
    void unRefusDejaEnregistreConserveSaDateDOrigine() {
        Contact contact = contact(1, "connu@acme.fr");
        contact.setRefusEtreContacte(true);
        Date dateOrigine = new Date(0);
        contact.setDateRefusEtreContacte(dateOrigine);
        contact.setOrigineRefusEtreContacte(DroitOpposition.ORIGINE_REFUS_MANUEL);
        when(contactJpaRepository.findByMailIgnoreCase("connu@acme.fr")).thenReturn(List.of(contact));

        service.enregistrerRefus(List.of("connu@acme.fr"));

        assertThat(contact.getDateRefusEtreContacte()).isEqualTo(dateOrigine);
        assertThat(contact.getOrigineRefusEtreContacte()).isEqualTo(DroitOpposition.ORIGINE_REFUS_MANUEL);
    }

    @Test
    void uneSaisieVideNeDeclencheAucunTraitement() {
        assertThat(service.enregistrerRefus(null).getTraitees()).isEmpty();
        assertThat(service.enregistrerRefus(List.of()).getTraitees()).isEmpty();
        verify(contactJpaRepository, never()).findByMailIgnoreCase(anyString());
    }
}
