package org.esup_portail.esup_stage.service.siren.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SirenResponse {

    @JsonProperty("etablissements")
    private List<EtablissementSiren> etablissements;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class EtablissementSiren {

        @JsonProperty("siren")
        private String siren;

        @JsonProperty("siret")
        private String siret;

        @JsonProperty("activitePrincipaleEtablissement")
        private String activitePrincipale;

        @JsonProperty("denominationUsuelleEtablissement")
        private String raisonSociale;

        @JsonProperty("uniteLegale")
        private UniteLegale uniteLegale;

        @JsonProperty("adresseEtablissement")
        private AdresseEtablissement adresse;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class UniteLegale {

            @JsonProperty("denominationUniteLegale")
            private String denominationUniteLegale;

            @JsonProperty("categorieJuridiqueUniteLegale")
            private String statutJuridique;

            @JsonProperty("trancheEffectifsUniteLegale")
            private String effectif;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class AdresseEtablissement {

            @JsonProperty("libelleVoieEtablissement")
            private String voie;

            @JsonProperty("libelleCommuneEtablissement")
            private String commune;

            @JsonProperty("codePostalEtablissement")
            private String codePostal;

            @JsonProperty("codeCommuneEtablissement")
            private String codeCommune;
        }
    }
}