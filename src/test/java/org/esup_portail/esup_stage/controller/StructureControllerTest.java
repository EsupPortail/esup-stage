package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.SireneProperties;
import org.esup_portail.esup_stage.dto.StructureCentreGestionProprietaireDto;
import org.esup_portail.esup_stage.dto.StructureConfidentialiteDto;
import org.esup_portail.esup_stage.dto.StructureDto;
import org.esup_portail.esup_stage.dto.StructureFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConfidentialiteService;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.sirene.SireneService;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StructureControllerTest {

    private static final String SIRET_VALIDE = "73282932000074";      // clé de Luhn correcte
    private static final String SIRET_INVALIDE = "73282932000075";
    private static final String SIRET_LA_POSTE_VALIDE = "35600000000001";   // somme des chiffres multiple de 5
    private static final String SIRET_LA_POSTE_INVALIDE = "35600000000002";

    private StructureController controller;
    private StructureRepository structureRepository;
    private StructureJpaRepository structureJpaRepository;
    private EffectifJpaRepository effectifJpaRepository;
    private TypeStructureJpaRepository typeStructureJpaRepository;
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;
    private NafN5JpaRepository nafN5JpaRepository;
    private PaysJpaRepository paysJpaRepository;
    private CentreGestionJpaRepository centreGestionJpaRepository;
    private AppConfigService appConfigService;
    private SireneService sireneService;
    private StructureService structureService;
    private SireneProperties sireneProperties;

    @BeforeEach
    void setUp() {
        controller = new StructureController();
        structureRepository = mock(StructureRepository.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        effectifJpaRepository = mock(EffectifJpaRepository.class);
        typeStructureJpaRepository = mock(TypeStructureJpaRepository.class);
        statutJuridiqueJpaRepository = mock(StatutJuridiqueJpaRepository.class);
        nafN5JpaRepository = mock(NafN5JpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        appConfigService = mock(AppConfigService.class);
        sireneService = mock(SireneService.class);
        structureService = mock(StructureService.class);
        sireneProperties = new SireneProperties();

        ReflectionTestUtils.setField(controller, "structureRepository", structureRepository);
        ReflectionTestUtils.setField(controller, "structureJpaRepository", structureJpaRepository);
        ReflectionTestUtils.setField(controller, "effectifJpaRepository", effectifJpaRepository);
        ReflectionTestUtils.setField(controller, "typeStructureJpaRepository", typeStructureJpaRepository);
        ReflectionTestUtils.setField(controller, "statutJuridiqueJpaRepository", statutJuridiqueJpaRepository);
        ReflectionTestUtils.setField(controller, "nafN5JpaRepository", nafN5JpaRepository);
        ReflectionTestUtils.setField(controller, "paysJpaRepository", paysJpaRepository);
        ReflectionTestUtils.setField(controller, "centreGestionJpaRepository", centreGestionJpaRepository);
        ReflectionTestUtils.setField(controller, "appConfigService", appConfigService);
        ReflectionTestUtils.setField(controller, "sireneService", sireneService);
        ReflectionTestUtils.setField(controller, "structureService", structureService);
        ReflectionTestUtils.setField(controller, "confidentialiteService", new ConfidentialiteService());
        ReflectionTestUtils.setField(controller, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(controller, "sireneProperties", sireneProperties);

        when(appConfigService.getConfigGenerale()).thenReturn(new ConfigGeneraleDto());
        when(structureService.save(any(), any(Structure.class))).thenAnswer(inv -> inv.getArgument(1));
        when(effectifJpaRepository.findById(anyInt())).thenReturn(new Effectif());
        when(typeStructureJpaRepository.findById(anyInt())).thenReturn(new TypeStructure());
        when(statutJuridiqueJpaRepository.findById(anyInt())).thenReturn(new StatutJuridique());
        when(paysJpaRepository.findById(anyInt())).thenReturn(new Pays());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Utilisateur connecte(String uid, String... roleCodes) {
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
        return utilisateur;
    }

    private StructureFormDto formulaireValide() {
        StructureFormDto dto = new StructureFormDto();
        dto.setRaisonSociale("ACME");
        dto.setNumeroSiret(SIRET_VALIDE);
        dto.setActivitePrincipale("Conseil");
        dto.setIdEffectif(1);
        dto.setIdTypeStructure(3);
        dto.setIdStatutJuridique(2);
        dto.setIdPays(82);
        return dto;
    }

    // ------------------------------------------------------------------
    // getById / delete / sirene
    // ------------------------------------------------------------------

    @Test
    void getByIdRenvoieLaStructure() {
        connecte("adm1", Role.ADM);
        Structure structure = new Structure();
        structure.setRaisonSociale("ACME");
        when(structureJpaRepository.findById(7)).thenReturn(structure);

        StructureDto dto = controller.getById(7);

        assertThat(dto.getRaisonSociale()).isEqualTo("ACME");
    }

    @Test
    void getByIdEchoueSiInconnue() {
        when(structureJpaRepository.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteDelegueAuService() {
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);

        controller.delete(7);

        verify(structureService).delete(structure);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void lesInfosSireneExposentLaConfiguration() {
        sireneProperties.setUrl("https://api.insee.fr");
        sireneProperties.setToken("token");
        sireneProperties.setNombreMinimumResultats(5);

        assertThat(controller.getSireneInfo().getIsApiSireneActive()).isTrue();
        assertThat(controller.getSireneInfo().getNombreResultats()).isEqualTo(5);
    }

    @Test
    void updateFromSireneExigeUnSiret() {
        connecte("adm1", Role.ADM);
        when(structureJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.updateFromSirene(99)).isInstanceOf(AppException.class);

        Structure sansSiret = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(sansSiret);
        assertThatThrownBy(() -> controller.updateFromSirene(7))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("SIRET est vide");

        Structure avecSiret = new Structure();
        avecSiret.setNumeroSiret(SIRET_VALIDE);
        when(structureJpaRepository.findById(8)).thenReturn(avecSiret);
        controller.updateFromSirene(8);
        verify(sireneService).update(anyString(), any(Structure.class));
    }

    // ------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------

    @Test
    void createValideLaStructurePourUnGestionnaire() {
        Utilisateur gestionnaire = connecte("ges1", Role.GES);
        CentreGestion centre = new CentreGestion();
        centre.setId(11);
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre));
        when(structureJpaRepository.findBySiret(SIRET_VALIDE)).thenReturn(null);

        StructureDto dto = controller.create(formulaireValide());

        assertThat(dto.getRaisonSociale()).isEqualTo("ACME");
        verify(structureService).save(any(), any(Structure.class));
    }

    @Test
    void createRefuseUnSiretDejaActif() {
        connecte("ges1", Role.GES);
        Structure existante = new Structure();
        existante.setTemEnServStructure(true);
        existante.setRaisonSociale("DOUBLON SA");
        when(structureJpaRepository.findBySiret(SIRET_VALIDE)).thenReturn(existante);

        assertThatThrownBy(() -> controller.create(formulaireValide()))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void createRefuseUnSiretInvalide() {
        connecte("ges1", Role.GES);
        CentreGestion centre = new CentreGestion();
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre));
        StructureFormDto dto = formulaireValide();
        dto.setNumeroSiret(SIRET_INVALIDE);

        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("SIRET est invalide");
    }

    @Test
    void lesSiretsLaPosteOntLeurPropreControle() {
        connecte("ges1", Role.GES);
        CentreGestion centre = new CentreGestion();
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre));

        StructureFormDto valide = formulaireValide();
        valide.setNumeroSiret(SIRET_LA_POSTE_VALIDE);
        assertThat(controller.create(valide)).isNotNull();

        StructureFormDto invalide = formulaireValide();
        invalide.setNumeroSiret(SIRET_LA_POSTE_INVALIDE);
        assertThatThrownBy(() -> controller.create(invalide)).isInstanceOf(AppException.class);
    }

    @Test
    void createExigeApeOuActivitePrincipale() {
        connecte("ges1", Role.GES);
        CentreGestion centre = new CentreGestion();
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre));
        StructureFormDto dto = formulaireValide();
        dto.setActivitePrincipale(null);
        dto.setCodeNafN5(null);

        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("code APE");
    }

    // ------------------------------------------------------------------
    // update / confidentialite / centre propriétaire
    // ------------------------------------------------------------------

    @Test
    void updateModifieLaStructureExistante() {
        connecte("adm1", Role.ADM);
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);

        StructureDto dto = controller.update(7, formulaireValide());

        assertThat(dto.getRaisonSociale()).isEqualTo("ACME");
        assertThatThrownBy(() -> controller.update(99, formulaireValide())).isInstanceOf(AppException.class);
    }

    @Test
    void laConfidentialiteEstInterditeAuxEtudiantsEtEnseignants() {
        connecte("etu1", Role.ETU);
        StructureConfidentialiteDto dto = new StructureConfidentialiteDto();
        dto.setConfidentialiteCoordonnees(true);

        assertThatThrownBy(() -> controller.updateConfidentialite(7, dto))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    void laConfidentialiteEstModifiableParUnGestionnaire() {
        connecte("ges1", Role.GES);
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);
        StructureConfidentialiteDto dto = new StructureConfidentialiteDto();
        dto.setConfidentialiteCoordonnees(true);

        controller.updateConfidentialite(7, dto);

        assertThat(structure.isConfidentialiteCoordonnees()).isTrue();
    }

    @Test
    void leCentreProprietaireEstControleParRole() {
        connecte("ges1", Role.GES);
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);
        CentreGestion centre = new CentreGestion();
        centre.setId(11);
        when(centreGestionJpaRepository.findAllByGestionnaireUid("ges1")).thenReturn(List.of(centre));
        StructureCentreGestionProprietaireDto dto = new StructureCentreGestionProprietaireDto();
        dto.setIdCentreGestionProprietaire(11);

        controller.updateCentreGestionProprietaire(7, dto);
        assertThat(structure.getCentreGestionProprietaire()).isSameAs(centre);

        // centre non autorisé pour ce gestionnaire
        dto.setIdCentreGestionProprietaire(99);
        assertThatThrownBy(() -> controller.updateCentreGestionProprietaire(7, dto))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));
    }

    // ------------------------------------------------------------------
    // getOrCreate
    // ------------------------------------------------------------------

    @Test
    void getOrCreateRenvoieLaStructureExistanteParId() {
        connecte("adm1", Role.ADM);
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setDesactiverMajAutoEtabSelection(true);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        Structure existante = new Structure();
        existante.setId(7);
        existante.setTemEnServStructure(true);
        existante.setRaisonSociale("ACME");
        when(structureJpaRepository.findById((Integer) 7)).thenReturn(Optional.of(existante));

        Structure demande = new Structure();
        demande.setId(7);

        assertThat(controller.getOrCreate(demande).getRaisonSociale()).isEqualTo("ACME");
    }

    @Test
    void getOrCreateRefuseUnSiretActifExistant() {
        connecte("adm1", Role.ADM);
        Structure existante = new Structure();
        existante.setTemEnServStructure(true);
        existante.setRaisonSociale("DOUBLON");
        when(structureJpaRepository.findBySiret(SIRET_VALIDE)).thenReturn(existante);

        Structure demande = new Structure();
        demande.setNumeroSiret(SIRET_VALIDE);

        assertThatThrownBy(() -> controller.getOrCreate(demande))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void getOrCreateSansSiretRetrouveParRaisonSociale() {
        connecte("adm1", Role.ADM);
        Structure existante = new Structure();
        existante.setRaisonSociale("ONG X");
        when(structureJpaRepository.findByRaisonSociale("ONG X")).thenReturn(existante);

        Structure demande = new Structure();
        demande.setRaisonSociale("ONG X");

        assertThat(controller.getOrCreate(demande).getRaisonSociale()).isEqualTo("ONG X");
    }

    @Test
    void getOrCreateSansSiretNiRaisonSocialeEchoue() {
        connecte("adm1", Role.ADM);

        assertThatThrownBy(() -> controller.getOrCreate(new Structure()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("raison sociale est obligatoire");
    }

    @Test
    void getOrCreateCreeUneNouvelleStructureValidee() {
        connecte("adm1", Role.ADM);
        when(structureJpaRepository.findBySiret(SIRET_VALIDE)).thenReturn(null);

        Structure demande = new Structure();
        demande.setNumeroSiret(SIRET_VALIDE);
        demande.setRaisonSociale("NOUVELLE");

        StructureDto dto = controller.getOrCreate(demande);

        assertThat(dto.getRaisonSociale()).isEqualTo("NOUVELLE");
        assertThat(demande.isEstValidee()).isTrue();
        assertThat(demande.getLoginCreation()).isEqualTo("adm1");
        verify(structureService).save(any(), any(Structure.class));
    }
}
