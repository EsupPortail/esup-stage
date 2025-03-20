package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.AppSignatureEnum;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ConfigGeneraleDto {
    private String codeUniversite;
    private int anneeBasculeJour = 1;
    private int anneeBasculeMois = 9;
    private boolean autoriserConventionsOrphelines = false;
    private TypeCentreEnum typeCentre = null;
    private boolean autoriserCentresBloquerImpressionConvention = false;

    private boolean saisieManuelle = false;

    @JsonView(Views.Etu.class)
    private boolean signatureEnabled = false;

    @JsonView(Views.Etu.class)
    private AppSignatureEnum signatureType;

    @JsonView(Views.Etu.class)
    private boolean autoriserEtudiantAModifierEntreprise = false;

    private boolean autoriserValidationAutoOrgaAccCreaEtu = false;

    @JsonView(Views.Etu.class)
    private boolean autoriserElementPedagogiqueFacultatif = false;

    @JsonView(Views.Etu.class)
    private String validationPedagogiqueLibelle = "validation pédagogique";

    @JsonView(Views.Etu.class)
    private String validationAdministrativeLibelle = "validation administrative";

    @JsonView(Views.Etu.class)
    private String codeCesure;

    @JsonView(Views.Etu.class)
    private boolean autoriserEtudiantACreerEntreprise = false;

    private boolean utiliserMailPersoEtudiant = false;

}
