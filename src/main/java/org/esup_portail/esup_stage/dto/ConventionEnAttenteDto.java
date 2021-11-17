package org.esup_portail.esup_stage.dto;

public class ConventionEnAttenteDto {
    private long nbEnAttenteValidPedadogique;
    private long nbEnAttenteValidAdministratif;

    public ConventionEnAttenteDto() { }

    public ConventionEnAttenteDto(long nbEnAttenteValidPedadogique, long nbEnAttenteValidAdministratif) {
        this.nbEnAttenteValidPedadogique = nbEnAttenteValidPedadogique;
        this.nbEnAttenteValidAdministratif = nbEnAttenteValidAdministratif;
    }

    public long getNbEnAttenteValidPedadogique() {
        return nbEnAttenteValidPedadogique;
    }

    public void setNbEnAttenteValidPedadogique(long nbEnAttenteValidPedadogique) {
        this.nbEnAttenteValidPedadogique = nbEnAttenteValidPedadogique;
    }

    public long getNbEnAttenteValidAdministratif() {
        return nbEnAttenteValidAdministratif;
    }

    public void setNbEnAttenteValidAdministratif(long nbEnAttenteValidAdministratif) {
        this.nbEnAttenteValidAdministratif = nbEnAttenteValidAdministratif;
    }
}
