package org.esup_portail.esup_stage.service.sirene.utils;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
public final class SireneGestionAdressePaysEtranger {

    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\b\\d+(-\\d+)?\\b.*");
    private static final Pattern SINGLE_PROPER_NOUN = Pattern.compile("^[A-Z]+$");
    private static final Pattern TYPICAL_CITY_PATTERN = Pattern.compile("^[A-Z]+( [A-Z]+){0,2}$");

    private static final List<String> STREET_KEYWORDS = Arrays.asList(
            "STREET", "ST", "ROAD", "RD", "AVENUE", "AVE", "BOULEVARD", "BLVD",
            "LANE", "LN", "DRIVE", "DR", "COURT", "CT", "PLACE", "PL",
            "SQUARE", "SQ", "TERRACE", "PARKWAY", "PKY", "HIGHWAY", "HWY",
            "WAY", "CIRCLE", "CRESCENT",

            "RUE", "AVENUE", "BOULEVARD", "CHEMIN", "CH", "ROUTE", "IMPASSE", "ALLEE",
            "ALL�E", "VOIE", "PASSAGE", "QUAI",

            "CALLE", "AVENIDA", "VIA", "RUTA", "CAMINO",

            "BUILDING", "BLDG", "TOWER", "TOWERS", "COMPLEX", "ZONE",
            "PARC", "PARK", "PO BOX", "BP", "BOX", "LOT", "KM",
            "CORNER", "QUARTERS", "QUARTER", "INDUSTRIAL", "CENTER", "CENTRE",
            "FLOOR", "SUITE", "UNIT", "APT", "APARTMENT", "WING"
    );

    public static MappingResult map(SirenResponse.EtablissementSiren.AdresseEtablissement adresse) {
        if (adresse == null) {
            return MappingResult.empty();
        }

        String complement = clean(adresse.getComplementAdresseEtablissement());
        String voieBrute = clean(adresse.getVoie());
        String numeroVoie = clean(adresse.getNumeroVoie());
        String indiceRepetition = clean(adresse.getIndiceRepetitionEtablissement());
        String typeVoie = clean(adresse.getTypeVoie());
        String distributionSpeciale = clean(adresse.getDistributionSpecialeEtablissement());
        String communeFrance = clean(adresse.getCommune());
        String communeEtranger = clean(adresse.getLibelleCommuneEtrangerEtablissement());
        String codePostal = clean(adresse.getCodePostal());
        String codeCommune = clean(adresse.getCodeCommune());
        String codePaysEtranger = clean(adresse.getCodePaysEtrangerEtablissement());
        String libellePaysEtranger = clean(adresse.getLibellePaysEtrangerEtablissement());
        String libCedex = clean(adresse.getLibelleCedexEtablissement());

        boolean adresseEtrangere = notBlank(codePaysEtranger) || notBlank(libellePaysEtranger) || notBlank(communeEtranger);

        String voieNominale = joinNonBlank(
                complement,
                joinNonBlank(numeroVoie, indiceRepetition, typeVoie, voieBrute),
                distributionSpeciale
        );
        String communeNominale = adresseEtrangere ? communeEtranger : communeFrance;

        if (!adresseEtrangere) {
            return new MappingResult(
                    voieNominale,
                    communeNominale,
                    codePostal,
                    codeCommune,
                    libCedex,
                    codePaysEtranger,
                    libellePaysEtranger,
                    false,
                    false,
                    MappingDecision.NOMINAL
            );
        }

        int communeStreetScore = calculateStreetScore(communeEtranger);
        int communeCityScore = calculateCityScore(communeEtranger);
        int rawVoieStreetScore = calculateStreetScore(voieBrute);
        int rawVoieCityScore = calculateCityScore(voieBrute);
        int nominalVoieStreetScore = calculateStreetScore(voieNominale);
        int nominalVoieCityScore = calculateCityScore(voieNominale);

        boolean shouldSwap = notBlank(communeEtranger)
                && notBlank(voieBrute)
                && communeStreetScore > communeCityScore
                && rawVoieCityScore > rawVoieStreetScore;

        boolean clearNominal = notBlank(voieNominale)
                && notBlank(communeNominale)
                && nominalVoieStreetScore > nominalVoieCityScore
                && communeCityScore > communeStreetScore;

        String voie = voieNominale;
        String commune = communeNominale;
        MappingDecision decision;

        if (shouldSwap) {
            voie = joinNonBlank(complement, communeEtranger, distributionSpeciale);
            commune = voieBrute;
            decision = MappingDecision.PERMUTED;
        } else if (clearNominal) {
            decision = MappingDecision.NOMINAL;
        } else {
            decision = MappingDecision.AMBIGUOUS;
        }

        return new MappingResult(
                voie,
                commune,
                codePostal,
                codeCommune,
                libCedex,
                codePaysEtranger,
                libellePaysEtranger,
                true,
                shouldSwap,
                decision
        );
    }

    static int calculateStreetScore(String value) {
        if (!notBlank(value)) {
            return 0;
        }

        String normalized = normalize(value);
        int score = 0;

        if (NUMBER_PATTERN.matcher(normalized).matches()) {
            score += 3;
        }

        if (containsStreetKeyword(normalized)) {
            score += 2;
        }

        if (normalized.split("\\s+").length > 4) {
            score += 1;
        }

        if (SINGLE_PROPER_NOUN.matcher(normalized).matches()) {
            score -= 2;
        }

        if (normalized.split("\\s+").length == 1 && !HAS_DIGIT.matcher(normalized).matches()) {
            score -= 1;
        }

        return score;
    }

    static int calculateCityScore(String value) {
        if (!notBlank(value)) {
            return 0;
        }

        String normalized = normalize(value);
        int score = 0;

        if (SINGLE_PROPER_NOUN.matcher(normalized).matches()) {
            score += 3;
        }

        int wordCount = normalized.split("\\s+").length;
        if (wordCount <= 2) {
            score += 2;
        }

        if (TYPICAL_CITY_PATTERN.matcher(normalized).matches()) {
            score += 1;
        }

        if (HAS_DIGIT.matcher(normalized).matches()) {
            score -= 3;
        }

        if (containsStreetKeyword(normalized)) {
            score -= 2;
        }

        if (wordCount > 4) {
            score -= 1;
        }

        return score;
    }

    private static boolean containsStreetKeyword(String normalized) {
        return STREET_KEYWORDS.stream().anyMatch(keyword -> containsKeyword(normalized, keyword));
    }

    private static boolean containsKeyword(String normalized, String keyword) {
        String candidate = " " + normalized + " ";
        String token = " " + keyword.toUpperCase(Locale.ROOT) + " ";
        return candidate.contains(token);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean notBlank(String value) {
        return clean(value) != null;
    }

    private static String joinNonBlank(String... values) {
        String result = Arrays.stream(values)
                .map(SireneGestionAdressePaysEtranger::clean)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));

        return result.isBlank() ? null : result;
    }

    private static String normalize(String value) {
        return value.toUpperCase(Locale.ROOT)
                .replace('-', ' ')
                .replace(',', ' ')
                .replace('/', ' ')
                .replace('(', ' ')
                .replace(')', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }

    public enum MappingDecision {
        NOMINAL,
        PERMUTED,
        AMBIGUOUS
    }

    @Data
    public static class MappingResult {
        private final String voie;
        private final String commune;
        private final String codePostal;
        private final String codeCommune;
        private final String libCedex;
        private final String codePaysEtranger;
        private final String libellePaysEtranger;
        private final boolean adresseEtrangere;
        private final boolean permutationEffectuee;
        private final MappingDecision decision;

        public MappingResult(String voie, String commune, String codePostal, String codeCommune,
                             String libCedex, String codePaysEtranger, String libellePaysEtranger,
                             boolean adresseEtrangere, boolean permutationEffectuee, MappingDecision decision) {
            this.voie = voie;
            this.commune = commune;
            this.codePostal = codePostal;
            this.codeCommune = codeCommune;
            this.libCedex = libCedex;
            this.codePaysEtranger = codePaysEtranger;
            this.libellePaysEtranger = libellePaysEtranger;
            this.adresseEtrangere = adresseEtrangere;
            this.permutationEffectuee = permutationEffectuee;
            this.decision = decision;
        }

        public static MappingResult empty() {
            return new MappingResult(null, null, null, null, null, null, null, false, false, MappingDecision.NOMINAL);
        }
    }
}
