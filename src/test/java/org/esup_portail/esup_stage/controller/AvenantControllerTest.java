package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.config.properties.SignatureProperties;
import org.esup_portail.esup_stage.dto.AvenantDto;
import org.esup_portail.esup_stage.dto.AvenantResponseDto;
import org.esup_portail.esup_stage.dto.ConfigAlerteMailDto;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.dto.MetadataDto;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvenantControllerTest {

    private AvenantController controller;
    private AvenantJpaRepository avenantJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private AppConfigService appConfigService;
    private MailerService mailerService;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private EnseignantJpaRepository enseignantJpaRepository;
    private UniteGratificationJpaRepository uniteGratificationJpaRepository;
    private UniteDureeJpaRepository uniteDureeJpaRepository;
    private ModeVersGratificationJpaRepository modeVersGratificationJpaRepository;
    private DeviseJpaRepository deviseJpaRepository;
    private PeriodeInterruptionAvenantJpaRepository periodeInterruptionAvenantJpaRepository;
    private ConventionService conventionService;
    private SignatureService signatureService;
    private SignatureProperties signatureProperties;

    @BeforeEach
    void setUp() {
        controller = new AvenantController();
        avenantJpaRepository = mock(AvenantJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        mailerService = mock(MailerService.class);
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        enseignantJpaRepository = mock(EnseignantJpaRepository.class);
        uniteGratificationJpaRepository = mock(UniteGratificationJpaRepository.class);
        uniteDureeJpaRepository = mock(UniteDureeJpaRepository.class);
        modeVersGratificationJpaRepository = mock(ModeVersGratificationJpaRepository.class);
        deviseJpaRepository = mock(DeviseJpaRepository.class);
        periodeInterruptionAvenantJpaRepository = mock(PeriodeInterruptionAvenantJpaRepository.class);
        conventionService = mock(ConventionService.class);
        signatureService = mock(SignatureService.class);
        signatureProperties = mock(SignatureProperties.class);
        controller.avenantJpaRepository = avenantJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.appConfigService = appConfigService;
        controller.mailerService = mailerService;
        controller.serviceJpaRepository = serviceJpaRepository;
        controller.contactJpaRepository = contactJpaRepository;
        controller.enseignantJpaRepository = enseignantJpaRepository;
        controller.uniteGratificationJpaRepository = uniteGratificationJpaRepository;
        controller.uniteDureeJpaRepository = uniteDureeJpaRepository;
        controller.modeVersGratificationJpaRepository = modeVersGratificationJpaRepository;
        controller.deviseJpaRepository = deviseJpaRepository;
        controller.periodeInterruptionAvenantJpaRepository = periodeInterruptionAvenantJpaRepository;
        controller.conventionService = conventionService;
        controller.signatureService = signatureService;
        controller.signatureProperties = signatureProperties;

        when(appConfigService.getConfigAlerteMail()).thenReturn(new ConfigAlerteMailDto());
        when(avenantJpaRepository.save(any(Avenant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(avenantJpaRepository.saveAndFlush(any(Avenant.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void connecte(String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(java.util.Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
    }

    private Avenant avenantDe(String identEtudiant) {
        Avenant avenant = new Avenant();
        avenant.setId(9);
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        avenant.setConvention(convention);
        return avenant;
    }

    @Test
    void getByIdControleLAccesEtudiant() {
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        connecte("etu1", Role.ETU);
        assertThat(controller.getById(9)).isNotNull();

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getById(9)).isInstanceOf(AppException.class);

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void getByConventionFiltreLesEtudiants() {
        connecte("etu1", Role.ETU);
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("etu1");
        convention.setEtudiant(etudiant);
        when(conventionJpaRepository.findById(42)).thenReturn(convention);
        when(avenantJpaRepository.findByConvention(42)).thenReturn(List.of(avenantDe("etu1")));

        List<AvenantResponseDto> avenants = controller.getByConvention(42);

        assertThat(avenants).hasSize(1);

        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.getByConvention(42)).isInstanceOf(AppException.class);
    }

    @Test
    void validateBasculeLeFlagEtNotifie() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        AvenantResponseDto dto = controller.validate(9);

        assertThat(avenant.isValidationAvenant()).isTrue();
        assertThat(avenant.getDateValidation()).isNotNull();
        assertThat(dto).isNotNull();
        verify(mailerService).sendValidationMail(eq(avenant.getConvention()), eq(avenant), any(Utilisateur.class),
                eq(TemplateMail.CODE_AVENANT_VALIDATION), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.validate(99)).isInstanceOf(AppException.class);
    }

    @Test
    void cancelValidationRemetLAvenantEnAttente() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        avenant.setValidationAvenant(true);
        avenant.setDateValidation(new java.util.Date());
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);

        controller.cancelValidation(9);

        assertThat(avenant.isValidationAvenant()).isFalse();
        assertThat(avenant.getDateValidation()).isNull();

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.cancelValidation(99)).isInstanceOf(AppException.class);
    }

    private Convention conventionDe(String identEtudiant) {
        Convention convention = new Convention();
        convention.setId(42);
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(identEtudiant);
        convention.setEtudiant(etudiant);
        return convention;
    }

    private AvenantDto dtoMinimal() {
        AvenantDto dto = mock(AvenantDto.class);
        when(dto.getIdConvention()).thenReturn(42);
        when(dto.getTitreAvenant()).thenReturn("Avenant 1");
        return dto;
    }

    @Test
    void createRenseigneLesNomenclaturesEtNotifieLEtudiant() {
        connecte("etu1", Role.ETU);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        AvenantDto dto = dtoMinimal();
        when(dto.getIdService()).thenReturn(1);
        when(dto.getIdContact()).thenReturn(2);
        when(dto.getIdEnseignant()).thenReturn(3);
        when(dto.getIdUniteGratification()).thenReturn(4);
        when(dto.getIdUniteDuree()).thenReturn(5);
        when(dto.getIdModeVersGratification()).thenReturn(6);
        when(dto.getIdDevise()).thenReturn(7);
        when(serviceJpaRepository.findById(1)).thenReturn(new Service());
        when(contactJpaRepository.findById(2)).thenReturn(new Contact());
        when(enseignantJpaRepository.findById(3)).thenReturn(new Enseignant());
        when(uniteGratificationJpaRepository.findById(4)).thenReturn(new UniteGratification());
        when(uniteDureeJpaRepository.findById(5)).thenReturn(new UniteDuree());
        when(modeVersGratificationJpaRepository.findById(6)).thenReturn(new ModeVersGratification());
        when(deviseJpaRepository.findById(7)).thenReturn(new Devise());

        AvenantResponseDto reponse = controller.create(dto);

        assertThat(reponse).isNotNull();
        verify(avenantJpaRepository).saveAndFlush(any(Avenant.class));
        verify(mailerService).sendValidationMail(any(Convention.class), any(Avenant.class), any(Utilisateur.class),
                eq(TemplateMail.CODE_ETU_CREA_AVENANT), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void createNotifieLeGestionnaireEtIgnoreLesAutresRoles() {
        connecte("ges1", Role.GES);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        controller.create(dtoMinimal());
        verify(mailerService).sendValidationMail(any(Convention.class), any(Avenant.class), any(Utilisateur.class),
                eq(TemplateMail.CODE_GES_CREA_AVENANT), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        connecte("adm1", Role.ADM);
        controller.create(dtoMinimal());
        verify(mailerService, never()).sendValidationMail(any(Convention.class), any(Avenant.class), any(Utilisateur.class),
                eq(TemplateMail.CODE_ETU_CREA_AVENANT), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void createRejetteLesReferencesInconnues() {
        connecte("ges1", Role.GES);

        // convention inconnue
        AvenantDto dto = dtoMinimal();
        when(conventionJpaRepository.findById(42)).thenReturn(null);
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);

        // convention d'un autre étudiant
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));
        connecte("autre", Role.ETU);
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);

        // nomenclatures inconnues : chaque référence absente est rejetée
        connecte("ges1", Role.GES);
        AvenantDto dtoService = dtoMinimal();
        when(dtoService.getIdService()).thenReturn(1);
        assertThatThrownBy(() -> controller.create(dtoService)).isInstanceOf(AppException.class);

        AvenantDto dtoContact = dtoMinimal();
        when(dtoContact.getIdContact()).thenReturn(2);
        assertThatThrownBy(() -> controller.create(dtoContact)).isInstanceOf(AppException.class);

        AvenantDto dtoEnseignant = dtoMinimal();
        when(dtoEnseignant.getIdEnseignant()).thenReturn(3);
        assertThatThrownBy(() -> controller.create(dtoEnseignant)).isInstanceOf(AppException.class);

        AvenantDto dtoUnite = dtoMinimal();
        when(dtoUnite.getIdUniteGratification()).thenReturn(4);
        assertThatThrownBy(() -> controller.create(dtoUnite)).isInstanceOf(AppException.class);

        AvenantDto dtoDuree = dtoMinimal();
        when(dtoDuree.getIdUniteDuree()).thenReturn(5);
        assertThatThrownBy(() -> controller.create(dtoDuree)).isInstanceOf(AppException.class);

        AvenantDto dtoMode = dtoMinimal();
        when(dtoMode.getIdModeVersGratification()).thenReturn(6);
        assertThatThrownBy(() -> controller.create(dtoMode)).isInstanceOf(AppException.class);

        AvenantDto dtoDevise = dtoMinimal();
        when(dtoDevise.getIdDevise()).thenReturn(7);
        assertThatThrownBy(() -> controller.create(dtoDevise)).isInstanceOf(AppException.class);
    }

    @Test
    void updateModifieLAvenantEtNotifieSelonLeRole() {
        connecte("etu1", Role.ETU);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        when(conventionJpaRepository.findById(42)).thenReturn(conventionDe("etu1"));

        controller.update(9, dtoMinimal());
        assertThat(avenant.getTitreAvenant()).isEqualTo("Avenant 1");
        verify(mailerService).sendValidationMail(any(Convention.class), eq(avenant), any(Utilisateur.class),
                eq(TemplateMail.CODE_ETU_MODIF_AVENANT), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        connecte("ges1", Role.GES);
        controller.update(9, dtoMinimal());
        verify(mailerService).sendValidationMail(any(Convention.class), eq(avenant), any(Utilisateur.class),
                eq(TemplateMail.CODE_GES_MODIF_AVENANT), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        AvenantDto dto = dtoMinimal();
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLesPeriodesDInterruptionPuisLAvenant() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        PeriodeInterruptionAvenant periode = new PeriodeInterruptionAvenant();
        when(periodeInterruptionAvenantJpaRepository.findByAvenant(9)).thenReturn(List.of(periode));

        assertThat(controller.delete(9)).isTrue();
        verify(periodeInterruptionAvenantJpaRepository).delete(periode);
        verify(avenantJpaRepository).delete(avenant);

        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lEnvoiEnSignatureMarqueLeLoginDeLEnvoyeur() {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById((Integer) 9)).thenReturn(Optional.of(avenant));
        IdsListDto idsListDto = new IdsListDto();
        idsListDto.setIds(List.of(9));
        when(signatureService.upload(idsListDto, true)).thenReturn(1);

        assertThat(controller.envoiSignatureElectroniqueMultiple(idsListDto)).isEqualTo(1);
        assertThat(avenant.getLoginEnvoiSignature()).isEqualTo("ges1");
        verify(avenantJpaRepository).save(avenant);
    }

    @Test
    void leControleAvantSignatureVerifieLaConfiguration() {
        connecte("ges1", Role.GES);
        ConfigGeneraleDto configGeneraleDto = mock(ConfigGeneraleDto.class);
        when(appConfigService.getConfigGenerale()).thenReturn(configGeneraleDto);

        // signature désactivée
        when(configGeneraleDto.isSignatureEnabled()).thenReturn(false);
        assertThatThrownBy(() -> controller.controleSignatureElectronique(9))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("signature");

        when(configGeneraleDto.isSignatureEnabled()).thenReturn(true);

        // avenant inconnu
        when(avenantJpaRepository.findById(9)).thenReturn(null);
        assertThatThrownBy(() -> controller.controleSignatureElectronique(9)).isInstanceOf(AppException.class);

        // avenant non validé
        Avenant avenant = avenantDe("etu1");
        avenant.getConvention().setCentreGestion(new CentreGestion());
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        assertThatThrownBy(() -> controller.controleSignatureElectronique(9))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("validé");

        // Docaposte sans circuit de signature sur le centre
        avenant.setValidationAvenant(true);
        when(signatureProperties.getAppSignatureType()).thenReturn(AppSignatureEnum.DOCAPOSTE);
        assertThatThrownBy(() -> controller.controleSignatureElectronique(9))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("circuit");

        // circuit présent : le contrôle des coordonnées est délégué
        avenant.getConvention().getCentreGestion().setCircuitSignature("CIRCUIT");
        controller.controleSignatureElectronique(9);
        verify(conventionService).controleEmailTelephone(avenant.getConvention());
    }

    @Test
    void updateSignatureElectroniqueInfoRafraichitLHistorique() {
        connecte("ges1", Role.GES);
        ConfigGeneraleDto configGeneraleDto = mock(ConfigGeneraleDto.class);
        when(appConfigService.getConfigGenerale()).thenReturn(configGeneraleDto);

        when(configGeneraleDto.isSignatureEnabled()).thenReturn(false);
        assertThatThrownBy(() -> controller.updateSignatureElectroniqueInfo(9)).isInstanceOf(AppException.class);

        when(configGeneraleDto.isSignatureEnabled()).thenReturn(true);
        when(avenantJpaRepository.findById(9)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateSignatureElectroniqueInfo(9)).isInstanceOf(AppException.class);

        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        assertThat(controller.updateSignatureElectroniqueInfo(9)).isNotNull();
        verify(signatureService).updateHistorique(avenant);
    }

    @Test
    void downloadDocRetourneLeFichierSigne(@TempDir Path tempDir) throws Exception {
        connecte("ges1", Role.GES);
        Avenant avenant = avenantDe("etu1");
        when(avenantJpaRepository.findById(9)).thenReturn(avenant);
        MetadataDto metadataDto = mock(MetadataDto.class);
        when(metadataDto.getTitle()).thenReturn("avenant-signe.pdf");
        when(signatureService.getPublicMetadata(avenant.getConvention(), 9)).thenReturn(metadataDto);

        Path fichier = tempDir.resolve("avenant-signe.pdf");
        Files.write(fichier, "PDF-SIGNE".getBytes());
        when(signatureService.getSignatureFilePath("avenant-signe.pdf")).thenReturn(fichier.toString());

        ResponseEntity<byte[]> reponse = controller.downloadDoc(9);
        assertThat(new String(reponse.getBody())).isEqualTo("PDF-SIGNE");

        // fichier absent
        when(signatureService.getSignatureFilePath("avenant-signe.pdf")).thenReturn(tempDir.resolve("absent.pdf").toString());
        assertThatThrownBy(() -> controller.downloadDoc(9)).isInstanceOf(AppException.class);

        // avenant inconnu
        when(avenantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.downloadDoc(99)).isInstanceOf(AppException.class);
    }
}
