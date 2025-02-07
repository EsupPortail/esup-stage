package org.esup_portail.esup_stage.service.apogee.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiplomeEtape {
    private String codeDiplome;
    private String versionDiplome;
    private String libDiplome;
    private List<EtapeV2Apogee> listeEtapes;

    public String getCodeDiplome() {
        return codeDiplome;
    }

    public void setCodeDiplome(String codeDiplome) {
        this.codeDiplome = codeDiplome;
    }

    public String getVersionDiplome() {
        return versionDiplome;
    }

    public void setVersionDiplome(String versionDiplome) {
        this.versionDiplome = versionDiplome;
    }

    public String getLibDiplome() {
        return libDiplome;
    }

    public void setLibDiplome(String libDiplome) {
        this.libDiplome = libDiplome;
    }

    public List<EtapeV2Apogee> getListeEtapes() {
        return listeEtapes;
    }

    public void setListeEtapes(List<EtapeV2Apogee> listeEtapes) {
        this.listeEtapes = listeEtapes;
    }
}
