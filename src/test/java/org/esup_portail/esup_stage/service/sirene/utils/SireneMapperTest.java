package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.model.*;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SireneMapperTest {

    private SireneMapper mapper;
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;
    private NafN5JpaRepository nafN5JpaRepository;
    private PaysJpaRepository paysJpaRepository;
    private TypeStructureJpaRepository typeStructureJpaRepository;
    private EffectifJpaRepository effectifJpaRepository;

    private Pays france;

    @BeforeEach
    void setUp() {
        mapper = new SireneMapper();
        statutJuridiqueJpaRepository = mock(StatutJuridiqueJpaRepository.class);
        nafN5JpaRepository = mock(NafN5JpaRepository.class);
        paysJpaRepository = mock(PaysJpaRepository.class);
        typeStructureJpaRepository = mock(TypeStructureJpaRepository.class);
        effectifJpaRepository = mock(EffectifJpaRepository.class);
        ReflectionTestUtils.setField(mapper, "statutJuridiqueJpaRepository", statutJuridiqueJpaRepository);
        ReflectionTestUtils.setField(mapper, "nafN5JpaRepository", nafN5JpaRepository);
        ReflectionTestUtils.setField(mapper, "paysJpaRepository", paysJpaRepository);
        ReflectionTestUtils.setField(mapper, "typeStructureJpaRepository", typeStructureJpaRepository);
        ReflectionTestUtils.setField(mapper, "effectifJpaRepository", effectifJpaRepository);

        france = new Pays();
        france.setLib("France");
        when(paysJpaRepository.findById(82)).thenReturn(france);
    }

    private SirenResponse.EtablissementSiren etablissement() {
        SirenResponse.EtablissementSiren etablissement = new SirenResponse.EtablissementSiren();
        etablissement.setUniteLegale(new SirenResponse.EtablissementSiren.UniteLegale());
        return etablissement;
    }

    @Test
    void etablissementNullDonneNull() {
        assertThat(mapper.toStructure(null)).isNull();
    }

    @Test
    void etablissementCompletEstProjeteEnStructure() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.setSiret("12345678901234");
        etablissement.setNaf_n5("62.01Z");
        etablissement.getUniteLegale().setDenominationUniteLegale("ACME SAS");
        etablissement.getUniteLegale().setStatutJuridique("5710");
        etablissement.getUniteLegale().setTrancheEffectifsUniteLegale("21");
        etablissement.getUniteLegale().setStatutDiffusionUniteLegale("O");

        NafN5 naf = new NafN5();
        naf.setLibelle("Programmation informatique");
        when(nafN5JpaRepository.findByCode("62.01Z")).thenReturn(naf);

        TypeStructure entreprise = new TypeStructure();
        StatutJuridique sas = new StatutJuridique();
        sas.setLibelle("SAS");
        sas.setTypeStructure(entreprise);
        when(statutJuridiqueJpaRepository.findByCode("57")).thenReturn(sas);

        Effectif effectif = new Effectif();
        when(effectifJpaRepository.findById((Integer) 4)).thenReturn(Optional.of(effectif));

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getNumeroSiret()).isEqualTo("12345678901234");
        assertThat(structure.getRaisonSociale()).isEqualTo("ACME SAS");
        assertThat(structure.getNafN5()).isSameAs(naf);
        assertThat(structure.getActivitePrincipale()).isEqualTo("Programmation informatique");
        assertThat(structure.getStatutJuridique()).isSameAs(sas);
        assertThat(structure.getTypeStructure()).isSameAs(entreprise);
        assertThat(structure.getEffectif()).isSameAs(effectif);
        assertThat(structure.getPays()).isSameAs(france);
        assertThat(structure.isEstValidee()).isFalse();
        assertThat(structure.getTemEnServStructure()).isTrue();
        assertThat(structure.isTemSiren()).isTrue();
        assertThat(structure.isTemDiffusibleSirene()).isTrue();
        assertThat(structure.isVerrouillageSynchroStructureSirene()).isFalse();
    }

    @Test
    void enseigneDeLaPeriodeActivePrimeSurLaDenomination() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.getUniteLegale().setDenominationUniteLegale("RAISON LEGALE");

        SirenResponse.EtablissementSiren.PeriodeEtablissement periodeClose =
                new SirenResponse.EtablissementSiren.PeriodeEtablissement();
        periodeClose.setDateFin("2020-01-01");
        periodeClose.setEnseigne1Etablissement("VIEILLE ENSEIGNE");
        SirenResponse.EtablissementSiren.PeriodeEtablissement periodeActive =
                new SirenResponse.EtablissementSiren.PeriodeEtablissement();
        periodeActive.setEnseigne1Etablissement("ENSEIGNE ACTUELLE");
        etablissement.setPeriodesEtablissement(List.of(periodeClose, periodeActive));

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getRaisonSociale()).isEqualTo("ENSEIGNE ACTUELLE");
    }

    @Test
    void personnePhysiqueEstNommeeAvecCivilite() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.getUniteLegale().setNomUniteLegale("DUPONT");
        etablissement.getUniteLegale().setPrenomUsuelUniteLegale("MARIE");
        etablissement.getUniteLegale().setSexeUniteLegale("F");

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getRaisonSociale()).isEqualTo("MADAME MARIE DUPONT");
    }

    @Test
    void raisonSocialeNonDiffusibleEstLibellee() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.getUniteLegale().setDenominationUniteLegale("[ND]");
        etablissement.getUniteLegale().setStatutDiffusionUniteLegale("P");

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getRaisonSociale()).isEqualTo("[Non diffusé]");
        assertThat(structure.isTemDiffusibleSirene()).isFalse();
        assertThat(structure.isVerrouillageSynchroStructureSirene()).isTrue();
    }

    @Test
    void statutJuridiqueInconnuRetombeSurAutre() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.getUniteLegale().setStatutJuridique("9999");
        when(statutJuridiqueJpaRepository.findByCode(anyString())).thenReturn(null);
        StatutJuridique autre = new StatutJuridique();
        TypeStructure typeAutre = new TypeStructure();
        autre.setTypeStructure(typeAutre);
        when(statutJuridiqueJpaRepository.findByLibelle("Autre")).thenReturn(autre);

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getStatutJuridique()).isSameAs(autre);
        assertThat(structure.getTypeStructure()).isSameAs(typeAutre);
    }

    @Test
    void sansStatutJuridiqueLeTypeParDefautEstEntreprisePrivee() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        StatutJuridique autre = new StatutJuridique();
        when(statutJuridiqueJpaRepository.findByLibelle("Autre")).thenReturn(autre);
        TypeStructure parDefaut = new TypeStructure();
        when(typeStructureJpaRepository.findById(3)).thenReturn(parDefaut);

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getStatutJuridique()).isSameAs(autre);
        assertThat(structure.getTypeStructure()).isSameAs(parDefaut);
    }

    @Test
    void paysEtrangerEstResoluParCodeCog() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse =
                new SirenResponse.EtablissementSiren.AdresseEtablissement();
        adresse.setCodePaysEtrangerEtablissement("99109");
        etablissement.setAdresse(adresse);
        Pays allemagne = new Pays();
        allemagne.setLib("Allemagne");
        when(paysJpaRepository.findByCog(99109)).thenReturn(allemagne);

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getPays()).isSameAs(allemagne);
    }

    @Test
    void paysEtrangerEstResoluParLibelleNormalise() {
        SirenResponse.EtablissementSiren etablissement = etablissement();
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse =
                new SirenResponse.EtablissementSiren.AdresseEtablissement();
        adresse.setLibellePaysEtrangerEtablissement("ALLEMAGNE");
        etablissement.setAdresse(adresse);
        when(paysJpaRepository.findByLib("ALLEMAGNE")).thenReturn(null);
        Pays allemagne = new Pays();
        allemagne.setLib("Allemagne");
        when(paysJpaRepository.findAll()).thenReturn(List.of(france, allemagne));

        Structure structure = mapper.toStructure(etablissement);

        assertThat(structure.getPays()).isSameAs(allemagne);
    }

    @Test
    void updateStructureRefuseLesParametresNuls() {
        assertThatThrownBy(() -> mapper.updateStructure(null, new Structure()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.updateStructure(new SirenResponse(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mapper.updateStructure(new SirenResponse(), new Structure()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Aucun établissement");
    }

    @Test
    void updateStructureNEcrasePasLesChampsAbsents() {
        Structure structure = new Structure();
        structure.setRaisonSociale("ANCIEN NOM");
        NafN5 ancienNaf = new NafN5();
        structure.setNafN5(ancienNaf);
        Effectif ancienEffectif = new Effectif();
        structure.setEffectif(ancienEffectif);
        TypeStructure ancienType = new TypeStructure();
        structure.setTypeStructure(ancienType);

        SirenResponse reponse = new SirenResponse();
        SirenResponse.EtablissementSiren etablissement = etablissement();
        reponse.setEtablissement(etablissement);
        StatutJuridique autre = new StatutJuridique();
        when(statutJuridiqueJpaRepository.findByLibelle("Autre")).thenReturn(autre);

        Structure resultat = mapper.updateStructure(reponse, structure);

        assertThat(resultat.getRaisonSociale()).isEqualTo("ANCIEN NOM");
        assertThat(resultat.getNafN5()).isSameAs(ancienNaf);
        assertThat(resultat.getEffectif()).isSameAs(ancienEffectif);
        assertThat(resultat.getTypeStructure()).isSameAs(ancienType);
        assertThat(resultat.getStatutJuridique()).isSameAs(autre);
    }

    @Test
    void updateStructureMetAJourLesChampsPresents() {
        Structure structure = new Structure();
        structure.setRaisonSociale("ANCIEN NOM");

        SirenResponse reponse = new SirenResponse();
        SirenResponse.EtablissementSiren etablissement = etablissement();
        etablissement.getUniteLegale().setDenominationUniteLegale("NOUVEAU NOM");
        etablissement.getUniteLegale().setStatutJuridique("5710");
        etablissement.getUniteLegale().setTrancheEffectifsUniteLegale("42");
        reponse.setEtablissement(etablissement);

        TypeStructure entreprise = new TypeStructure();
        StatutJuridique sas = new StatutJuridique();
        sas.setTypeStructure(entreprise);
        when(statutJuridiqueJpaRepository.findByCode("57")).thenReturn(sas);
        Effectif grosEffectif = new Effectif();
        when(effectifJpaRepository.findById((Integer) 6)).thenReturn(Optional.of(grosEffectif));

        Structure resultat = mapper.updateStructure(reponse, structure);

        assertThat(resultat.getRaisonSociale()).isEqualTo("NOUVEAU NOM");
        assertThat(resultat.getStatutJuridique()).isSameAs(sas);
        assertThat(resultat.getTypeStructure()).isSameAs(entreprise);
        assertThat(resultat.getEffectif()).isSameAs(grosEffectif);
    }

    @Test
    void toStructureListMappeTousLesEtablissements() {
        SirenResponse reponse = new SirenResponse();
        reponse.setEtablissements(List.of(etablissement(), etablissement()));
        when(statutJuridiqueJpaRepository.findByLibelle(anyString())).thenReturn(new StatutJuridique());
        when(typeStructureJpaRepository.findById(anyInt())).thenReturn(new TypeStructure());

        List<Structure> structures = mapper.toStructureList(reponse);

        assertThat(structures).hasSize(2);
    }
}
