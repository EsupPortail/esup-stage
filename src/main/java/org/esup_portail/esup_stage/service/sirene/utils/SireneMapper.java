package org.esup_portail.esup_stage.service.sirene.utils;

import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.model.TypeStructure;
import org.esup_portail.esup_stage.repository.*;
import org.esup_portail.esup_stage.service.sirene.model.SirenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
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

    public Structure toStructure(SirenResponse.EtablissementSiren etablissement) {
        if (etablissement == null) {
            return null;
        }

        Structure structure = new Structure();
        structure.setNumeroSiret(etablissement.getSiret());
        structure.setRaisonSociale(etablissement.getUniteLegale().getDenominationUniteLegale());

        // Adresse
        if (etablissement.getAdresse() != null) {
            structure.setVoie(etablissement.getAdresse().getNumeroVoie() + " " + etablissement.getAdresse().getTypeVoie() + " " + etablissement.getAdresse().getVoie());
            structure.setCommune(etablissement.getAdresse().getCommune());            structure.setCommune(etablissement.getAdresse().getCommune());
            structure.setCodePostal(etablissement.getAdresse().getCodePostal());
            structure.setCodeCommune(etablissement.getAdresse().getCodeCommune());
        }

        // NAF
        if(etablissement.getUniteLegale().getNaf_n5() != null) {
            structure.setNafN5(nafN5JpaRepository.findByCode(etablissement.getUniteLegale().getNaf_n5()));
            if(structure.getNafN5() != null){
                structure.setActivitePrincipale(structure.getNafN5().getLibelle());
            }
        }

        // Statut juridique
        if (etablissement.getUniteLegale().getStatutJuridique() != null) {
            String codeJuridique = etablissement.getUniteLegale().getStatutJuridique();
            structure.setStatutJuridique(statutJuridiqueJpaRepository.findByCode(codeJuridique));
            structure.setTypeStructure(determinerTypeStructure(codeJuridique));
        } else {
            structure.setStatutJuridique(statutJuridiqueJpaRepository.findByLibelle("Autre"));
            structure.setTypeStructure(typeStructureJpaRepository.findByid(3));
        }

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

    // Méthode pour déterminer le type de structure en fonction du code juridique
    private TypeStructure determinerTypeStructure(String codeJuridique) {
        // Code niveau 1 : premier chiffre du code juridique
        String niveauUn = codeJuridique.substring(0, 1);
        // Code niveau 2 : deux premiers chiffres si disponible
        String niveauDeux = codeJuridique.length() >= 2 ? codeJuridique.substring(0, 2) : "";
        // Code niveau 3 : code complet à 4 chiffres si disponible

        int typeStructureId;

        // Administration (code 1)
        if (niveauUn.equals("7") || // Personne morale et organisme soumis au droit administratif
                niveauUn.equals("4") || // Personne morale de droit public soumise au droit commercial
                niveauUn.equals("8") || // Organisme privé spécialisé
                niveauDeux.equals("32") || // Personne morale de droit étranger, non immatriculée au RCS
                niveauDeux.equals("71") || // Administration de l'état
                niveauDeux.equals("72") || // Collectivité territoriale
                niveauDeux.equals("73") || // Établissement public administratif
                niveauDeux.equals("74")) { // Autre personne morale de droit public administratif
            typeStructureId = 1; // Administration
        }

        // Association (code 2)
        else if (niveauUn.equals("9") || // Groupement de droit privé
                niveauDeux.equals("92") || // Association loi 1901 ou assimilé
                niveauDeux.equals("93") || // Fondation
                niveauDeux.equals("62") || // Groupement d'intérêt économique
                niveauDeux.equals("63")) { // Société coopérative agricole
            typeStructureId = 2; // Association
        }

        // Entreprise privée (code 3)
        else if (niveauUn.equals("2") || // Groupement de droit privé non doté de la personnalité morale
                niveauUn.equals("3") || // Personne morale de droit étranger
                niveauUn.equals("5") || // Société commerciale
                niveauUn.equals("6") || // Autre personne morale immatriculée au RCS
                niveauDeux.equals("31")) { // Personne morale de droit étranger, immatriculée au RCS
            typeStructureId = 3; // Entreprise privée
        }

        // Entreprise publique / SEM (code 4)
        else if (niveauDeux.equals("41") || // Établissement public national à caractère industriel ou commercial
                niveauDeux.equals("55") || // Société anonyme à conseil d'administration
                niveauDeux.equals("56") || // Société anonyme à directoire
                niveauDeux.equals("57") || // Société par actions simplifiée
                codeJuridique.contains("5515") || // SA d'économie mixte à conseil d'administration
                codeJuridique.contains("5615")) { // SA d'économie mixte à directoire
            typeStructureId = 4; // Entreprise publique / SEM
        }

        // Mutuelle Coopérative (code 5)
        else if (niveauDeux.equals("51") || // Société coopérative commerciale particulière
                niveauDeux.equals("65") || // Société civile
                niveauDeux.equals("81") || // Organisme gérant un régime de protection sociale à adhésion obligatoire
                niveauDeux.equals("82") || // Organisme mutualiste
                niveauDeux.equals("83") || // Comité d'entreprise
                niveauDeux.equals("84") || // Organisme professionnel
                niveauDeux.equals("85")) { // Organisme de retraite à adhésion non obligatoire
            typeStructureId = 5; // Mutuelle Coopérative
        }

        // Etablissement d'enseignement (code 7)
        else if (codeJuridique.contains("7331") || // Établissement public local d'enseignement
                codeJuridique.contains("7383") || // Établissement public national à caractère scientifique culturel et professionnel
                codeJuridique.contains("7384")) { // Autre établissement public national d'enseignement
            typeStructureId = 7; // Etablissement d'enseignement
        }

        // Par défaut: Entreprise privée
        else {
            typeStructureId = 3;
        }

        // Retourne l'objet TypeStructure au lieu de l'ID
        return typeStructureJpaRepository.findById(typeStructureId);
    }

    public Structure updateStructure(SirenResponse sirenResponse, Structure structure) {
        if (sirenResponse == null || structure == null) {
            throw new IllegalArgumentException("Les paramètres sirenResponse et structure ne doivent pas être null");
        }

        SirenResponse.EtablissementSiren etablissement = sirenResponse.getEtablissement();
        if (etablissement == null) {
            throw new IllegalArgumentException("Aucun établissement trouvé dans la réponse Siren");
        }

        structure.setRaisonSociale(etablissement.getUniteLegale().getDenominationUniteLegale());

        if (etablissement.getAdresse() != null) {
            structure.setVoie(etablissement.getAdresse().getNumeroVoie() + " " + etablissement.getAdresse().getTypeVoie() + " " + etablissement.getAdresse().getVoie());
            structure.setCommune(etablissement.getAdresse().getCommune());
            structure.setCodePostal(etablissement.getAdresse().getCodePostal());
            structure.setCodeCommune(etablissement.getAdresse().getCodeCommune());
        }

        if (etablissement.getUniteLegale().getNaf_n5() != null) {
            structure.setNafN5(nafN5JpaRepository.findByCode(etablissement.getUniteLegale().getNaf_n5()));
            if (structure.getNafN5() != null) {
                structure.setActivitePrincipale(structure.getNafN5().getLibelle());
            }
        }

        if (etablissement.getUniteLegale().getStatutJuridique() != null) {
            String codeJuridique = etablissement.getUniteLegale().getStatutJuridique();
            structure.setStatutJuridique(statutJuridiqueJpaRepository.findByCode(codeJuridique));
            structure.setTypeStructure(determinerTypeStructure(codeJuridique));
        }

        return structure;
    }
}