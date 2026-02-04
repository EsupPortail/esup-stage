package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.model.Effectif;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.TypeStructure;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SireneMapper {

    @Autowired
    private StatutJuridiqueJpaRepository statutJuridiqueJpaRepository;

    @Autowired
    private NafN5JpaRepository nafN5JpaRepository;

    @Autowired
    private PaysJpaRepository paysJpaRepository;

    @Autowired
    private TypeStructureJpaRepository typeStructureJpaRepository;

    @Autowired
    private EffectifJpaRepository effectifJpaRepository;

    public Structure toStructure(SirenResponse.EtablissementSiren etablissement) {
        if (etablissement == null) {
            return null;
        }

        Structure structure = new Structure();
        structure.setNumeroSiret(etablissement.getSiret());
        structure.setRaisonSociale(determinerRaisonSociale(etablissement));

        // Adresse
        if (etablissement.getAdresse() != null) {
            String voie = cleanConcat(etablissement.getAdresse().getNumeroVoie(),
                    etablissement.getAdresse().getTypeVoie(),
                    etablissement.getAdresse().getVoie());
            structure.setVoie(voie);
            structure.setCommune(clean(etablissement.getAdresse().getCommune()));
            structure.setCodePostal(clean(etablissement.getAdresse().getCodePostal()));
            structure.setCodeCommune(clean(etablissement.getAdresse().getCodeCommune()));
        }

        // NAF
        String naf25 = null;

        if (notBlank(etablissement.getNaf_n5())) {
            naf25 = etablissement.getNaf_n5();
        }
        else if (etablissement.getUniteLegale() != null
                && notBlank(etablissement.getUniteLegale().getActivitePrincipaleNAF25UniteLegale())) {
            naf25 = etablissement.getUniteLegale().getActivitePrincipaleNAF25UniteLegale();
        }

        if (notBlank(naf25) && !naf25.equalsIgnoreCase("[ND]")) {
            structure.setNafN5(nafN5JpaRepository.findByCode(naf25));
            if (structure.getNafN5() != null) {
                structure.setActivitePrincipale(structure.getNafN5().getLibelle());
            }
        }


        // Statut juridique + type structure
        if (etablissement.getUniteLegale() != null && etablissement.getUniteLegale().getStatutJuridique() != null) {
            String codeJuridique = etablissement.getUniteLegale().getStatutJuridique().substring(0,2);// On ne garde que les 2 premiers caractères (niveau 2)
            StatutJuridique sj = statutJuridiqueJpaRepository.findByCode(codeJuridique);
            if (sj == null) sj = statutJuridiqueJpaRepository.findByLibelle("Autre");
            structure.setStatutJuridique(sj);
            structure.setTypeStructure(determinerTypeStructure(codeJuridique));
        } else {
            structure.setStatutJuridique(statutJuridiqueJpaRepository.findByLibelle("Autre"));
            structure.setTypeStructure(typeStructureJpaRepository.findById(3));
        }

        // Effectif
        Effectif effectif = determinerEffectif(etablissement.getUniteLegale().getTrancheEffectifsUniteLegale()) ;
        if (effectif != null) structure.setEffectif(effectif);

        // Valeurs par défaut
        structure.setPays(paysJpaRepository.findById(82)); // France
        structure.setEstValidee(false);
        structure.setTemEnServStructure(true);
        structure.setTemSiren(true);

        return structure;
    }

    public List<Structure> toStructureList(SirenResponse sirenResponse) {
        return sirenResponse.getEtablissements().stream()
                .map(this::toStructure)
                .collect(Collectors.toList());
    }

    private TypeStructure determinerTypeStructure(String codeJuridique) {
        if (codeJuridique == null || codeJuridique.isBlank()) {
            return typeStructureJpaRepository.findById(3);
        }

        String codeNiveau2 = codeJuridique.length() >= 2
            ? codeJuridique.substring(0, 2)
            : codeJuridique;

        StatutJuridique statutJuridique = statutJuridiqueJpaRepository.findByCode(codeNiveau2);

        if (statutJuridique != null && statutJuridique.getTypeStructure() != null) {
            return statutJuridique.getTypeStructure();
        }

        StatutJuridique autre = statutJuridiqueJpaRepository.findByLibelle("Autre");
        if (autre != null && autre.getTypeStructure() != null) {
            return autre.getTypeStructure();
        }

        return typeStructureJpaRepository.findById(3);
    }

    /**
     * Bridge code SIRENE -> tranche Effectif (id en base).
     * Retourne l'entité Effectif correspondante, ou null si code inconnu.
     */
    private Effectif determinerEffectif(String codeSirene) {
        if (codeSirene == null || codeSirene.isBlank()) {
            return null;
        }

        String code = codeSirene.trim();

        Integer effectifId = switch (code) {
            // 0 salarié
            case "NN", "00" -> 1;

            // 1 à 9
            case "01", "02", "03" -> 2;

            // 10 à 49
            case "11", "12" -> 3;

            // 50 à 199
            case "21", "22" -> 4;

            // 200 à 999
            case "31", "32", "41" -> 5;

            // 1000 et +
            case "42", "51", "52", "53" -> 6;

            default -> null; // code non prévu : ne pas écraser l’existant
        };

        if (effectifId == null) {
            return null;
        }

        return effectifJpaRepository.findById(effectifId).orElse(null);
    }

    public Structure updateStructure(SirenResponse sirenResponse, Structure structure) {
        if (sirenResponse == null || structure == null) {
            throw new IllegalArgumentException("Les paramètres sirenResponse et structure ne doivent pas être null");
        }

        SirenResponse.EtablissementSiren etablissement = sirenResponse.getEtablissement();
        if (etablissement == null) {
            throw new IllegalArgumentException("Aucun établissement trouvé dans la réponse Siren");
        }

        // --- Raison sociale : MAJ seulement si non vide, sinon garder l’existant
        String raisonSociale = determinerRaisonSociale(etablissement);
        if (notBlank(raisonSociale)) {
            structure.setRaisonSociale(raisonSociale);
        }


        // --- Adresse : ne patcher que les champs présents (ne jamais vider)
        if (etablissement.getAdresse() != null) {
            String numeroVoie = etablissement.getAdresse().getNumeroVoie();
            String typeVoie   = etablissement.getAdresse().getTypeVoie();
            String voie       = etablissement.getAdresse().getVoie();

            String voieConcat = ((notBlank(numeroVoie) ? numeroVoie + " " : "") +
                    (notBlank(typeVoie)   ? typeVoie   + " " : "") +
                    (notBlank(voie)       ? voie             : "")).trim();

            if (notBlank(voieConcat)) {
                structure.setVoie(voieConcat);
            }

            String commune = etablissement.getAdresse().getCommune();
            if (notBlank(commune)) {
                structure.setCommune(commune);
            }

            String codePostal = etablissement.getAdresse().getCodePostal();
            if (notBlank(codePostal)) {
                structure.setCodePostal(codePostal);
            }

            String codeCommune = etablissement.getAdresse().getCodeCommune();
            if (notBlank(codeCommune)) {
                structure.setCodeCommune(codeCommune);
            }
        }

        // --- NAF : ne remplacer que si on résout le code en base
        if (etablissement.getUniteLegale() != null && notBlank(etablissement.getNaf_n5())) {
            var naf = nafN5JpaRepository.findByCode(etablissement.getNaf_n5());
            if (naf != null) {
                structure.setNafN5(naf);
                if (notBlank(naf.getLibelle())) {
                    structure.setActivitePrincipale(naf.getLibelle());
                }
            }
            // sinon : on ne touche pas à l’ancienne valeur (pas d’écrasement à null)
        }

        // --- Effectif : via determinerEffectif, garder l’existant si non résolu
        String codeEffectif = (etablissement.getUniteLegale() != null)
                ? etablissement.getUniteLegale().getTrancheEffectifsUniteLegale()
                : null;
        var effectifMappe = determinerEffectif(codeEffectif);
        if (effectifMappe != null) {
            structure.setEffectif(effectifMappe);
        }

        // --- Statut juridique : fallback "Autre" si non trouvé, sinon garder l’existant ?
        String codeJuridique = (etablissement.getUniteLegale() != null)
                ? etablissement.getUniteLegale().getStatutJuridique()
                : null;

        StatutJuridique sj = null;
        if (notBlank(codeJuridique)) {
            // Extraire le code niveau 2 (2 premiers caractères)
            String codeNiveau2 = codeJuridique.length() >= 2
                ? codeJuridique.substring(0, 2)
                : codeJuridique;
            sj = statutJuridiqueJpaRepository.findByCode(codeNiveau2);
        }
        // On veut explicitement un fallback "Autre" si rien n'est trouvé :
        if (sj == null) {
            sj = statutJuridiqueJpaRepository.findByLibelle("Autre");
        }
        structure.setStatutJuridique(sj);

        // Type structure : recalcul si code exploitable, sinon garder l’existant (et défaut si null)
        if (notBlank(codeJuridique)) {
            structure.setTypeStructure(determinerTypeStructure(codeJuridique));
        } else if (structure.getTypeStructure() == null) {
            structure.setTypeStructure(typeStructureJpaRepository.findById(3)); // défaut : entreprise privée
        }

        return structure;
    }


    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String clean(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty() || v.equalsIgnoreCase("null")) return null;
        return v;
    }

    private String cleanConcat(String... parts) {
        return Arrays.stream(parts)
                .map(this::clean)
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "));
    }

    /**
     * Détermine la raison sociale selon l'ordre de priorité :
     * 1. Enseigne de la période active (dateFin == null)
     * 2. Dénomination usuelle de l'établissement (période active)
     * 3. Dénomination usuelle de l'unité légale
     * 4. Dénomination légale de l'unité légale
     * 5. Identité personne physique (civilité + nom + prénom usuel)
     * @param etablissement L'établissement renvoyé par l'api SIREN
     * @return La raison sociale déterminée, ou null si aucune info disponible
     */
    private String determinerRaisonSociale(SirenResponse.EtablissementSiren etablissement) {

        String resultat = null;

        // 1 + 2 : infos de la période active (dateFin == null)
        if (etablissement.getPeriodesEtablissement() != null && !etablissement.getPeriodesEtablissement().isEmpty()) {

            var periodeActiveOpt = etablissement.getPeriodesEtablissement().stream()
                    .filter(p -> p.getDateFin() == null)
                    .findFirst();

            if (periodeActiveOpt.isPresent()) {
                var periodeActive = periodeActiveOpt.get();

                if (notBlank(periodeActive.getEnseigne1Etablissement())) {
                    resultat = periodeActive.getEnseigne1Etablissement();
                } else if (notBlank(periodeActive.getDenominationUsuelleEtablissement())) {
                    resultat = periodeActive.getDenominationUsuelleEtablissement();
                }
            }
        }

        // 3 + 4 + 5 : unité légale
        if (resultat == null && etablissement.getUniteLegale() != null) {

            var ul = etablissement.getUniteLegale();

            // 3
            if (notBlank(ul.getDenominationUsuelle1UniteLegale())) {
                resultat = ul.getDenominationUsuelle1UniteLegale();
            }
            // 4
            else if (notBlank(ul.getDenominationUniteLegale())) {
                resultat = ul.getDenominationUniteLegale();
            }
            // 5 : personne physique
            else {
                String nom = ul.getNomUniteLegale();
                String prenom = ul.getPrenomUsuelUniteLegale();

                if (notBlank(nom) && notBlank(prenom)) {
                    String civilite = "";
                    if ("F".equalsIgnoreCase(ul.getSexeUniteLegale())) {
                        civilite = "MADAME ";
                    } else if ("M".equalsIgnoreCase(ul.getSexeUniteLegale())) {
                        civilite = "MONSIEUR ";
                    }

                    resultat = (civilite + prenom + " " + nom).trim();
                }
            }
        }

        // --- Post-traitement ND
        if (resultat != null && resultat.equalsIgnoreCase("[ND]")) {
            return "[Non diffusé]";
        }

        return resultat;
    }
}