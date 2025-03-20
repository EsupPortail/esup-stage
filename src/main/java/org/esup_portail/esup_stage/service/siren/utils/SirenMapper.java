package org.esup_portail.esup_stage.service.siren.utils;

import org.esup_portail.esup_stage.model.Effectif;
import org.esup_portail.esup_stage.model.StatutJuridique;
import org.esup_portail.esup_stage.model.Structure;
import org.esup_portail.esup_stage.service.siren.model.SirenResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SirenMapper {

    public Structure toStructure(SirenResponse.EtablissementSiren etablissement) {
        if (etablissement == null) {
            return null;
        }

        Structure structure = new Structure();
        structure.setNumeroSiret(etablissement.getSiret());
        structure.setRaisonSociale(etablissement.getRaisonSociale());
        structure.setActivitePrincipale(etablissement.getActivitePrincipale());

        // Adresse
        if (etablissement.getAdresse() != null) {
            structure.setVoie(etablissement.getAdresse().getVoie());
            structure.setCommune(etablissement.getAdresse().getCommune());
            structure.setCodePostal(etablissement.getAdresse().getCodePostal());
            structure.setCodeCommune(etablissement.getAdresse().getCodeCommune());
        }

        // Effectif
        if (etablissement.getUniteLegale() != null) {
            structure.setEffectif(new Effectif());
            structure.getEffectif().setLibelle(etablissement.getUniteLegale().getEffectif());
        }

        // Statut juridique
        if (etablissement.getUniteLegale() != null) {
            structure.setStatutJuridique(new StatutJuridique());
            structure.getStatutJuridique().setLibelle(etablissement.getUniteLegale().getStatutJuridique());
        }

        // Valeurs par défaut
        structure.setEstValidee(false);
        structure.setTemEnServStructure("O");
        structure.setTemSiren(true);

        return structure;
    }

    public List<Structure> toStructureList(SirenResponse sirenResponse) {
        return sirenResponse.getEtablissements().stream()
                .map(this::toStructure)
                .collect(Collectors.toList());
    }
}