package org.esup_portail.esup_stage.dto;

import org.esup_portail.esup_stage.service.apogee.model.EtudiantDiplomeEtapeResponse;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class GroupeEtudiantDto {

    @NotEmpty
    @NotNull
    @Size(max = 100)
    private String codeGroupe;

    @NotEmpty
    @NotNull
    @Size(max = 100)
    private String nomGroupe;

    @NotNull
    private List<Integer> etudiantRemovedIds = new ArrayList<>();

    @NotNull
    private List<EtudiantDiplomeEtapeResponse> etudiantAdded = new ArrayList<>();

    public String getCodeGroupe() {
        return codeGroupe;
    }

    public void setCodeGroupe(String codeGroupe) {
        this.codeGroupe = codeGroupe;
    }

    public String getNomGroupe() {
        return nomGroupe;
    }

    public void setNomGroupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
    }

    public List<Integer> getEtudiantRemovedIds() {
        return etudiantRemovedIds;
    }

    public void setEtudiantRemovedIds(List<Integer> etudiantRemovedIds) {
        this.etudiantRemovedIds = etudiantRemovedIds;
    }

    public List<EtudiantDiplomeEtapeResponse> getEtudiantAdded() {
        return etudiantAdded;
    }

    public void setEtudiantAdded(List<EtudiantDiplomeEtapeResponse> etudiantAdded) {
        this.etudiantAdded = etudiantAdded;
    }
}