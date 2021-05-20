package fr.esupportail.esupstage.configuration.security;

import static fr.esupportail.esupstage.configuration.security.UserRole.ADMINISTRATOR;
import static fr.esupportail.esupstage.configuration.security.UserRole.CHEF_MANAGER;
import static fr.esupportail.esupstage.configuration.security.UserRole.MANAGER;
import static fr.esupportail.esupstage.configuration.security.UserRole.REFERENT_TEACHER;
import static fr.esupportail.esupstage.configuration.security.UserRole.STUDENT;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fr.esupportail.esupstage.domain.jpa.repositories.EnseignantRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.EtudiantRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.PersonnelCentreGestionRepository;
import fr.esupportail.esupstage.property.ApplicationProperties;

public class AuthoritiesUtils {

	/**
	 * The ID on the ADMIN right in database.
	 */
	private static final Integer ADMIN_RIGHT_ID = 3;

	public static final Collection<GrantedAuthority> getAuthorities(final ApplicationProperties applicationProperties,
			final PersonnelCentreGestionRepository personnelCentreGestionRepository, final EtudiantRepository etudiantRepository, final EnseignantRepository enseignantRepository,
			final String login) {
		final Collection<GrantedAuthority> authorities = new ArrayList<>();

		if (etudiantRepository.existsOneByIdentEtudiant(login)) {
			authorities.add(new SimpleGrantedAuthority(STUDENT.toString()));
		}

		if (enseignantRepository.existsOneByUidEnseignant(login)) {
			authorities.add(new SimpleGrantedAuthority(REFERENT_TEACHER.toString()));
		}

		if (personnelCentreGestionRepository.existsOneByUidPersonnel(login)) {
			authorities.add(new SimpleGrantedAuthority(MANAGER.toString()));
			if (personnelCentreGestionRepository.existsOneByUidPersonnelAndDroitAdministrationId(login, ADMIN_RIGHT_ID)) {
				authorities.add(new SimpleGrantedAuthority(CHEF_MANAGER.toString()));
			}
		}

		if (applicationProperties.getAdministrators().contains(login)) {
			authorities.add(new SimpleGrantedAuthority(ADMINISTRATOR.toString()));
		}
		return authorities;
	}

}
