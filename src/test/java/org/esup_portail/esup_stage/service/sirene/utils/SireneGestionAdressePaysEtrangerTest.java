package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.esup_portail.esup_stage.service.sirene.utils.SireneGestionAdressePaysEtranger.MappingDecision;
import org.esup_portail.esup_stage.service.sirene.utils.SireneGestionAdressePaysEtranger.MappingResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SireneGestionAdressePaysEtrangerTest {

    private SirenResponse.EtablissementSiren.AdresseEtablissement adresse() {
        return new SirenResponse.EtablissementSiren.AdresseEtablissement();
    }

    @Test
    void adresseNulleDonneUnResultatVide() {
        MappingResult result = SireneGestionAdressePaysEtranger.map(null);

        assertThat(result.getVoie()).isNull();
        assertThat(result.getCommune()).isNull();
        assertThat(result.isAdresseEtrangere()).isFalse();
        assertThat(result.getDecision()).isEqualTo(MappingDecision.NOMINAL);
    }

    @Test
    void adresseFrancaiseEstMappeeSansPermutation() {
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse = adresse();
        adresse.setNumeroVoie("12");
        adresse.setTypeVoie("RUE");
        adresse.setVoie("DE LA PAIX");
        adresse.setCommune("NANCY");
        adresse.setCodePostal("54000");
        adresse.setCodeCommune("54395");

        MappingResult result = SireneGestionAdressePaysEtranger.map(adresse);

        assertThat(result.getVoie()).isEqualTo("12 RUE DE LA PAIX");
        assertThat(result.getCommune()).isEqualTo("NANCY");
        assertThat(result.getCodePostal()).isEqualTo("54000");
        assertThat(result.getCodeCommune()).isEqualTo("54395");
        assertThat(result.isAdresseEtrangere()).isFalse();
        assertThat(result.isPermutationEffectuee()).isFalse();
        assertThat(result.getDecision()).isEqualTo(MappingDecision.NOMINAL);
    }

    @Test
    void adresseEtrangereBienRenseigneeResteNominale() {
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse = adresse();
        adresse.setVoie("10 DOWNING STREET");
        adresse.setLibelleCommuneEtrangerEtablissement("LONDON");
        adresse.setLibellePaysEtrangerEtablissement("ROYAUME-UNI");
        adresse.setCodePaysEtrangerEtablissement("99132");

        MappingResult result = SireneGestionAdressePaysEtranger.map(adresse);

        assertThat(result.isAdresseEtrangere()).isTrue();
        assertThat(result.getCommune()).isEqualTo("LONDON");
        assertThat(result.getVoie()).isEqualTo("10 DOWNING STREET");
        assertThat(result.isPermutationEffectuee()).isFalse();
        assertThat(result.getDecision()).isEqualTo(MappingDecision.NOMINAL);
        assertThat(result.getLibellePaysEtranger()).isEqualTo("ROYAUME-UNI");
    }

    @Test
    void adresseEtrangereInverseeEstPermutee() {
        // cas réel : la voie contient la ville et la "commune étrangère" contient la rue
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse = adresse();
        adresse.setVoie("BERLIN");
        adresse.setLibelleCommuneEtrangerEtablissement("15 HAUPTSTRASSE BUILDING 2");
        adresse.setLibellePaysEtrangerEtablissement("ALLEMAGNE");

        MappingResult result = SireneGestionAdressePaysEtranger.map(adresse);

        assertThat(result.isAdresseEtrangere()).isTrue();
        assertThat(result.isPermutationEffectuee()).isTrue();
        assertThat(result.getDecision()).isEqualTo(MappingDecision.PERMUTED);
        assertThat(result.getVoie()).isEqualTo("15 HAUPTSTRASSE BUILDING 2");
        assertThat(result.getCommune()).isEqualTo("BERLIN");
    }

    @Test
    void adresseEtrangereIncertaineEstMarqueeAmbigue() {
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse = adresse();
        adresse.setLibelleCommuneEtrangerEtablissement("SINGAPORE");
        adresse.setLibellePaysEtrangerEtablissement("SINGAPOUR");
        // pas de voie du tout : impossible de trancher

        MappingResult result = SireneGestionAdressePaysEtranger.map(adresse);

        assertThat(result.isAdresseEtrangere()).isTrue();
        assertThat(result.isPermutationEffectuee()).isFalse();
        assertThat(result.getDecision()).isEqualTo(MappingDecision.AMBIGUOUS);
        assertThat(result.getCommune()).isEqualTo("SINGAPORE");
    }

    @Test
    void scoreRueValoriseNumerosEtMotsClefs() {
        assertThat(SireneGestionAdressePaysEtranger.calculateStreetScore("12 RUE DE LA GARE"))
                .isGreaterThan(SireneGestionAdressePaysEtranger.calculateStreetScore("PARIS"));
        assertThat(SireneGestionAdressePaysEtranger.calculateStreetScore("")).isZero();
        assertThat(SireneGestionAdressePaysEtranger.calculateStreetScore(null)).isZero();
    }

    @Test
    void scoreVilleValoriseLesNomsSimples() {
        assertThat(SireneGestionAdressePaysEtranger.calculateCityScore("MUNICH"))
                .isGreaterThan(SireneGestionAdressePaysEtranger.calculateCityScore("25 MAIN STREET SUITE 12"));
        assertThat(SireneGestionAdressePaysEtranger.calculateCityScore(null)).isZero();
    }

    @Test
    void lesChampsComplementairesSontConcatenesDansLaVoie() {
        SirenResponse.EtablissementSiren.AdresseEtablissement adresse = adresse();
        adresse.setComplementAdresseEtablissement("BATIMENT B");
        adresse.setNumeroVoie("3");
        adresse.setIndiceRepetitionEtablissement("BIS");
        adresse.setTypeVoie("AVENUE");
        adresse.setVoie("FOCH");
        adresse.setDistributionSpecialeEtablissement("BP 12");
        adresse.setCommune("METZ");

        MappingResult result = SireneGestionAdressePaysEtranger.map(adresse);

        assertThat(result.getVoie()).isEqualTo("BATIMENT B 3 BIS AVENUE FOCH BP 12");
    }
}
