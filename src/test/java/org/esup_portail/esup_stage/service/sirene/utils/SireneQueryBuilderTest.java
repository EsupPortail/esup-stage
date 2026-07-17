package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.repository.NafN5JpaRepository;
import org.esup_portail.esup_stage.repository.StatutJuridiqueJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SireneQueryBuilderTest {

    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;
    private NafN5JpaRepository nafN5JpaRepository;

    @BeforeEach
    void setUp() {
        statutJuridiqueJpaRepository = mock(StatutJuridiqueJpaRepository.class);
        nafN5JpaRepository = mock(NafN5JpaRepository.class);
        // le constructeur alimente les champs statiques utilisés par buildLuceneQuery
        new SireneQueryBuilder(statutJuridiqueJpaRepository, nafN5JpaRepository);
    }

    @Test
    void sansFiltreSeulesLesSocietesActivesSontDemandees() {
        assertThat(SireneQueryBuilder.buildLuceneQuery("{}"))
                .isEqualTo("(etatAdministratifUniteLegale:A)");
    }

    @Test
    void raisonSocialeMultiTermesEstDecoupeeEnPrefixes() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"raisonSociale\":{\"value\":\"Air Liquide\",\"type\":\"text\"}}");

        assertThat(query).isEqualTo(
                "((raisonSociale:Air* AND raisonSociale:Liquide*)) AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void raisonSocialeMonoTermeResteSimple() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"raisonSociale\":{\"value\":\"ACME\",\"type\":\"text\"}}");

        assertThat(query).isEqualTo("(raisonSociale:ACME*) AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void siretEstRechercheSansJoker() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"numeroSiret\":{\"value\":\"12345678901234\",\"type\":\"text\"}}");

        assertThat(query).isEqualTo("(siret:12345678901234) AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void communeEstRechercheeAvecJoker() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"commune\":{\"value\":\"Nancy\",\"type\":\"text\"}}");

        assertThat(query).isEqualTo(
                "(libelleCommuneEtablissement:Nancy*) AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void statutJuridiqueEstResoluEnCodesDepuisLaBase() {
        when(statutJuridiqueJpaRepository.findCodeByIdIn(List.of(1, 2))).thenReturn(List.of("57", "10"));

        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"statutJuridique.id\":{\"value\":[1,2],\"type\":\"list\"}}");

        assertThat(query).isEqualTo(
                "(categorieJuridiqueUniteLegale:57 OR categorieJuridiqueUniteLegale:10)"
                        + " AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void statutJuridiqueInconnuEnBaseEstIgnore() {
        when(statutJuridiqueJpaRepository.findCodeByIdIn(anyList())).thenReturn(List.of());

        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"statutJuridique.id\":{\"value\":[99],\"type\":\"list\"}}");

        assertThat(query).isEqualTo("(etatAdministratifUniteLegale:A)");
    }

    @Test
    void nafN1EstEtenduEnCodesN5EntreGuillemets() {
        when(nafN5JpaRepository.findAllCodesByNafN1Codes(List.of("J"))).thenReturn(List.of("62.01Z", "62.02A"));

        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"nafN1.code\":{\"value\":[\"J\"],\"type\":\"list\"}}");

        assertThat(query).isEqualTo(
                "(activitePrincipaleUniteLegale:\"62.01Z\" OR activitePrincipaleUniteLegale:\"62.02A\")"
                        + " AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void typeStructureEnseignementDonneLesCodesJuridiquesDedies() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"typeStructure.id\":{\"value\":6,\"type\":\"int\"}}");

        assertThat(query).contains("categorieJuridiqueUniteLegale:7331")
                .contains("categorieJuridiqueUniteLegale:7383")
                .contains("categorieJuridiqueUniteLegale:7384")
                .endsWith(" AND (etatAdministratifUniteLegale:A)");
    }

    @Test
    void typeStructureAccepteLesChainesEtLesTableaux() {
        String depuisChaine = SireneQueryBuilder.buildLuceneQuery(
                "{\"typeStructure.id\":{\"value\":\"6\"}}");
        String depuisTableau = SireneQueryBuilder.buildLuceneQuery(
                "{\"typeStructure.id\":{\"value\":[6]}}");

        assertThat(depuisChaine).isEqualTo(depuisTableau);
    }

    @Test
    void typeStructureNonParseableEstIgnore() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"typeStructure.id\":{\"value\":\"abc\"}}");

        assertThat(query).isEqualTo("(etatAdministratifUniteLegale:A)");
    }

    @Test
    void filtresVidesOuTechniquesSontIgnores() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"pays.id\":{\"value\":82},"
                        + "\"commune\":{\"value\":\"   \"},"
                        + "\"sansValeur\":{\"type\":\"text\"}}");

        assertThat(query).isEqualTo("(etatAdministratifUniteLegale:A)");
    }

    @Test
    void lesCaracteresSpeciauxLuceneSontEchappes() {
        String query = SireneQueryBuilder.buildLuceneQuery(
                "{\"raisonSociale\":{\"value\":\"AT&T (France)\",\"type\":\"text\"}}");

        // découpage sur les caractères non alphanumériques : AT, T, France
        assertThat(query).isEqualTo(
                "((raisonSociale:AT* AND raisonSociale:T* AND raisonSociale:France*))"
                        + " AND (etatAdministratifUniteLegale:A)");
    }
}
