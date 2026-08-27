package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.ConventionFormDto;
import org.esup_portail.esup_stage.dto.GroupeEtudiantDto;
import org.esup_portail.esup_stage.dto.IdsListDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.security.userdetails.CasUserDetailsImpl;
import org.esup_portail.esup_stage.service.ConventionService;
import org.esup_portail.esup_stage.service.MailerService;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.ApogeeMap;
import org.esup_portail.esup_stage.service.apogee.model.EtapeInscription;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeResponse;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantRef;
import org.esup_portail.esup_stage.service.apogee.model.RegimeInscription;
import org.esup_portail.esup_stage.service.impression.ImpressionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GroupeEtudiantControllerTest {

    private GroupeEtudiantController controller;
    private GroupeEtudiantRepository groupeEtudiantRepository;
    private GroupeEtudiantJpaRepository groupeEtudiantJpaRepository;
    private HistoriqueMailGroupeJpaRepository historiqueMailGroupeJpaRepository;
    private TypeConventionJpaRepository typeConventionJpaRepository;
    private ConventionJpaRepository conventionJpaRepository;
    private EtudiantRepository etudiantRepository;
    private EtudiantGroupeEtudiantJpaRepository etudiantGroupeEtudiantJpaRepository;
    private StructureJpaRepository structureJpaRepository;
    private LangueConventionJpaRepository langueConventionJpaRepository;
    private ImpressionService impressionService;
    private ApogeeService apogeeService;
    private ConventionService conventionService;
    private MailerService mailerService;
    private PeriodeStageJpaRepository periodeStageJpaRepository;

    @BeforeEach
    void setUp() {
        controller = new GroupeEtudiantController();
        groupeEtudiantRepository = mock(GroupeEtudiantRepository.class);
        groupeEtudiantJpaRepository = mock(GroupeEtudiantJpaRepository.class);
        historiqueMailGroupeJpaRepository = mock(HistoriqueMailGroupeJpaRepository.class);
        typeConventionJpaRepository = mock(TypeConventionJpaRepository.class);
        conventionJpaRepository = mock(ConventionJpaRepository.class);
        etudiantRepository = mock(EtudiantRepository.class);
        etudiantGroupeEtudiantJpaRepository = mock(EtudiantGroupeEtudiantJpaRepository.class);
        structureJpaRepository = mock(StructureJpaRepository.class);
        langueConventionJpaRepository = mock(LangueConventionJpaRepository.class);
        impressionService = mock(ImpressionService.class);
        apogeeService = mock(ApogeeService.class);
        conventionService = mock(ConventionService.class);
        mailerService = mock(MailerService.class);
        periodeStageJpaRepository = mock(PeriodeStageJpaRepository.class);
        controller.groupeEtudiantRepository = groupeEtudiantRepository;
        controller.groupeEtudiantJpaRepository = groupeEtudiantJpaRepository;
        controller.historiqueMailGroupeJpaRepository = historiqueMailGroupeJpaRepository;
        controller.typeConventionJpaRepository = typeConventionJpaRepository;
        controller.conventionJpaRepository = conventionJpaRepository;
        controller.etudiantRepository = etudiantRepository;
        controller.etudiantGroupeEtudiantJpaRepository = etudiantGroupeEtudiantJpaRepository;
        controller.structureJpaRepository = structureJpaRepository;
        controller.langueConventionJpaRepository = langueConventionJpaRepository;
        controller.impressionService = impressionService;
        controller.apogeeService = apogeeService;
        controller.conventionService = conventionService;
        controller.mailerService = mailerService;
        controller.periodeStageJpaRepository = periodeStageJpaRepository;

        when(groupeEtudiantJpaRepository.save(any(GroupeEtudiant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupeEtudiantJpaRepository.saveAndFlush(any(GroupeEtudiant.class))).thenAnswer(inv -> inv.getArgument(0));
        when(conventionJpaRepository.save(any(Convention.class))).thenAnswer(inv -> inv.getArgument(0));
        when(etudiantGroupeEtudiantJpaRepository.save(any(EtudiantGroupeEtudiant.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Utilisateur connecte(String uid) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new CasUserDetailsImpl(utilisateur, List.of()), null));
        return utilisateur;
    }

    // ------------------------------------------------------------------
    // recherche / lecture
    // ------------------------------------------------------------------

    @Test
    void searchDelegueAuRepositoryPagine() {
        when(groupeEtudiantRepository.count(anyString())).thenReturn(1L);
        when(groupeEtudiantRepository.findPaginated(anyInt(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(List.of(new GroupeEtudiant()));

        var reponse = controller.search(1, 50, "id", "asc", "{}", new MockHttpServletResponse());

        assertThat(reponse.getTotal()).isEqualTo(1L);
        assertThat(reponse.getData()).hasSize(1);
    }

    @Test
    void getByIdEchoueSiInconnu() {
        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.getById(99)).isInstanceOf(AppException.class);

        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        assertThat(controller.getById(7)).isSameAs(groupe);
    }

    @Test
    void historiqueDesMailsDuGroupe() {
        when(historiqueMailGroupeJpaRepository.findByGroupeEtudiant(7)).thenReturn(List.of());

        assertThat(controller.getHistorique(7)).isEmpty();
    }

    @Test
    void leBrouillonDeLUtilisateurEstRetourne() {
        connecte("ges1");
        GroupeEtudiant brouillon = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findBrouillon("ges1")).thenReturn(brouillon);

        assertThat(controller.getBrouillon()).isSameAs(brouillon);
    }

    // ------------------------------------------------------------------
    // type de convention / flags
    // ------------------------------------------------------------------

    @Test
    void leTypeDeConventionEstPropageATousLesEtudiants() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        Convention conventionGroupe = new Convention();
        groupe.setConvention(conventionGroupe);
        EtudiantGroupeEtudiant etudiantGroupe = new EtudiantGroupeEtudiant();
        Convention conventionEtudiant = new Convention();
        etudiantGroupe.setConvention(conventionEtudiant);
        groupe.setEtudiantGroupeEtudiants(List.of(etudiantGroupe));
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        TypeConvention type = new TypeConvention();
        when(typeConventionJpaRepository.findById(3)).thenReturn(type);

        controller.setTypeConventionGroupe(7, 3);

        assertThat(conventionGroupe.getTypeConvention()).isSameAs(type);
        assertThat(conventionEtudiant.getTypeConvention()).isSameAs(type);
        verify(conventionJpaRepository).flush();
    }

    @Test
    void leTypeDeConventionExigeGroupeEtTypeExistants() {
        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.setTypeConventionGroupe(99, 3)).isInstanceOf(AppException.class);

        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(new GroupeEtudiant());
        when(typeConventionJpaRepository.findById(88)).thenReturn(null);
        assertThatThrownBy(() -> controller.setTypeConventionGroupe(7, 88)).isInstanceOf(AppException.class);
    }

    @Test
    void deleteSupprimeLeGroupeExistant() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);

        assertThat(controller.delete(7)).isTrue();
        verify(groupeEtudiantJpaRepository).delete(groupe);

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.delete(99)).isInstanceOf(AppException.class);
    }

    @Test
    void setInfosStageValidBasculeLeFlag() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.saveAndFlush(groupe)).thenReturn(groupe);

        assertThat(controller.setInfosStageValid(7, true).isInfosStageValid()).isTrue();
        assertThat(controller.setInfosStageValid(7, false).isInfosStageValid()).isFalse();

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.setInfosStageValid(99, true)).isInstanceOf(AppException.class);
    }

    @Test
    void validateBrouillonValideLaCreation() {
        GroupeEtudiant groupe = new GroupeEtudiant();
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.saveAndFlush(groupe)).thenReturn(groupe);

        assertThat(controller.validateBrouillon(7).isValidationCreation()).isTrue();

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.validateBrouillon(99)).isInstanceOf(AppException.class);
    }

    @Test
    void mergeObjectsPrivilegieLesValeursDuPremierObjet() throws IllegalAccessException {
        Etudiant premier = new Etudiant();
        premier.setNom("Durand");
        Etudiant second = new Etudiant();
        second.setNom("Ignoré");
        second.setPrenom("Alice");

        Etudiant fusion = GroupeEtudiantController.mergeObjects(premier, second);

        assertThat(fusion.getNom()).isEqualTo("Durand");
        assertThat(fusion.getPrenom()).isEqualTo("Alice");
    }

    // ------------------------------------------------------------------
    // fusion + validation des conventions du groupe
    // ------------------------------------------------------------------

    @Test
    void mergeAndValidateFusionneLesConventionsEtValideToutesLesEtapes() {
        connecte("ges1");
        GroupeEtudiant groupe = new GroupeEtudiant();
        Convention conventionGroupe = new Convention();
        conventionGroupe.setDetails("Détails groupe");
        groupe.setConvention(conventionGroupe);

        Convention conventionEtudiant = new Convention();
        conventionEtudiant.setSujetStage("Sujet étudiant");
        PeriodeStage periode = new PeriodeStage();
        conventionEtudiant.setPeriodeStage(List.of(periode));

        EtudiantGroupeEtudiant ege = new EtudiantGroupeEtudiant();
        ege.setConvention(conventionEtudiant);
        Convention anciennefusion = new Convention();
        ege.setMergedConvention(anciennefusion);
        groupe.setEtudiantGroupeEtudiants(List.of(ege));
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(periodeStageJpaRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupeEtudiant resultat = controller.mergeAndValidateConventions(7);

        Convention fusion = ege.getMergedConvention();
        assertThat(fusion.getSujetStage()).isEqualTo("Sujet étudiant");
        assertThat(fusion.getDetails()).isEqualTo("Détails groupe");
        assertThat(fusion.isCreationEnMasse()).isTrue();
        assertThat(fusion.isValidationCreation()).isTrue();
        assertThat(fusion.getValidationPedagogique()).isTrue();
        assertThat(fusion.getVerificationAdministrative()).isTrue();
        assertThat(fusion.getValidationConvention()).isTrue();
        assertThat(fusion.getLoginValidation()).isEqualTo("ges1");
        assertThat(fusion.getPeriodeStage()).hasSize(1);
        assertThat(resultat.isValidationCreation()).isTrue();
        verify(conventionJpaRepository).delete(anciennefusion);
        verify(conventionService).validationAutoDonnees(eq(fusion), any());

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.mergeAndValidateConventions(99)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // création en masse (Apogée)
    // ------------------------------------------------------------------

    private EtudiantDiplomeEtapeResponse etudiantApogee(String codEtu) {
        EtudiantDiplomeEtapeResponse etudiant = mock(EtudiantDiplomeEtapeResponse.class);
        when(etudiant.getCodEtu()).thenReturn(codEtu);
        when(etudiant.getAnnee()).thenReturn("2025");
        when(etudiant.getCodeComposante()).thenReturn("UFR");
        when(etudiant.getCodeDiplome()).thenReturn("DIP");
        when(etudiant.getVersionDiplome()).thenReturn("1");
        when(etudiant.getCodeEtape()).thenReturn("ETP");
        when(etudiant.getVersionEtape()).thenReturn("1");
        return etudiant;
    }

    private void stubCreationConvention() {
        EtudiantRef ref = mock(EtudiantRef.class);
        when(apogeeService.getInfoApogee(anyString(), anyString())).thenReturn(ref);
        ApogeeMap apogeeMap = mock(ApogeeMap.class);
        RegimeInscription regime = mock(RegimeInscription.class);
        when(regime.getAnnee()).thenReturn("2025");
        EtapeInscription etape = mock(EtapeInscription.class);
        when(etape.getCodeComposante()).thenReturn("UFR");
        when(etape.getCodeDiplome()).thenReturn("DIP");
        when(etape.getVersionDiplome()).thenReturn("1");
        when(etape.getCodeEtp()).thenReturn("ETP");
        when(etape.getCodVrsVet()).thenReturn("1");
        when(apogeeMap.getRegimeInscription()).thenReturn(List.of(regime));
        when(apogeeMap.getListeEtapeInscriptions()).thenReturn(List.of(etape));
        when(apogeeService.getEtudiantEtapesInscription(anyString(), anyString())).thenReturn(apogeeMap);
        when(conventionService.getCentreGestionEtab()).thenReturn(new CentreGestion());
        when(conventionService.getCentreGestion(any(), anyString(), anyString(), anyString())).thenReturn(new CentreGestion());
        when(apogeeService.resolveTypeConvention(any(), any(), any())).thenReturn(new TypeConvention());
        LangueConvention langue = new LangueConvention();
        langue.setCode("fr");
        when(langueConventionJpaRepository.findByCode("fr")).thenReturn(langue);
        doAnswer(inv -> {
            Convention convention = inv.getArgument(0);
            Etudiant etudiant = new Etudiant();
            etudiant.setNumEtudiant("num");
            convention.setEtudiant(etudiant);
            return null;
        }).when(conventionService).setConventionData(any(Convention.class), any(ConventionFormDto.class));
    }

    @Test
    void createConstruitLeGroupeDepuisApogee() {
        connecte("ges1");
        stubCreationConvention();
        when(groupeEtudiantRepository.exists("G1", 0)).thenReturn(false);

        GroupeEtudiantDto dto = new GroupeEtudiantDto();
        dto.setCodeGroupe("G1");
        dto.setNomGroupe("Groupe 1");
        dto.setEtudiantAdded(List.of(etudiantApogee("123")));

        GroupeEtudiant groupe = controller.create(dto);

        assertThat(groupe.getCode()).isEqualTo("G1");
        assertThat(groupe.getNom()).isEqualTo("Groupe 1");
        assertThat(groupe.getConvention()).isNotNull();
        assertThat(groupe.getConvention().isCreationEnMasse()).isTrue();
        assertThat(groupe.getEtudiantGroupeEtudiants()).hasSize(1);
        assertThat(groupe.getEtudiantGroupeEtudiants().get(0).getEtudiant().getNumEtudiant()).isEqualTo("num");
    }

    @Test
    void createControleCodeExistantEtListeVide() {
        when(groupeEtudiantRepository.exists("G1", 0)).thenReturn(true);
        GroupeEtudiantDto doublon = new GroupeEtudiantDto();
        doublon.setCodeGroupe("G1");
        doublon.setEtudiantAdded(List.of(etudiantApogee("123")));
        assertThatThrownBy(() -> controller.create(doublon))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Code groupe déjà existant");

        when(groupeEtudiantRepository.exists("G2", 0)).thenReturn(false);
        GroupeEtudiantDto vide = new GroupeEtudiantDto();
        vide.setCodeGroupe("G2");
        vide.setEtudiantAdded(List.of());
        assertThatThrownBy(() -> controller.create(vide))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Aucun étudiant");
    }

    @Test
    void createEchoueSiInscriptionOuTypeConventionIntrouvable() {
        connecte("ges1");
        stubCreationConvention();
        when(groupeEtudiantRepository.exists(any(), anyInt())).thenReturn(false);

        // étape d'inscription qui ne correspond pas
        GroupeEtudiantDto dto = new GroupeEtudiantDto();
        dto.setCodeGroupe("G3");
        EtudiantDiplomeEtapeResponse inconnu = etudiantApogee("456");
        when(inconnu.getCodeEtape()).thenReturn("AUTRE");
        dto.setEtudiantAdded(List.of(inconnu));
        assertThatThrownBy(() -> controller.create(dto))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("inscription");

        // type de convention non résolu
        when(apogeeService.resolveTypeConvention(any(), any(), any())).thenReturn(null);
        GroupeEtudiantDto sansType = new GroupeEtudiantDto();
        sansType.setCodeGroupe("G4");
        sansType.setEtudiantAdded(List.of(etudiantApogee("789")));
        assertThatThrownBy(() -> controller.create(sansType))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Type de convention");
    }

    // ------------------------------------------------------------------
    // mise à jour du groupe (ajout / retrait d'étudiants)
    // ------------------------------------------------------------------

    @Test
    void updateAjouteEtRetireDesEtudiants() {
        connecte("ges1");
        stubCreationConvention();

        GroupeEtudiant groupe = new GroupeEtudiant();
        EtudiantGroupeEtudiant existant = new EtudiantGroupeEtudiant();
        existant.setId(5);
        Etudiant etudiant = new Etudiant();
        etudiant.setNumEtudiant("111");
        existant.setEtudiant(etudiant);
        Convention ancienneConvention = new Convention();
        existant.setConvention(ancienneConvention);
        groupe.setEtudiantGroupeEtudiants(new ArrayList<>(List.of(existant)));
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);

        GroupeEtudiantDto dto = new GroupeEtudiantDto();
        dto.setCodeGroupe("G1bis");
        dto.setNomGroupe("Groupe renommé");
        dto.setEtudiantAdded(List.of(etudiantApogee("222")));
        dto.setEtudiantRemovedIds(List.of(5));

        GroupeEtudiant resultat = controller.update(7, dto);

        assertThat(resultat.getNom()).isEqualTo("Groupe renommé");
        assertThat(resultat.getCode()).isEqualTo("G1bis");
        assertThat(resultat.getEtudiantGroupeEtudiants()).hasSize(1);
        assertThat(resultat.getEtudiantGroupeEtudiants().get(0)).isNotSameAs(existant);
        verify(etudiantGroupeEtudiantJpaRepository).delete(existant);
        verify(conventionJpaRepository).delete(ancienneConvention);

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.update(99, dto)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // duplication
    // ------------------------------------------------------------------

    /** Convention avec le minimum requis par setValeurNomenclature() lors du clonage. */
    private Convention conventionClonable(String sujet) {
        Convention convention = new Convention();
        convention.setSujetStage(sujet);
        LangueConvention langue = new LangueConvention();
        langue.setLibelle("Français");
        convention.setLangueConvention(langue);
        TypeConvention type = new TypeConvention();
        type.setLibelle("Stage");
        convention.setTypeConvention(type);
        return convention;
    }

    @Test
    void duplicateCloneLeGroupeSesConventionsEtSesEtudiants() {
        connecte("ges1");
        GroupeEtudiant groupe = new GroupeEtudiant();
        groupe.setCode("G1");
        groupe.setNom("Groupe 1");
        groupe.setInfosStageValid(true);
        Convention conventionGroupe = conventionClonable("Sujet commun");
        groupe.setConvention(conventionGroupe);
        EtudiantGroupeEtudiant ege = new EtudiantGroupeEtudiant();
        Etudiant etudiant = new Etudiant();
        ege.setEtudiant(etudiant);
        Convention conventionEtudiant = conventionClonable("Sujet étudiant");
        ege.setConvention(conventionEtudiant);
        groupe.setEtudiantGroupeEtudiants(List.of(ege));
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.findBrouillon("ges1")).thenReturn(null);
        when(groupeEtudiantRepository.exists(anyString(), anyInt())).thenReturn(false);

        GroupeEtudiant copie = controller.duplicate(7);

        assertThat(copie.getCode()).isEqualTo("G1_copie");
        assertThat(copie.getNom()).isEqualTo("Groupe 1 (copie)");
        assertThat(copie.isValidationCreation()).isFalse();
        assertThat(copie.isInfosStageValid()).isTrue();
        assertThat(copie.getConvention()).isNotSameAs(conventionGroupe);
        assertThat(copie.getConvention().getSujetStage()).isEqualTo("Sujet commun");
        assertThat(copie.getConvention().isCreationEnMasse()).isTrue();
        assertThat(copie.getEtudiantGroupeEtudiants()).hasSize(1);
        assertThat(copie.getEtudiantGroupeEtudiants().get(0).getEtudiant()).isSameAs(etudiant);
        assertThat(copie.getEtudiantGroupeEtudiants().get(0).getMergedConvention()).isNull();
        assertThat(copie.getEtudiantGroupeEtudiants().get(0).getConvention().getSujetStage()).isEqualTo("Sujet étudiant");
    }

    @Test
    void duplicateGereLesCollisionsDeCodeEtLeBrouillonExistant() {
        connecte("ges1");
        GroupeEtudiant groupe = new GroupeEtudiant();
        groupe.setCode("G1");
        groupe.setConvention(conventionClonable("Sujet"));
        groupe.setEtudiantGroupeEtudiants(List.of());
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        when(groupeEtudiantJpaRepository.findBrouillon("ges1")).thenReturn(null);
        when(groupeEtudiantRepository.exists("G1_copie", 0)).thenReturn(true);
        when(groupeEtudiantRepository.exists("G1_copie_1", 0)).thenReturn(false);

        assertThat(controller.duplicate(7).getCode()).isEqualTo("G1_copie_1");

        when(groupeEtudiantJpaRepository.findBrouillon("ges1")).thenReturn(new GroupeEtudiant());
        assertThatThrownBy(() -> controller.duplicate(7))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("déjà un groupe");

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.duplicate(99)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // PDF zip et envoi de mails
    // ------------------------------------------------------------------

    private void stubGenerationPdf() {
        doAnswer(inv -> {
            ByteArrayOutputStream os = inv.getArgument(2);
            os.write("PDF".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(impressionService).generateConventionAvenantPDF(any(), any(), any(), any(Boolean.class));
    }

    @Test
    void lePdfDeConventionsRenvoieUneArchiveZip() {
        stubGenerationPdf();
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        convention.setEtudiant(etudiant);
        when(conventionJpaRepository.findById(1)).thenReturn(convention);

        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(1));
        byte[] archive = controller.getConventionPDF(ids).getBody();

        assertThat(archive).isNotEmpty();
        assertThat(archive[0]).isEqualTo((byte) 'P');
        assertThat(archive[1]).isEqualTo((byte) 'K');

        when(conventionJpaRepository.findById(99)).thenReturn(null);
        ids.setIds(List.of(99));
        assertThatThrownBy(() -> controller.getConventionPDF(ids)).isInstanceOf(AppException.class);
    }

    private GroupeEtudiant groupePourMail(boolean onlyMailCentre) {
        GroupeEtudiant groupe = new GroupeEtudiant();
        EtudiantGroupeEtudiant ege = new EtudiantGroupeEtudiant();
        ege.setId(5);
        Etudiant etudiant = new Etudiant();
        etudiant.setNom("Durand");
        etudiant.setPrenom("Alice");
        ege.setEtudiant(etudiant);
        Convention fusion = new Convention();
        Structure structure = new Structure();
        structure.setMail("struct@entreprise.fr");
        fusion.setStructure(structure);
        CentreGestion centre = new CentreGestion();
        centre.setOnlyMailCentreGestion(onlyMailCentre);
        centre.setMail("centre@univ.fr");
        fusion.setCentreGestion(centre);
        ege.setMergedConvention(fusion);
        groupe.setEtudiantGroupeEtudiants(List.of(ege));
        return groupe;
    }

    @Test
    void lEnvoiDeMailRegroupeParStructure() {
        Utilisateur utilisateur = connecte("ges1");
        stubGenerationPdf();
        when(historiqueMailGroupeJpaRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        GroupeEtudiant groupe = groupePourMail(false);
        when(groupeEtudiantJpaRepository.findById(7)).thenReturn(groupe);
        IdsListDto ids = new IdsListDto();
        ids.setIds(List.of(5));

        assertThat(controller.sendMail(7, "RELANCE", ids)).isTrue();
        verify(mailerService).sendMailGroupe(eq("struct@entreprise.fr"), any(Convention.class), eq(utilisateur), eq("RELANCE"), any(byte[].class));

        GroupeEtudiant groupeCentre = groupePourMail(true);
        when(groupeEtudiantJpaRepository.findById(8)).thenReturn(groupeCentre);
        assertThat(controller.sendMail(8, "RELANCE", ids)).isTrue();
        verify(mailerService).sendMailGroupe(eq("centre@univ.fr"), any(Convention.class), eq(utilisateur), eq("RELANCE"), any(byte[].class));

        when(groupeEtudiantJpaRepository.findById(99)).thenReturn(null);
        assertThatThrownBy(() -> controller.sendMail(99, "RELANCE", ids)).isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // import CSV des structures
    // ------------------------------------------------------------------

    private ByteArrayInputStream csv(String... lignes) {
        return new ByteArrayInputStream(String.join("\n", lignes).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void lImportCsvAffecteLesStructuresParRneOuSiret() {
        Structure parRne = new Structure();
        when(structureJpaRepository.findByRNE("RNE1")).thenReturn(parRne);
        Structure parSiret = new Structure();
        when(structureJpaRepository.findBySiret("SIRET1")).thenReturn(parSiret);

        Etudiant etudiant = new Etudiant();
        etudiant.setId(9);
        when(etudiantRepository.findByNumEtudiant("111")).thenReturn(etudiant);
        Etudiant etudiant2 = new Etudiant();
        etudiant2.setId(10);
        when(etudiantRepository.findByNumEtudiant("222")).thenReturn(etudiant2);

        EtudiantGroupeEtudiant ege1 = new EtudiantGroupeEtudiant();
        ege1.setConvention(new Convention());
        when(etudiantGroupeEtudiantJpaRepository.findByEtudiantAndGroupe(9, 7)).thenReturn(ege1);
        EtudiantGroupeEtudiant ege2 = new EtudiantGroupeEtudiant();
        ege2.setConvention(new Convention());
        when(etudiantGroupeEtudiantJpaRepository.findByEtudiantAndGroupe(10, 7)).thenReturn(ege2);

        controller.importStructures(csv(
                "numEtu;nom;prenom;RNE;SIRET",
                "111;Durand;Alice;RNE1;",
                "222;Martin;Paul;;SIRET1"
        ), 7);

        assertThat(ege1.getConvention().getStructure()).isSameAs(parRne);
        assertThat(ege2.getConvention().getStructure()).isSameAs(parSiret);
        verify(conventionJpaRepository).flush();
    }

    @Test
    void lImportCsvSignaleLesLignesInvalides() {
        // ni RNE ni SIRET
        assertThatThrownBy(() -> controller.importStructures(csv(
                "numEtu;nom;prenom;RNE;SIRET",
                "111;Durand;Alice;;"
        ), 7)).isInstanceOf(AppException.class).hasMessageContaining("SIRET ou RNE");

        // RNE inconnu
        when(structureJpaRepository.findByRNE("INCONNU")).thenReturn(null);
        assertThatThrownBy(() -> controller.importStructures(csv(
                "numEtu;nom;prenom;RNE;SIRET",
                "111;Durand;Alice;INCONNU;"
        ), 7)).isInstanceOf(AppException.class).hasMessageContaining("RNE");

        // étudiant inconnu
        when(structureJpaRepository.findByRNE("RNE1")).thenReturn(new Structure());
        when(etudiantRepository.findByNumEtudiant("999")).thenReturn(null);
        assertThatThrownBy(() -> controller.importStructures(csv(
                "numEtu;nom;prenom;RNE;SIRET",
                "999;Durand;Alice;RNE1;"
        ), 7)).isInstanceOf(AppException.class).hasMessageContaining("numero etudiant");

        // étudiant hors groupe
        Etudiant etudiant = new Etudiant();
        etudiant.setId(9);
        when(etudiantRepository.findByNumEtudiant("111")).thenReturn(etudiant);
        when(etudiantGroupeEtudiantJpaRepository.findByEtudiantAndGroupe(9, 7)).thenReturn(null);
        assertThatThrownBy(() -> controller.importStructures(csv(
                "numEtu;nom;prenom;RNE;SIRET",
                "111;Durand;Alice;RNE1;"
        ), 7)).isInstanceOf(AppException.class).hasMessageContaining("dans le groupe");
    }
}
