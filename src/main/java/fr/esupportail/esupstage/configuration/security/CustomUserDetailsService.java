/*******************************************************************************
 * Copyright 2018 UPHF
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package fr.esupportail.esupstage.configuration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import fr.esupportail.esupstage.domain.jpa.repositories.EnseignantRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.EtudiantRepository;
import fr.esupportail.esupstage.domain.jpa.repositories.PersonnelCentreGestionRepository;
import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Component
public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	private final ApplicationProperties applicationProperties;

	private final PersonnelCentreGestionRepository personnelCentreGestionRepository;

	private final EtudiantRepository etudiantRepository;

	private final EnseignantRepository enseignantRepository;

	@Autowired
	public CustomUserDetailsService(final ApplicationProperties applicationProperties, final PersonnelCentreGestionRepository personnelCentreGestionRepository,
			final EtudiantRepository etudiantRepository, final EnseignantRepository enseignantRepository) {
		super();
		this.applicationProperties = applicationProperties;
		this.personnelCentreGestionRepository = personnelCentreGestionRepository;
		this.etudiantRepository = etudiantRepository;
		this.enseignantRepository = enseignantRepository;
	}

	@Override
	public UserDetails loadUserDetails(final CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		final String login = token.getPrincipal().toString();
		return new User(login, "", AuthoritiesUtils.getAuthorities(applicationProperties, personnelCentreGestionRepository, etudiantRepository, enseignantRepository, login));
	}

}
