package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.dto.LdapSearchDto;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.CritereGestionJpaRepository;
import org.esup_portail.esup_stage.repository.PersonnelCentreGestionJpaRepository;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeResponse;
import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeSearch;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtudiantSecurityServiceTest {

    private EtudiantSecurityService service;
    private ApogeeService apogeeService;
    private PersonnelCentreGestionJpaRepository personnelCentreGestionJpaRepository;
    private CritereGestionJpaRepository critereGestionJpaRepository;

    @BeforeEach
    void setUp() {
        service = new EtudiantSecurityService();
        apogeeService = mock(ApogeeService.class);
        personnelCentreGestionJpaRepository = mock(PersonnelCentreGestionJpaRepository.class);
        critereGestionJpaRepository = mock(CritereGestionJpaRepository.class);
        service.apogeeService = apogeeService;
        service.personnelCentreGestionJpaRepository = personnelCentreGestionJpaRepository;
        service.critereGestionJpaRepository = critereGestionJpaRepository;
    }

    private Utilisateur gestionnaire(String uid) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        Role role = new Role();
        role.setCode(Role.GES);
        utilisateur.setRoles(List.of(role));
        return utilisateur;
    }

    private CritereGestion critere(String code, String version) {
        CritereGestion critereGestion = new CritereGestion();
        CritereGestionId id = new CritereGestionId();
        id.setCode(code);
        id.setCodeVersionEtape(version);
        critereGestion.setId(id);
        return critereGestion;
    }

    private ConventionFormationDto inscription(Integer idCentre) {
        ConventionFormationDto dto = new ConventionFormationDto();
        if (idCentre != null) {
            CentreGestion centreGestion = new CentreGestion();
            centreGestion.setId(idCentre);
            dto.setCentreGestion(centreGestion);
        }
        return dto;
    }

    @Test
    void unEtudiantEstProprietaireDeSesRessources() {
        Utilisateur etudiant = new Utilisateur();
        etudiant.setNumEtudiant("123");
        etudiant.setLogin("alice1");

        assertThat(service.isNotOwnResource(etudiant, "123")).isFalse();
        assertThat(service.isNotOwnResource(etudiant, "999")).isTrue();
        assertThat(service.isNotOwnResourceLogin(etudiant, "alice1")).isFalse();
        assertThat(service.isNotOwnResourceLogin(etudiant, "bob2")).isTrue();

        Utilisateur nonEtudiant = new Utilisateur();
        assertThat(service.isNotOwnResource(nonEtudiant, "123")).isTrue();
        assertThat(service.isNotOwnResourceLogin(nonEtudiant, "alice1")).isTrue();
    }

    @Test
    void detecteLesGestionnaires() {
        assertThat(service.isGestionnaireOrResponsableGestionnaire(gestionnaire("g1"))).isTrue();

        Utilisateur responsable = new Utilisateur();
        Role respGes = new Role();
        respGes.setCode(Role.RESP_GES);
        responsable.setRoles(List.of(respGes));
        assertThat(service.isGestionnaireOrResponsableGestionnaire(responsable)).isTrue();

        Utilisateur etudiant = new Utilisateur();
        Role etu = new Role();
        etu.setCode(Role.ETU);
        etudiant.setRoles(List.of(etu));
        assertThat(service.isGestionnaireOrResponsableGestionnaire(etudiant)).isFalse();
        assertThat(service.isGestionnaireOrResponsableGestionnaire(null)).isFalse();
    }

    @Test
    void lesCentresDeGestionDeLUtilisateurSontDeduitsDeSesAffectations() {
        Utilisateur utilisateur = gestionnaire("g1");
        PersonnelCentreGestion p1 = new PersonnelCentreGestion();
        CentreGestion c1 = new CentreGestion();
        c1.setId(11);
        p1.setCentreGestion(c1);
        PersonnelCentreGestion p2 = new PersonnelCentreGestion();
        p2.setCentreGestion(c1); // doublon
        PersonnelCentreGestion sansCentre = new PersonnelCentreGestion();
        when(personnelCentreGestionJpaRepository.findByUidPersonnel("g1"))
                .thenReturn(List.of(p1, p2, sansCentre));

        assertThat(service.getIdsCentresGestionUtilisateur(utilisateur)).containsExactly(11);
        assertThat(service.getIdsCentresGestionUtilisateur(null)).isEmpty();
        assertThat(service.getIdsCentresGestionUtilisateur(new Utilisateur())).isEmpty();
    }

    @Test
    void unEtudiantEstDansLesCentresDeSonGestionnaireViaSesInscriptions() {
        Utilisateur gestionnaire = gestionnaire("g1");
        when(apogeeService.getInscriptions(eq(gestionnaire), eq("123"), any()))
                .thenReturn(List.of(inscription(null), inscription(11)));

        assertThat(service.isEtuInCentreGestionUtilisateur(gestionnaire, "123", List.of(11))).isTrue();
        assertThat(service.isEtuInCentreGestionUtilisateur(gestionnaire, "123", List.of(99))).isFalse();
        assertThat(service.isEtuInCentreGestionUtilisateur(gestionnaire, "123", List.of())).isFalse();
        assertThat(service.isEtuInCentreGestionUtilisateur(gestionnaire, null, List.of(11))).isFalse();
    }

    @Test
    void laVarianteSansListeChargeLesCentresDuGestionnaire() {
        Utilisateur gestionnaire = gestionnaire("g1");
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        CentreGestion centre = new CentreGestion();
        centre.setId(11);
        personnel.setCentreGestion(centre);
        when(personnelCentreGestionJpaRepository.findByUidPersonnel("g1")).thenReturn(List.of(personnel));
        when(apogeeService.getInscriptions(eq(gestionnaire), eq("123"), any()))
                .thenReturn(List.of(inscription(11)));

        assertThat(service.isEtuInCentreGestionUtilisateur(gestionnaire, "123")).isTrue();

        Utilisateur etudiant = new Utilisateur();
        etudiant.setRoles(List.of());
        assertThat(service.isEtuInCentreGestionUtilisateur(etudiant, "123")).isFalse();
    }

    @Test
    void isInscriptionInCentreGestionGereLesCasLimites() {
        assertThat(service.isInscriptionInCentreGestionUtilisateur(null, List.of(1))).isFalse();
        assertThat(service.isInscriptionInCentreGestionUtilisateur(List.of(), List.of(1))).isFalse();
        assertThat(service.isInscriptionInCentreGestionUtilisateur(List.of(inscription(1)), null)).isFalse();
    }

    @Test
    void unEtudiantSaitSIlAppartientAUnCentre() {
        Utilisateur etudiant = new Utilisateur();
        etudiant.setNumEtudiant("123");
        when(apogeeService.getInscriptions(eq(etudiant), eq("123"), any()))
                .thenReturn(List.of(inscription(7)));

        assertThat(service.isEtudiantInCentreGestion(etudiant, 7)).isTrue();
        assertThat(service.isEtudiantInCentreGestion(etudiant, 8)).isFalse();
        assertThat(service.isEtudiantInCentreGestion(new Utilisateur(), 7)).isFalse();
        assertThat(service.isEtudiantInCentreGestion(null, 7)).isFalse();
    }

    @Test
    void lesCriteresDesCentresSontAgreges() {
        when(critereGestionJpaRepository.findByCentreId(1)).thenReturn(List.of(critere("SCI", null)));
        when(critereGestionJpaRepository.findByCentreId(2)).thenReturn(List.of(critere("L3", "1")));

        assertThat(service.getCriteresCentresGestionUtilisateur(List.of(1, 2))).hasSize(2);
        assertThat(service.getCriteresCentresGestionUtilisateur(List.of())).isEmpty();
        assertThat(service.getCriteresCentresGestionUtilisateur(null)).isEmpty();
    }

    @Test
    void unDiplomeEtapeCorrespondParComposanteOuParEtapeVersionnee() {
        EtudiantDiplomeEtapeResponse etudiant = new EtudiantDiplomeEtapeResponse();
        etudiant.setCodeComposante("SCI");
        etudiant.setCodeEtape("L3INFO");
        etudiant.setVersionEtape("1");

        // critère composante (pas de version)
        assertThat(service.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(etudiant, List.of(critere("sci", null)))).isTrue();
        // critère étape versionnée
        assertThat(service.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(etudiant, List.of(critere("L3INFO", "1")))).isTrue();
        assertThat(service.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(etudiant, List.of(critere("L3INFO", "2")))).isFalse();
        assertThat(service.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(etudiant, List.of())).isFalse();
        assertThat(service.isEtudiantDiplomeEtapeInCentreGestionUtilisateur(null, List.of(critere("SCI", null)))).isFalse();
    }

    @Test
    void uneRechercheDiplomeEtapeSuitLesMemesRegles() {
        EtudiantDiplomeEtapeSearch recherche = new EtudiantDiplomeEtapeSearch();
        recherche.setCodeComposante("SCI");
        recherche.setCodeEtape("L3INFO");
        recherche.setVersionEtape("1");

        assertThat(service.isRechercheDiplomeEtapeInCentreGestionUtilisateur(recherche, List.of(critere("SCI", null)))).isTrue();
        assertThat(service.isRechercheDiplomeEtapeInCentreGestionUtilisateur(recherche, List.of(critere("L3INFO", "1")))).isTrue();
        assertThat(service.isRechercheDiplomeEtapeInCentreGestionUtilisateur(recherche, List.of(critere("AUTRE", null)))).isFalse();
        assertThat(service.isRechercheDiplomeEtapeInCentreGestionUtilisateur(null, List.of(critere("SCI", null)))).isFalse();
    }

    @Test
    void uneRechercheLdapEstReconnueParSonAffectation() {
        LdapSearchDto avecAffectation = new LdapSearchDto();
        avecAffectation.setSupannEntiteAffectation("SCI");

        assertThat(service.isRechercheLdapEtudiantWithCentreGestionCriteria(avecAffectation)).isTrue();
        assertThat(service.isRechercheLdapEtudiantWithCentreGestionCriteria(new LdapSearchDto())).isFalse();
        assertThat(service.isRechercheLdapEtudiantWithCentreGestionCriteria(null)).isFalse();

        assertThat(service.isRechercheLdapEtudiantInCentreGestionUtilisateur(avecAffectation, List.of(critere("SCI", null)))).isTrue();
        assertThat(service.isRechercheLdapEtudiantInCentreGestionUtilisateur(avecAffectation, List.of(critere("L3", "1")))).isFalse();
        assertThat(service.isRechercheLdapEtudiantInCentreGestionUtilisateur(null, List.of(critere("SCI", null)))).isFalse();
    }

    @Test
    void unEtudiantLdapEstReconnuParSesAffectationsOuSesInscriptions() {
        Utilisateur gestionnaire = gestionnaire("g1");
        LdapUser etudiantLdap = new LdapUser();
        etudiantLdap.setCodEtu("123");
        etudiantLdap.setSupannEntiteAffectation(List.of("SCI", "IUT"));

        // reconnu par critère d'affectation
        assertThat(service.isLdapEtudiantInCentreGestionUtilisateur(
                gestionnaire, etudiantLdap, List.of(), List.of(critere("sci", null)))).isTrue();

        // sinon par inscription Apogée
        when(apogeeService.getInscriptions(eq(gestionnaire), eq("123"), any()))
                .thenReturn(List.of(inscription(11)));
        assertThat(service.isLdapEtudiantInCentreGestionUtilisateur(
                gestionnaire, etudiantLdap, List.of(11), List.of(critere("AUTRE", null)))).isTrue();

        assertThat(service.isLdapEtudiantInCentreGestionUtilisateur(gestionnaire, null, List.of(), List.of())).isFalse();
        assertThat(service.isLdapEtudiantInCentreGestionUtilisateur(null, etudiantLdap, List.of(), List.of())).isFalse();
    }
}
