package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Enseignant;
import org.esup_portail.esup_stage.model.Etudiant;
import org.esup_portail.esup_stage.model.PersonnelCentreGestion;
import org.esup_portail.esup_stage.model.Role;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConventionServiceTest {

    private final ConventionService conventionService = new ConventionService();

    @Test
    void parseNumTelKeepsValidFixedLineNumbersForGeneralUse() {
        assertThat(conventionService.parseNumTel("0182280026")).isEqualTo("+33182280026");
    }

    @Test
    void parseNumTelMobileRejectsFixedLineNumbersRejectedByDocaposteOtp() {
        assertThat(conventionService.parseNumTelMobile("0182280026")).isNull();
        assertThat(conventionService.parseNumTelMobile("0596782950")).isNull();
        assertThat(conventionService.parseNumTelMobile("0188321350")).isNull();
    }

    @Test
    void parseNumTelMobileFormatsValidMobileNumbers() {
        assertThat(conventionService.parseNumTelMobile("0612345678")).isEqualTo("+33612345678");
    }

    // ------------------------------------------------------------------
    // canViewEditConvention — scénarios métier cumul de rôles
    // ------------------------------------------------------------------

    @Test
    void unAdminVoitToutesLesConventions() {
        assertThatCode(() -> conventionService.canViewEditConvention(new Convention(), utilisateur("adm1", Role.ADM)))
                .doesNotThrowAnyException();
    }

    @Test
    void unEtudiantNeVoitQueSesConventions() {
        Convention convention = conventionAvecEtudiant("etu1");

        assertThatCode(() -> conventionService.canViewEditConvention(convention, utilisateur("etu1", Role.ETU)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> conventionService.canViewEditConvention(convention, utilisateur("autre", Role.ETU)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void unEnseignantSeulNeVoitQueSesConventionsReferent() {
        Convention convention = conventionAvecEnseignantEtCentre("ens1", "ges1");

        assertThatCode(() -> conventionService.canViewEditConvention(convention, utilisateur("ens1", Role.ENS)))
                .doesNotThrowAnyException();
        // Même s'il est aussi dans les personnels du centre, sans rôle GES le périmètre centre reste fermé
        assertThatThrownBy(() -> conventionService.canViewEditConvention(convention, utilisateur("ges1", Role.ENS)))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> conventionService.canViewEditConvention(convention, utilisateur("autre", Role.ENS)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unGestionnaireSeulVoitLesConventionsDeSonCentre() {
        Convention convention = conventionAvecEnseignantEtCentre("ensRef", "ges1");

        assertThatCode(() -> conventionService.canViewEditConvention(convention, utilisateur("ges1", Role.GES)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> conventionService.canViewEditConvention(convention, utilisateur("autre", Role.GES)))
                .isInstanceOf(AppException.class);
    }

    @Test
    void unGestionnaireEnseignantVoitUneConventionDuCentreMemeSIlNEstPasReferent() {
        // Symptôme métier : liste GES OK, clic 404 avant correctif
        Convention convention = conventionAvecEnseignantEtCentre("autreEns", "gesEns1");
        Utilisateur cumul = utilisateur("gesEns1", Role.GES, Role.ENS);

        assertThatCode(() -> conventionService.canViewEditConvention(convention, cumul))
                .doesNotThrowAnyException();
    }

    @Test
    void unGestionnaireEnseignantVoitAussiSesConventionsReferent() {
        Convention convention = conventionAvecEnseignantEtCentre("gesEns1", "autreGes");
        Utilisateur cumul = utilisateur("gesEns1", Role.GES, Role.ENS);

        assertThatCode(() -> conventionService.canViewEditConvention(convention, cumul))
                .doesNotThrowAnyException();
    }

    @Test
    void unGestionnaireEnseignantNeVoitPasHorsPerimetre() {
        Convention convention = conventionAvecEnseignantEtCentre("autreEns", "autreGes");
        Utilisateur cumul = utilisateur("gesEns1", Role.GES, Role.ENS);

        assertThatThrownBy(() -> conventionService.canViewEditConvention(convention, cumul))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void unResponsableGestionnaireEnseignantVoitSonCentre() {
        Convention convention = conventionAvecEnseignantEtCentre("autreEns", "resp1");
        Utilisateur cumul = utilisateur("resp1", Role.RESP_GES, Role.ENS);

        assertThatCode(() -> conventionService.canViewEditConvention(convention, cumul))
                .doesNotThrowAnyException();
    }

    @Test
    void isEnseignantSansGestionnaireDetecteLeCumul() {
        assertThat(conventionService.isEnseignantSansGestionnaire(utilisateur("ens1", Role.ENS))).isTrue();
        assertThat(conventionService.isEnseignantSansGestionnaire(utilisateur("ges1", Role.GES))).isFalse();
        assertThat(conventionService.isEnseignantSansGestionnaire(utilisateur("cumul", Role.GES, Role.ENS))).isFalse();
        assertThat(conventionService.isEnseignantSansGestionnaire(utilisateur("cumul", Role.RESP_GES, Role.ENS))).isFalse();
        assertThat(conventionService.isEnseignantSansGestionnaire(utilisateur("adm", Role.ADM, Role.ENS))).isFalse();
    }

    private Utilisateur utilisateur(String uid, String... roleCodes) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setUid(uid);
        utilisateur.setLogin(uid);
        utilisateur.setRoles(Arrays.stream(roleCodes).map(code -> {
            Role role = new Role();
            role.setCode(code);
            return role;
        }).toList());
        return utilisateur;
    }

    private Convention conventionAvecEtudiant(String uid) {
        Convention convention = new Convention();
        Etudiant etudiant = new Etudiant();
        etudiant.setIdentEtudiant(uid);
        convention.setEtudiant(etudiant);
        return convention;
    }

    private Convention conventionAvecEnseignantEtCentre(String uidEnseignant, String uidPersonnel) {
        Convention convention = new Convention();
        Enseignant enseignant = new Enseignant();
        enseignant.setUidEnseignant(uidEnseignant);
        convention.setEnseignant(enseignant);

        CentreGestion centreGestion = new CentreGestion();
        PersonnelCentreGestion personnel = new PersonnelCentreGestion();
        personnel.setUidPersonnel(uidPersonnel);
        centreGestion.setPersonnels(List.of(personnel));
        convention.setCentreGestion(centreGestion);
        return convention;
    }
}
