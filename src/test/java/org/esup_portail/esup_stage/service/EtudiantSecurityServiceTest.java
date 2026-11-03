package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.dto.ConventionFormationDto;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.service.apogee.ApogeeService;
import org.esup_portail.esup_stage.service.ldap.model.LdapUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EtudiantSecurityServiceTest {

    private static final int ID_CENTRE = 1;

    private final EtudiantSecurityService service = new EtudiantSecurityService();
    private final ApogeeService apogeeService = mock(ApogeeService.class);
    private final Utilisateur utilisateur = new Utilisateur();

    @BeforeEach
    void setUp() {
        service.apogeeService = apogeeService;
    }

    @Test
    void ignoreEtudiantAbsentDuReferentielEtConserveLesSuivants() {
        when(apogeeService.getInscriptions(utilisateur, "absent", null))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND, "Aucune donnée trouvée"));
        when(apogeeService.getInscriptions(utilisateur, "present", null))
                .thenReturn(List.of(inscriptionDansCentre(ID_CENTRE)));

        List<LdapUser> resultat = List.of(ldapEtudiant("absent"), ldapEtudiant("present")).stream()
                .filter(etudiant -> service.isLdapEtudiantInCentreGestionUtilisateur(utilisateur, etudiant, List.of(ID_CENTRE), List.of()))
                .toList();

        assertThat(resultat).extracting(LdapUser::getCodEtu).containsExactly("present");
    }

    @Test
    void propageLesErreursTechniquesDuReferentiel() {
        when(apogeeService.getInscriptions(utilisateur, "etudiant", null))
                .thenThrow(new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Une erreur technique est survenue."));

        assertThatThrownBy(() -> service.isLdapEtudiantInCentreGestionUtilisateur(utilisateur, ldapEtudiant("etudiant"), List.of(ID_CENTRE), List.of()))
                .isInstanceOf(AppException.class);
    }

    private LdapUser ldapEtudiant(String codEtu) {
        LdapUser ldapUser = new LdapUser();
        ldapUser.setCodEtu(codEtu);
        return ldapUser;
    }

    private ConventionFormationDto inscriptionDansCentre(int idCentre) {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setId(idCentre);
        ConventionFormationDto inscription = new ConventionFormationDto();
        inscription.setCentreGestion(centreGestion);
        return inscription;
    }
}
