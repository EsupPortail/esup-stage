package fr.esupportail.esupstage.configuration.security;

import static fr.esupportail.esupstage.configuration.security.UserRole.ADMINISTRATOR;
import static fr.esupportail.esupstage.configuration.security.UserRole.STUDENT;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	private final ApplicationProperties applicationProperties;

	@Autowired
	public CustomLdapAuthoritiesPopulator(final ApplicationProperties applicationProperties) {
		super();
		this.applicationProperties = applicationProperties;
	}

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(final DirContextOperations userData, final String login) {
		final Collection<GrantedAuthority> authorities = new ArrayList<>();

		// TODO – When does a user should get the "STUDENT" role?
		authorities.add(new SimpleGrantedAuthority(STUDENT.toString()));

		if (applicationProperties.getAdministrators().contains(login)) {
			// TODO – The "ADMINISTRATOR" role should only be given to a specific user if no administrator is registered.
			authorities.add(new SimpleGrantedAuthority(ADMINISTRATOR.toString()));
		}
		return authorities;
	}

}
