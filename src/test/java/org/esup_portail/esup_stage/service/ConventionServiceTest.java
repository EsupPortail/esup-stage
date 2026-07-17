package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.ContactJpaRepository;
import org.esup_portail.esup_stage.repository.ServiceJpaRepository;
import org.esup_portail.esup_stage.service.Structure.StructureService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ConventionServiceTest {

    private ConventionService service;
    private ServiceJpaRepository serviceJpaRepository;
    private ContactJpaRepository contactJpaRepository;
    private StructureService structureService;

    @BeforeEach
    void setUp() {
        service = new ConventionService();
        serviceJpaRepository = mock(ServiceJpaRepository.class);
        contactJpaRepository = mock(ContactJpaRepository.class);
        structureService = mock(StructureService.class);
        service.serviceJpaRepository = serviceJpaRepository;
        service.contactJpaRepository = contactJpaRepository;
        ReflectionTestUtils.setField(service, "structureService", structureService);
    }

    private Utilisateur utilisateur(String uid, String roleCode) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        Role role = new Role();
        role.setCode(roleCode);
        utilisateur.setRoles(List.of(role));
        return utilisateur;
    }

    // ------------------------------------------------------------------
    // canViewEditConvention
    // ------------------------------------------------------------------

    @Test
    void unAdminVoitToutesLesConventions() {
        assertThatCode(() -> service.canViewEditConvention(new Convention(), utilisateur("adm1", Role.ADM)))
                .doesNotThrowAnyException();
    }

    @Test
    void unEtudiantNeVoitQueSesConventions() {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant("Etu1");
        convention.setEtudiant(etudiant);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("etu1", Role.ETU)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.ETU)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("etu1", Role.ETU)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unEnseignantNeVoitQueSesConventions() {
        Convention convention = new Convention();
        Enseignant enseignant = new Enseignant();
        enseignant.setUidEnseignant("Ens1");
        convention.setEnseignant(enseignant);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("ens1", Role.ENS)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.ENS)))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("ens1", Role.ENS)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unGestionnaireNeVoitQueLesConventionsDeSesCentres() {
        Convention convention = new Convention();
        CentreGestion centreGestion = new CentreGestion();
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        personnel.setUidPersonnel("Ges1");
        centreGestion.setPersonnels(List.of(personnel));
        convention.setCentreGestion(centreGestion);

        assertThatCode(() -> service.canViewEditConvention(convention, utilisateur("ges1", Role.GES)))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> service.canViewEditConvention(convention, utilisateur("autre", Role.GES)))
                .isInstanceOf(AppException.class);

        assertThatThrownBy(() -> service.canViewEditConvention(new Convention(), utilisateur("ges1", Role.GES)))
                .isInstanceOf(AppException.class);
    }

    // ------------------------------------------------------------------
    // validationAutoDonnees
    // ------------------------------------------------------------------

    private Convention conventionTotalementValidee() {
        Convention convention = new Convention();
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(true);
        convention.setValidationConvention(true);
        return convention;
    }

    @Test
    void laValidationCompleteValideLesDonneesDAccueil() {
        Convention convention = conventionTotalementValidee();
        Structure structure = new Structure();
        convention.setStructure(structure);
        org.esup_portail.esup_stage.model.Service serviceAccueil = new org.esup_portail.esup_stage.model.Service();
        convention.setService(serviceAccueil);
        Contact tuteur = new Contact();
        convention.setContact(tuteur);

        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));

        assertThat(structure.isEstValidee()).isTrue();
        assertThat(structure.getLoginValidation()).isEqualTo("ges1");
        assertThat(structure.getInfosAJour()).isNotNull();
        verify(structureService).save(any(), any(Structure.class));
        assertThat(serviceAccueil.getInfosAJour()).isNotNull();
        verify(serviceJpaRepository).save(serviceAccueil);
        assertThat(tuteur.getInfosAJour()).isNotNull();
        verify(contactJpaRepository).save(tuteur);
    }

    @Test
    void sansValidationCompleteRienNEstTouche() {
        Convention convention = new Convention();
        convention.setValidationPedagogique(true);
        convention.setVerificationAdministrative(true);
        convention.setValidationConvention(false);
        convention.setStructure(new Structure());

        service.validationAutoDonnees(convention, utilisateur("ges1", Role.GES));

        verify(structureService, never()).save(any(), any(Structure.class));
        verify(serviceJpaRepository, never()).save(any());
        verify(contactJpaRepository, never()).save(any());
    }

    @Test
    void laValidationCompleteToleredLesDonneesAbsentes() {
        assertThatCode(() -> service.validationAutoDonnees(conventionTotalementValidee(), utilisateur("ges1", Role.GES)))
                .doesNotThrowAnyException();
    }

    // ------------------------------------------------------------------
    // parseNumTel / initSignataires
    // ------------------------------------------------------------------

    @Test
    void lesNumerosFrancaisSontNormalisesEnE164() {
        assertThat(service.parseNumTel("06 11 22 33 44")).isEqualTo("+33611223344");
        assertThat(service.parseNumTel("+33 6 11 22 33 44")).isEqualTo("+33611223344");
        assertThat(service.parseNumTel("0000")).isNull();
        assertThat(service.parseNumTel("pas un numero")).isNull();
        assertThat(service.parseNumTel("")).isNull();
        assertThat(service.parseNumTel(null)).isNull();
    }

    @Test
    void initSignatairesCreeUnSignataireParTypeDansLOrdre() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(3);

        List<CentreGestionSignataire> signataires = service.initSignataires(centreGestion);

        assertThat(signataires).hasSize(5);
        assertThat(signataires.get(0).getOrdre()).isEqualTo(1);
        assertThat(signataires.get(4).getOrdre()).isEqualTo(5);
        assertThat(signataires)
                .allSatisfy(s -> assertThat(s.getCentreGestion()).isSameAs(centreGestion));
    }
}
