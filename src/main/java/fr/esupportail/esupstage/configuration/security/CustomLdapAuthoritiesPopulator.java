package fr.esupportail.esupstage.configuration.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;

import fr.esupportail.esupstage.domain.jpa.repositories.PersonnelCentreGestionRepository;
import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Component
public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	private final ApplicationProperties applicationProperties;

	private final PersonnelCentreGestionRepository personnelCentreGestionRepository;

	@Autowired
	public CustomLdapAuthoritiesPopulator(final ApplicationProperties applicationProperties, final PersonnelCentreGestionRepository personnelCentreGestionRepository) {
		super();
		this.applicationProperties = applicationProperties;
		this.personnelCentreGestionRepository = personnelCentreGestionRepository;
	}

	@Override
	public Collection<? extends GrantedAuthority> getGrantedAuthorities(final DirContextOperations userData, final String login) {
		return AuthoritiesUtils.getAuthorities(applicationProperties, personnelCentreGestionRepository, login);
	}

}
