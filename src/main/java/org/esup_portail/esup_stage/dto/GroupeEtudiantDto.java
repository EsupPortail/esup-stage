package org.esup_portail.esup_stage.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class GroupeEtudiantDto {

    @NotEmpty
    @NotNull
    @Size(max = 100)
    private String nomGroupe;

    @NotNull
    private List<Integer> etudiantIds = new ArrayList<>();

    public String getNomGroupe() {
        return nomGroupe;
    }

    public void setNomGroupe(String nomGroupe) {
        this.nomGroupe = nomGroupe;
    }

    public List<Integer> getEtudiantIds() {
        return etudiantIds;
    }

    public void setEtudiantIds(List<Integer> etudiantIds) {
        this.etudiantIds = etudiantIds;
    }
}