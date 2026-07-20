package org.esup_portail.esup_stage.service.impression;

import org.esup_portail.esup_stage.model.Avenant;
import org.esup_portail.esup_stage.model.CentreGestion;
import org.esup_portail.esup_stage.model.Convention;
import org.esup_portail.esup_stage.model.Pays;
import org.esup_portail.esup_stage.repository.PaysJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PreviewConventionFactoryTest {

    private PreviewConventionFactory factory;
    private PaysJpaRepository paysJpaRepository;

    @BeforeEach
    void setUp() {
        factory = new PreviewConventionFactory();
        paysJpaRepository = mock(PaysJpaRepository.class);
        factory.paysJpaRepository = paysJpaRepository;
    }

    private CentreGestion centre() {
        CentreGestion centreGestion = new CentreGestion();
        centreGestion.setCodeUniversite("UL");
        return centreGestion;
    }

    @Test
    void laConventionFictiveEstCompletePourLApercu() {
        Pays france = new Pays();
        france.setLib("FRANCE");
        when(paysJpaRepository.findById(82)).thenReturn(france);

        Convention convention = factory.createFictionalConvention(centre());

        assertThat(convention.getId()).isEqualTo(999);
        assertThat(convention.getEtudiant().getNom()).isEqualTo("Dupont");
        assertThat(convention.getEtudiant().getCodeUniversite()).isEqualTo("UL");
        assertThat(convention.getEnseignant().getNom()).isEqualTo("Martin");
        assertThat(convention.getStructure().getRaisonSociale()).isEqualTo("ACME Solutions");
        assertThat(convention.getStructure().getPays()).isSameAs(france);
        assertThat(convention.getService().getNom()).isEqualTo("R&D");
        assertThat(convention.getNomenclature().getTypeConvention()).isEqualTo("Convention de stage");
        assertThat(convention.getDateDebutStage()).isBefore(convention.getDateFinStage());
        assertThat(convention.getPeriodeStage()).hasSize(1);
        assertThat(convention.getGratificationStage()).isTrue();
    }

    @Test
    void unPaysDeSubstitutionEstCreeSiLaFranceEstAbsente() {
        when(paysJpaRepository.findById(82)).thenReturn(null);

        Convention convention = factory.createFictionalConvention(null);

        assertThat(convention.getStructure().getPays().getLib()).isEqualTo("FRANCE");
        assertThat(convention.getEtudiant().getCodeUniversite()).isEqualTo("UNIV");
        assertThat(convention.getEtape().getId().getCodeUniversite()).isEqualTo("UNIV");
    }

    @Test
    void lAvenantFictifEtLeContexteSontConstruits() {
        when(paysJpaRepository.findById(82)).thenReturn(null);
        Convention convention = factory.createFictionalConvention(centre());

        Avenant avenant = factory.createFictionalAvenant(convention);
        assertThat(avenant.getId()).isEqualTo(12345);
        assertThat(avenant.getConvention()).isSameAs(convention);

        assertThat(factory.createPreviewContext(centre(), new CentreGestion())).isNotNull();
    }
}
