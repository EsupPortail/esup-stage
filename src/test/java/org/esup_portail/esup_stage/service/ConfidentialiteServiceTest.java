package org.esup_portail.esup_stage.service;

import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Confidentialite;
import org.esup_portail.esup_stage.model.Contact;
import org.esup_portail.esup_stage.model.NiveauCentre;
import org.esup_portail.esup_stage.model.Structure;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfidentialiteServiceTest {

    private final ConfidentialiteService service = new ConfidentialiteService();

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
        assertThat(service.isTotalConfidentiality(porteur)).isTrue();
        assertThat(service.isFreeConfidentiality(porteur)).isTrue();
    }

    @Test
    void confidentialiteLibreSuitLeChoixDesConventionsOrphelines() {
        CentreGestion porteur = centre(2, ConfidentialiteService.CONFIDENTIALITE_LIBRE);
        Confidentialite orpheline = new Confidentialite();
        orpheline.setCode(ConfidentialiteService.PAS_DE_CONFIDENTIALITE);
        porteur.setCodeConfidentialiteConventionOrpheline(orpheline);

        assertThat(service.getEffectiveConfidentialityForCentre(porteur))
                .isEqualTo(ConfidentialiteService.PAS_DE_CONFIDENTIALITE);
        assertThat(service.canViewCentreData(centre(1, null), porteur)).isTrue();
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
        assertThat(service.canViewContact(demandeur, porteurOuvert)).isTrue();
        assertThat(service.canViewService(demandeur, serviceAccueil)).isTrue();
        assertThat(service.canViewService(demandeur, (org.esup_portail.esup_stage.model.Service) null)).isFalse();
        assertThat(service.canViewService(demandeur, porteurOuvert)).isTrue();
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
    void lesCoordonneesDeStructureAvecPorteurExplicite() {
        CentreGestion demandeur = centre(1, null);
        Structure confidentielle = new Structure();
        confidentielle.setConfidentialiteCoordonnees(true);

        assertThat(service.canViewStructureCoordinates(demandeur, confidentielle, centre(2, ConfidentialiteService.PAS_DE_CONFIDENTIALITE))).isTrue();
        assertThat(service.canViewStructureCoordinates(demandeur, confidentielle, null)).isFalse();
        assertThat(service.canViewStructureCoordinates(demandeur, (Structure) null, centre(2, null))).isFalse();
        assertThat(service.canViewStructureCoordinates(demandeur, centre(2, ConfidentialiteService.PAS_DE_CONFIDENTIALITE))).isTrue();
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
