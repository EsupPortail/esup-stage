package fr.esupportail.esupstage.configuration.security;

import static fr.esupportail.esupstage.configuration.security.UserRole.USER;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

/**
 * @author vdubus
 */
@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	@Autowired
	public CustomLdapAuthoritiesPopulator() {
		super();
	}

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(final DirContextOperations userData, final String login) {
		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(USER.toString()));
		return authorities;
	}

}
