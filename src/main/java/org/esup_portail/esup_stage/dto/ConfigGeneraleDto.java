package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;
import org.esup_portail.esup_stage.enums.TypeCentreEnum;

public class ConfigGeneraleDto {
    private String codeUniversite;
    private int anneeBasculeJour = 1;
    private int anneeBasculeMois = 9;
    private boolean autoriserConventionsOrphelines = false;
    private TypeCentreEnum typeCentre = null;
    private boolean autoriserCentresBloquerImpressionConvention = false;

    @JsonView(Views.Etu.class)
    private boolean autoriserEtudiantAModifierEntreprise = false;

    private boolean autoriserValidationAutoOrgaAccCreaEtu = false;
    private String ldapFiltreEnseignant = "(|(eduPersonAffiliation=teacher)(eduPersonAffiliation=faculty))";

    @JsonView(Views.Etu.class)
    private boolean autoriserElementPedagogiqueFacultatif = false;

    @JsonView(Views.Etu.class)
    private String validationPedagogiqueLibelle = "validation pédagogique";

    @JsonView(Views.Etu.class)
    private String validationAdministrativeLibelle = "validation administrative";

    public String getCodeUniversite() {
        return codeUniversite;
    }

    public void setCodeUniversite(String codeUniversite) {
        this.codeUniversite = codeUniversite;
    }

    public int getAnneeBasculeJour() {
        return anneeBasculeJour;
    }

    public void setAnneeBasculeJour(int anneeBasculeJour) {
        this.anneeBasculeJour = anneeBasculeJour;
    }

    public int getAnneeBasculeMois() {
        return anneeBasculeMois;
    }

    public void setAnneeBasculeMois(int anneeBasculeMois) {
        this.anneeBasculeMois = anneeBasculeMois;
    }

    public boolean isAutoriserConventionsOrphelines() {
        return autoriserConventionsOrphelines;
    }

    public void setAutoriserConventionsOrphelines(boolean autoriserConventionsOrphelines) {
        this.autoriserConventionsOrphelines = autoriserConventionsOrphelines;
    }

    public TypeCentreEnum getTypeCentre() {
        return typeCentre;
    }

    public void setTypeCentre(TypeCentreEnum typeCentre) {
        this.typeCentre = typeCentre;
    }

    public boolean isAutoriserCentresBloquerImpressionConvention() {
        return autoriserCentresBloquerImpressionConvention;
    }

    public void setAutoriserCentresBloquerImpressionConvention(boolean autoriserCentresBloquerImpressionConvention) {
        this.autoriserCentresBloquerImpressionConvention = autoriserCentresBloquerImpressionConvention;
    }

    public boolean isAutoriserEtudiantAModifierEntreprise() {
        return autoriserEtudiantAModifierEntreprise;
    }

    public void setAutoriserEtudiantAModifierEntreprise(boolean autoriserEtudiantAModifierEntreprise) {
        this.autoriserEtudiantAModifierEntreprise = autoriserEtudiantAModifierEntreprise;
    }

    public boolean isAutoriserValidationAutoOrgaAccCreaEtu() {
        return autoriserValidationAutoOrgaAccCreaEtu;
    }

    public void setAutoriserValidationAutoOrgaAccCreaEtu(boolean autoriserValidationAutoOrgaAccCreaEtu) {
        this.autoriserValidationAutoOrgaAccCreaEtu = autoriserValidationAutoOrgaAccCreaEtu;
    }

    public String getLdapFiltreEnseignant() {
        return ldapFiltreEnseignant;
    }

    public void setLdapFiltreEnseignant(String ldapFiltreEnseignant) {
        this.ldapFiltreEnseignant = ldapFiltreEnseignant;
    }

    public boolean isAutoriserElementPedagogiqueFacultatif() {
        return autoriserElementPedagogiqueFacultatif;
    }

    public void setAutoriserElementPedagogiqueFacultatif(boolean autoriserElementPedagogiqueFacultatif) {
        this.autoriserElementPedagogiqueFacultatif = autoriserElementPedagogiqueFacultatif;
    }

    public String getValidationPedagogiqueLibelle() {
        return validationPedagogiqueLibelle;
    }

    public void setValidationPedagogiqueLibelle(String validationPedagogiqueLibelle) {
        this.validationPedagogiqueLibelle = validationPedagogiqueLibelle;
    }

    public String getValidationAdministrativeLibelle() {
        return validationAdministrativeLibelle;
    }

    public void setValidationAdministrativeLibelle(String validationAdministrativeLibelle) {
        this.validationAdministrativeLibelle = validationAdministrativeLibelle;
    }
}
