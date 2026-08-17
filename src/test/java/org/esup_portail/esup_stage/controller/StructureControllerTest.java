package org.esup_portail.esup_stage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.esup_portail.esup_stage.config.properties.SireneProperties;
import org.esup_portail.esup_stage.dto.ImportReportDto;
import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.dto.StructureCentreGestionProprietaireDto;
import org.esup_portail.esup_stage.dto.StructureConfidentialiteDto;
import org.esup_portail.esup_stage.dto.StructureDto;
import org.esup_portail.esup_stage.dto.StructureFormDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.AppConfigService;
import org.esup_portail.esup_stage.service.ConfidentialiteAccessService;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.esup_portail.esup_stage.service.Structure.utils.CsvStructureImportUtils;
import org.esup_portail.esup_stage.service.sirene.SireneService;
import org.esup_portail.esup_stage.service.sirene.model.ListStructureSireneDTO;
import org.esup_portail.esup_stage.dto.ConfigGeneraleDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
    private ConfidentialiteAccessService confidentialiteAccessService;

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
        confidentialiteAccessService = mock(ConfidentialiteAccessService.class);

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
        ReflectionTestUtils.setField(controller, "confidentialiteAccessService", confidentialiteAccessService);
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
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));
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
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));
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
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));

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
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));
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
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
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
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);
        CentreGestion centre = new CentreGestion();
        centre.setId(11);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));
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

    @Test
    void getOrCreateSynchroniseAvecSireneQuandActif() {
        connecte("adm1", Role.ADM);
        Structure existante = new Structure();
        existante.setId(7);
        existante.setTemEnServStructure(true);
        existante.setNumeroSiret(SIRET_VALIDE);
        when(structureJpaRepository.findById((Integer) 7)).thenReturn(Optional.of(existante));

        Structure demande = new Structure();
        demande.setId(7);
        controller.getOrCreate(demande);

        verify(sireneService).update(anyString(), eq(existante));
    }

    @Test
    void getOrCreateParEtudiantNeValidePasAutomatiquement() {
        connecte("etu1", Role.ETU);
        when(structureJpaRepository.findBySiret(SIRET_VALIDE)).thenReturn(null);

        Structure demande = new Structure();
        demande.setNumeroSiret(SIRET_VALIDE);
        demande.setRaisonSociale("ETU SARL");
        controller.getOrCreate(demande);

        assertThat(demande.isEstValidee()).isFalse();
        assertThat(demande.getLoginCreation()).isNull();
    }

    // ------------------------------------------------------------------
    // search + exports
    // ------------------------------------------------------------------

    @Test
    void searchSansApiSireneRetourneLaPaginationLocale() {
        connecte("adm1", Role.ADM);
        when(structureRepository.findPaginated(1, 50, "id", "asc", "{}")).thenReturn(new ArrayList<>(List.of(new Structure())));
        when(structureRepository.count("{}")).thenReturn(1L);

        PaginatedResponse<StructureDto> reponse = controller.search(1, 50, "id", "asc", "{}", null);

        assertThat(reponse.getTotal()).isEqualTo(1);
        assertThat(reponse.getData()).hasSize(1);
        verify(sireneService, never()).getEtablissementFiltered(anyInt(), anyInt(), anyString());
    }

    private void activeApiSirene() {
        sireneProperties.setUrl("https://api.insee.fr");
        sireneProperties.setToken("token");
        sireneProperties.setNombreMinimumResultats(5);
    }

    private ListStructureSireneDTO reponseSirene(int total, Structure... structures) {
        ListStructureSireneDTO reponse = mock(ListStructureSireneDTO.class);
        when(reponse.getStructures()).thenReturn(List.of(structures));
        when(reponse.getTotal()).thenReturn(total);
        return reponse;
    }

    @Test
    void searchCompleteLaPremierePageAvecLApiSirene() {
        connecte("ges1", Role.GES);
        activeApiSirene();
        String filters = "{\"numeroSiret\":{\"value\":\"732\",\"type\":\"text\"}}";
        Structure locale = new Structure();
        locale.setNumeroSiret("111");
        when(structureRepository.findPaginated(eq(1), eq(50), anyString(), anyString(), eq(filters)))
                .thenReturn(new ArrayList<>(List.of(locale)));
        when(structureRepository.count(filters)).thenReturn(1L);

        Structure doublon = new Structure();
        doublon.setNumeroSiret("111");
        Structure nouvelle = new Structure();
        nouvelle.setNumeroSiret("222");
        Structure sansSiret = new Structure();
        ListStructureSireneDTO reponseApi = reponseSirene(10, doublon, nouvelle, sansSiret);
        when(sireneService.getEtablissementFiltered(1, 49, filters)).thenReturn(reponseApi);

        PaginatedResponse<StructureDto> reponse = controller.search(1, 50, "id", "asc", filters, null);

        assertThat(reponse.getData()).hasSize(3); // locale + 2 résultats non doublons
        assertThat(reponse.getTotal()).isEqualTo(11);
    }

    @Test
    void searchRemplaceLesPagesSuivantesParLApiSirene() {
        connecte("ges1", Role.GES);
        activeApiSirene();
        String filters = "{\"numeroSiret\":{\"value\":\"732\",\"type\":\"text\"}}";
        Structure locale = new Structure();
        locale.setNumeroSiret("111");
        when(structureRepository.findPaginated(eq(2), eq(50), anyString(), anyString(), eq(filters)))
                .thenReturn(new ArrayList<>(List.of(locale)));
        when(structureRepository.count(filters)).thenReturn(1L);

        Structure doublon = new Structure();
        doublon.setNumeroSiret("111");
        Structure nouvelle = new Structure();
        nouvelle.setNumeroSiret("222");
        ListStructureSireneDTO reponseApi = reponseSirene(10, doublon, nouvelle);
        when(sireneService.getEtablissementFiltered(2, 50, filters)).thenReturn(reponseApi);

        PaginatedResponse<StructureDto> reponse = controller.search(2, 50, "id", "asc", filters, null);

        assertThat(reponse.getData()).hasSize(1); // page API sans le doublon local
        assertThat(reponse.getTotal()).isEqualTo(11);
    }

    @Test
    void searchNInterrogeSireneQuePourLaFrance() {
        connecte("ges1", Role.GES);
        activeApiSirene();
        when(structureRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenAnswer(inv -> new ArrayList<Structure>());
        when(structureRepository.count(anyString())).thenReturn(0L);
        ListStructureSireneDTO reponseVide = reponseSirene(0);
        when(sireneService.getEtablissementFiltered(anyInt(), anyInt(), anyString())).thenReturn(reponseVide);

        // pays unique France (valeur String)
        controller.search(1, 50, "id", "asc", "{\"pays.id\":{\"value\":\"82\"}}", null);
        verify(sireneService, org.mockito.Mockito.times(1)).getEtablissementFiltered(anyInt(), anyInt(), anyString());

        // liste de pays contenant la France
        controller.search(1, 50, "id", "asc", "{\"pays.id\":{\"value\":[\"82\",\"100\"]}}", null);
        verify(sireneService, org.mockito.Mockito.times(2)).getEtablissementFiltered(anyInt(), anyInt(), anyString());

        // valeur numérique convertie en chaîne
        controller.search(1, 50, "id", "asc", "{\"pays.id\":{\"value\":82}}", null);
        verify(sireneService, org.mockito.Mockito.times(3)).getEtablissementFiltered(anyInt(), anyInt(), anyString());

        // liste sans la France : pas d'appel supplémentaire
        controller.search(1, 50, "id", "asc", "{\"pays.id\":{\"value\":[\"100\"]}}", null);
        verify(sireneService, org.mockito.Mockito.times(3)).getEtablissementFiltered(anyInt(), anyInt(), anyString());
    }

    @Test
    void searchNInterrogePasSirenePourUnEtudiantAutoriseACreer() {
        ConfigGeneraleDto config = new ConfigGeneraleDto();
        config.setAutoriserEtudiantACreerEntrepriseFrance(true);
        when(appConfigService.getConfigGenerale()).thenReturn(config);
        connecte("etu1", Role.ETU);
        activeApiSirene();
        when(structureRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenAnswer(inv -> new ArrayList<Structure>());
        when(structureRepository.count(anyString())).thenReturn(0L);

        controller.search(1, 50, "id", "asc", "{\"pays.id\":{\"value\":\"82\"}}", null);

        verify(sireneService, never()).getEtablissementFiltered(anyInt(), anyInt(), anyString());
    }

    @Test
    void lesExportsDelegentAuRepository() {
        when(structureRepository.exportExcel("{}", "id", "asc", "{}")).thenReturn(new byte[]{1, 2});
        assertThat(controller.exportExcel("{}", "id", "asc", "{}", null).getBody()).hasSize(2);

        when(structureRepository.exportCsv("{}", "id", "asc", "{}")).thenReturn(new StringBuilder("a;b"));
        assertThat(controller.exportCsv("{}", "id", "asc", "{}", null).getBody()).isEqualTo("a;b");
    }

    // ------------------------------------------------------------------
    // confidentialité des coordonnées
    // ------------------------------------------------------------------

    @Test
    void lesCoordonneesConfidentiellesSontEvalueesSelonLeRole() {
        Structure structure = new Structure();
        structure.setRaisonSociale("SECRETE");
        structure.setConfidentialiteCoordonnees(true);
        when(structureJpaRepository.findById(7)).thenReturn(structure);

        // admin : jamais masqué
        connecte("adm1", Role.ADM);
        assertThat(controller.getById(7)).isNotNull();

        // étudiant : masqué
        connecte("etu1", Role.ETU);
        assertThat(controller.getById(7)).isNotNull();

        // gestionnaire sans centre rattaché : masqué
        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of());
        assertThat(controller.getById(7)).isNotNull();

        // gestionnaire dont le centre a (ou non) le droit de voir
        when(confidentialiteAccessService.isGestionnaire(any())).thenReturn(true);
        CentreGestion centre = new CentreGestion();
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(centre));
        when(confidentialiteAccessService.canViewStructureCoordinates(anyList(), eq(structure))).thenReturn(true);
        assertThat(controller.getById(7)).isNotNull();
        when(confidentialiteAccessService.canViewStructureCoordinates(anyList(), eq(structure))).thenReturn(false);
        assertThat(controller.getById(7)).isNotNull();
    }

    // ------------------------------------------------------------------
    // centre de gestion propriétaire + nomenclatures
    // ------------------------------------------------------------------

    @Test
    void leCentreProprietaireSuitLesReglesDeRole() {
        // admin avec centre demandé inexistant
        connecte("adm1", Role.ADM);
        StructureFormDto dto = formulaireValide();
        dto.setIdCentreGestionProprietaire(33);
        when(centreGestionJpaRepository.findById((Integer) 33)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);

        // admin avec centre trouvé
        when(centreGestionJpaRepository.findById((Integer) 33)).thenReturn(Optional.of(new CentreGestion()));
        assertThat(controller.create(dto)).isNotNull();

        // gestionnaire sans uid exploitable
        connecte("", Role.GES);
        StructureFormDto dtoGes = formulaireValide();
        assertThatThrownBy(() -> controller.create(dtoGes))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        // gestionnaire sans centre rattaché
        connecte("ges1", Role.GES);
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of());
        assertThatThrownBy(() -> controller.create(dtoGes))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN));

        // plusieurs centres possibles sans choix explicite
        CentreGestion c1 = new CentreGestion();
        c1.setId(1);
        c1.setNomCentre("A");
        CentreGestion c2 = new CentreGestion();
        c2.setId(2);
        c2.setNomCentre("B");
        when(confidentialiteAccessService.getCentresDemandeur(any())).thenReturn(List.of(c1, c2));
        assertThatThrownBy(() -> controller.create(dtoGes))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Plusieurs centres");

        // choix explicite d'un centre autorisé
        StructureFormDto dtoChoisi = formulaireValide();
        dtoChoisi.setIdCentreGestionProprietaire(2);
        assertThat(controller.create(dtoChoisi)).isNotNull();
    }

    @Test
    void updatePeutChangerLeCentreProprietaire() {
        connecte("adm1", Role.ADM);
        Structure structure = new Structure();
        when(structureJpaRepository.findById(7)).thenReturn(structure);
        CentreGestion centre = new CentreGestion();
        when(centreGestionJpaRepository.findById((Integer) 33)).thenReturn(Optional.of(centre));
        StructureFormDto dto = formulaireValide();
        dto.setIdCentreGestionProprietaire(33);

        controller.update(7, dto);

        assertThat(structure.getCentreGestionProprietaire()).isSameAs(centre);
    }

    @Test
    void lesNomenclaturesInconnuesSontRejetees() {
        connecte("adm1", Role.ADM);

        when(typeStructureJpaRepository.findById(anyInt())).thenReturn(null);
        assertThatThrownBy(() -> controller.create(formulaireValide()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Type de structure");
        when(typeStructureJpaRepository.findById(anyInt())).thenReturn(new TypeStructure());

        when(statutJuridiqueJpaRepository.findById(anyInt())).thenReturn(null);
        assertThatThrownBy(() -> controller.create(formulaireValide()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Statut juridique");
        when(statutJuridiqueJpaRepository.findById(anyInt())).thenReturn(new StatutJuridique());

        when(paysJpaRepository.findById(anyInt())).thenReturn(null);
        assertThatThrownBy(() -> controller.create(formulaireValide()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Pays");
        when(paysJpaRepository.findById(anyInt())).thenReturn(new Pays());

        StructureFormDto dto = formulaireValide();
        dto.setCodeNafN5("62.01Z");
        when(nafN5JpaRepository.findByCode("62.01Z")).thenReturn(null);
        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Code APE");
        when(nafN5JpaRepository.findByCode("62.01Z")).thenReturn(new NafN5());
        assertThat(controller.create(dto)).isNotNull();
    }

    @Test
    void unSiretLaPosteNonNumeriqueEstRejete() {
        connecte("adm1", Role.ADM);
        StructureFormDto dto = formulaireValide();
        dto.setNumeroSiret("35600000000E00");

        assertThatThrownBy(() -> controller.create(dto)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // import CSV
    // ------------------------------------------------------------------

    private static final String ENTETE_CSV = "NumeroRNE;RaisonSociale;NumeroSiret;ActivitePrincipale;CodeAPE;Voie;CodePostal;Commune;Telephone;Fax;SiteWeb;Mail;TypeStructure;StatutJuridique;Effectif;Pays";

    private void brancheImportCsv() {
        CsvStructureImportUtils csvUtils = new CsvStructureImportUtils();
        ReflectionTestUtils.setField(csvUtils, "nafN5JpaRepository", nafN5JpaRepository);
        ReflectionTestUtils.setField(csvUtils, "effectifJpaRepository", effectifJpaRepository);
        ReflectionTestUtils.setField(csvUtils, "statutJuridiqueJpaRepository", statutJuridiqueJpaRepository);
        ReflectionTestUtils.setField(csvUtils, "typeStructureJpaRepository", typeStructureJpaRepository);
        ReflectionTestUtils.setField(csvUtils, "paysJpaRepository", paysJpaRepository);
        ReflectionTestUtils.setField(controller, "csvUtils", csvUtils);
    }

    private ResponseEntity<?> importe(String contenu) {
        return controller.importStructures(new ByteArrayInputStream(contenu.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void importCsvCreeLesStructuresValides() {
        connecte("adm1", Role.ADM);
        brancheImportCsv();
        String ligne = ";ACME;12345678901234;Conseil;;1 rue A;75001;Paris;0102030405;;;contact@acme.fr;SARL;SARL;10-19;FR";

        ResponseEntity<?> reponse = importe(ENTETE_CSV + "\n" + ligne);

        assertThat(reponse.getStatusCode().is2xxSuccessful()).isTrue();
        verify(structureService).save(eq(null), any(Structure.class));
    }

    @Test
    void importCsvAccepteLEncodageWindows1252() {
        connecte("adm1", Role.ADM);
        brancheImportCsv();
        String contenu = ENTETE_CSV + "\n;SOCIÉTÉ GÉNÉRALE;12345678901234;Conseil;;;;;;;;;SARL;SARL;;FR";

        ResponseEntity<?> reponse = controller.importStructures(
                new ByteArrayInputStream(contenu.getBytes(java.nio.charset.Charset.forName("windows-1252"))));

        assertThat(reponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void importCsvRejetteUnEnteteIncomplet() {
        connecte("adm1", Role.ADM);
        brancheImportCsv();

        ResponseEntity<?> reponse = importe("Foo;Bar\nx;y");

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ImportReportDto rapport = (ImportReportDto) reponse.getBody();
        assertThat(rapport.getFatalError()).contains("colonnes manquantes");
    }

    @Test
    void importCsvSignaleLesLignesInvalidesEtLesDoublons() {
        connecte("adm1", Role.ADM);
        brancheImportCsv();
        when(structureJpaRepository.existAndActifByNumeroSiret("99999999999999")).thenReturn(true);
        when(structureJpaRepository.existAndActifByNumeroRNE("0751234A")).thenReturn(true);
        String ligneValide = ";ACME;12345678901234;Conseil;;;;;;;;;SARL;SARL;;FR";
        String ligneInvalide = ";;123;;99Z;;123;;;;;bad@;;;;";
        String doublonSiret = ";DOUBLON;99999999999999;Conseil;;;;;;;;;SARL;SARL;;FR";
        String doublonRne = "0751234A;Lycée X;;Enseignement;;;;;;;;;Etablissement;EPLE;;FR";

        ResponseEntity<?> reponse = importe(String.join("\n", ENTETE_CSV, ligneValide, "", ligneInvalide, doublonSiret, doublonRne));

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ImportReportDto rapport = (ImportReportDto) reponse.getBody();
        assertThat(rapport.getImported()).isEqualTo(1);
        assertThat(rapport.getTotalLines()).isEqualTo(5);
        assertThat(rapport.getErrors()).isNotEmpty();
    }

    @Test
    void importCsvSurvitAUneErreurDeLecture() {
        connecte("adm1", Role.ADM);
        brancheImportCsv();
        InputStream fluxEnPanne = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("flux interrompu");
            }
        };

        ResponseEntity<?> reponse = controller.importStructures(fluxEnPanne);

        assertThat(reponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ImportReportDto rapport = (ImportReportDto) reponse.getBody();
        assertThat(rapport.getFatalError()).contains("Erreur import");
    }
}
