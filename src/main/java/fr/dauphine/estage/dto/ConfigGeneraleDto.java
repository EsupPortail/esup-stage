package fr.dauphine.estage.dto;

import fr.dauphine.estage.enums.TypeCentreEnum;

public class ConfigGeneraleDto {
    private int anneeBasculeJour = 13;
    private int anneeBasculeMois = 12;
    private boolean autoriserConventionsOrphelines = false;
    private TypeCentreEnum typeCentre = null;
    private boolean autoriserCentresBloquerImpressionConvention = false;
    private boolean autoriserEtudiantAModifierEntreprise = false;
    private boolean autoriserValidationAutoOrgaAccCreaEtu = false;

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
}
