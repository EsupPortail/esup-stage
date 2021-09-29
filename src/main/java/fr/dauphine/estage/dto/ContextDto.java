package fr.dauphine.estage.dto;

import fr.dauphine.estage.model.Utilisateur;

public class ContextDto {

    private Utilisateur utilisateur;
    private String authMode;

    public ContextDto() {
    }

    public ContextDto(Utilisateur utilisateur, String authMode) {
        this.utilisateur = utilisateur;
        this.authMode = authMode;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }
}
