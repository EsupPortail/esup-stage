package org.esup_portail.esup_stage.security.userdetails;

import org.esup_portail.esup_stage.model.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class CasUserDetailsImpl implements UserDetails {

    private final Utilisateur utilisateur;
    private final Collection<? extends GrantedAuthority> grantedAuthorities;

    public CasUserDetailsImpl(Utilisateur utilisateur, Collection<? extends GrantedAuthority> grantedAuthorities) {
        this.utilisateur = utilisateur;
        this.grantedAuthorities = grantedAuthorities;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return utilisateur.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return utilisateur.isActif();
    }
}
