package fr.esupportail.esupstage.configuration.security;

import static fr.esupportail.esupstage.configuration.security.UserRole.ADMINISTRATOR;
import static fr.esupportail.esupstage.configuration.security.UserRole.CHEF_MANAGER;
import static fr.esupportail.esupstage.configuration.security.UserRole.MANAGER;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fr.esupportail.esupstage.domain.jpa.repositories.PersonnelCentreGestionRepository;
import fr.esupportail.esupstage.property.ApplicationProperties;

public class AuthoritiesUtils {

	public static final Collection<GrantedAuthority> getAuthorities(final ApplicationProperties applicationProperties, final PersonnelCentreGestionRepository personnelCentreGestionRepository,
			final String login) {
		final Collection<GrantedAuthority> authorities = new ArrayList<>();
		// TODO – When does a user should get the "STUDENT" role?
		// authorities.add(new SimpleGrantedAuthority(STUDENT.toString()));
		if (personnelCentreGestionRepository.existsOneByUidPersonnel(login)) {
			authorities.add(new SimpleGrantedAuthority(MANAGER.toString()));
			if (personnelCentreGestionRepository.existsOneByUidPersonnelAndDroitAdministrationId(login, 3)) {
				authorities.add(new SimpleGrantedAuthority(CHEF_MANAGER.toString()));
			}
		}

		if (applicationProperties.getAdministrators().contains(login)) {
			authorities.add(new SimpleGrantedAuthority(ADMINISTRATOR.toString()));
		}
		return authorities;
	}

}
