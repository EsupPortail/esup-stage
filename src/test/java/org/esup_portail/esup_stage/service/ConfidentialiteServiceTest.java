package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Confidentialite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.repository.CentreGestionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfidentialiteServiceTest {

    private final ConfidentialiteService service = new ConfidentialiteService();
    private CentreGestionJpaRepository centreGestionJpaRepository;

    @BeforeEach
    void setUp() {
        centreGestionJpaRepository = mock(CentreGestionJpaRepository.class);
        ReflectionTestUtils.setField(service, "centreGestionJpaRepository", centreGestionJpaRepository);
        // Centre ÉTABLISSEMENT configuré en confidentialité « libre » : chaque centre porteur
        // conserve alors sa propre confidentialité effective (héritage non forcé).
        when(centreGestionJpaRepository.getCentreEtablissement())
                .thenReturn(centre(99, ConfidentialiteService.CONFIDENTIALITE_LIBRE));
    }

    private CentreGestion centre(int id, String codeConfidentialite) {
        CentreGestion centre = new CentreGestion();
        centre.setId(id);
        if (codeConfidentialite != null) {
            Confidentialite confidentialite = new Confidentialite();
            confidentialite.setCode(codeConfidentialite);
            centre.setCodeConfidentialite(confidentialite);
        }
        return centre;
    }

    private CentreGestion etablissement(int id, String codeConfidentialite) {
        CentreGestion centre = centre(id, codeConfidentialite);
        NiveauCentre niveau = new NiveauCentre();
        niveau.setLibelle(ConfidentialiteService.NIVEAU_ETABLISSEMENT);
        centre.setNiveauCentre(niveau);
        return centre;
    }

    @Test
    void unCentreVoitToujoursSesPropresDonnees() {
        CentreGestion centre = centre(1, ConfidentialiteService.CONFIDENTIALITE_TOTALE);

        assertThat(service.canViewCentreData(centre, centre)).isTrue();
    }

    @Test
    void unAutreCentreNeVoitQueLesDonneesNonConfidentielles() {
        CentreGestion demandeur = centre(1, null);

        assertThat(service.canViewCentreData(demandeur, centre(2, ConfidentialiteService.PAS_DE_CONFIDENTIALITE))).isTrue();
        assertThat(service.canViewCentreData(demandeur, centre(3, ConfidentialiteService.CONFIDENTIALITE_TOTALE))).isFalse();
        assertThat(service.canViewCentreData(demandeur, null)).isFalse();
        assertThat(service.canViewCentreData(null, centre(2, null))).isFalse();
    }

    @Test
    void confidentialiteAbsenteEquivautAPasDeConfidentialite() {
        assertThat(service.canViewCentreData(centre(1, null), centre(2, null))).isTrue();
        assertThat(service.isNoConfidentiality(centre(2, null))).isTrue();
        assertThat(service.isNoConfidentiality(null)).isTrue();
    }

    @Test
    void confidentialiteLibreSansChoixOrphelinDevientTotale() {
        CentreGestion porteur = centre(2, ConfidentialiteService.CONFIDENTIALITE_LIBRE);

        assertThat(service.getEffectiveConfidentialityForCentre(porteur))
                .isEqualTo(ConfidentialiteService.CONFIDENTIALITE_TOTALE);
    }

    @Test
    void leCentreEtablissementLibreSuitLeChoixDesConventionsOrphelines() {
        // Le choix « conventions orphelines » n'a de sens qu'au niveau du centre ÉTABLISSEMENT :
        // c'est lui qui arbitre la confidentialité effective quand il est configuré en « libre ».
        CentreGestion etablissement = etablissement(2, ConfidentialiteService.CONFIDENTIALITE_LIBRE);
        Confidentialite orpheline = new Confidentialite();
        orpheline.setCode(ConfidentialiteService.PAS_DE_CONFIDENTIALITE);
        etablissement.setCodeConfidentialiteConventionOrpheline(orpheline);
        when(centreGestionJpaRepository.getCentreEtablissement()).thenReturn(etablissement);

        assertThat(service.getEffectiveConfidentialityForCentre(etablissement))
                .isEqualTo(ConfidentialiteService.PAS_DE_CONFIDENTIALITE);
        assertThat(service.canViewCentreData(centre(1, null), etablissement)).isTrue();
    }

    @Test
    void lesContactsEtServicesSuiventLaConfidentialiteDeLeurCentre() {
        CentreGestion demandeur = centre(1, null);
        CentreGestion porteurOuvert = centre(2, ConfidentialiteService.PAS_DE_CONFIDENTIALITE);
        Contact contact = new Contact();
        contact.setCentreGestion(porteurOuvert);
        org.esup_portail.esup_stage.model.Service serviceAccueil = new org.esup_portail.esup_stage.model.Service();
        serviceAccueil.setCentreGestion(porteurOuvert);

        assertThat(service.canViewContact(demandeur, contact)).isTrue();
        assertThat(service.canViewContact(demandeur, (Contact) null)).isFalse();
        assertThat(service.canViewService(demandeur, serviceAccueil)).isTrue();
        assertThat(service.canViewService(demandeur, (org.esup_portail.esup_stage.model.Service) null)).isFalse();
    }

    @Test
    void lesCoordonneesDeStructureSontProtegeesParLeurProprietaire() {
        CentreGestion demandeur = centre(1, null);

        Structure publique = new Structure();
        publique.setConfidentialiteCoordonnees(false);
        assertThat(service.canViewStructureCoordinates(demandeur, publique)).isTrue();

        Structure confidentielleSansProprietaire = new Structure();
        confidentielleSansProprietaire.setConfidentialiteCoordonnees(true);
        assertThat(service.canViewStructureCoordinates(demandeur, confidentielleSansProprietaire)).isFalse();

        Structure confidentielle = new Structure();
        confidentielle.setConfidentialiteCoordonnees(true);
        confidentielle.setCentreGestionProprietaire(centre(2, ConfidentialiteService.PAS_DE_CONFIDENTIALITE));
        assertThat(service.canViewStructureCoordinates(demandeur, confidentielle)).isTrue();

        confidentielle.setCentreGestionProprietaire(centre(3, ConfidentialiteService.CONFIDENTIALITE_TOTALE));
        assertThat(service.canViewStructureCoordinates(demandeur, confidentielle)).isFalse();

        assertThat(service.canViewStructureCoordinates(demandeur, (Structure) null)).isFalse();
    }

    @Test
    void detecteLeCentreEtablissement() {
        CentreGestion etablissement = centre(1, null);
        NiveauCentre niveau = new NiveauCentre();
        niveau.setLibelle("Etablissement");
        etablissement.setNiveauCentre(niveau);

        assertThat(service.isCentreEtablissement(etablissement)).isTrue();
        assertThat(service.isCentreEtablissement(centre(2, null))).isFalse();
        assertThat(service.isCentreEtablissement(null)).isFalse();
    }
}
