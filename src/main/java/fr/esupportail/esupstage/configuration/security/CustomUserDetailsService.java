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

import static fr.esupportail.esupstage.configuration.security.UserRole.ADMINISTRATOR;
import static fr.esupportail.esupstage.configuration.security.UserRole.STUDENT;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Component
public class CustomUserDetailsService implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

	private final ApplicationProperties applicationProperties;

	@Autowired
	public CustomUserDetailsService(final ApplicationProperties applicationProperties) {
		super();
		this.applicationProperties = applicationProperties;
	}

	@Override
	public UserDetails loadUserDetails(final CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
		final String login = token.getPrincipal().toString();
		final Collection<GrantedAuthority> authorities = new ArrayList<>();

		// TODO – When does a user should get the "STUDENT" role?
		authorities.add(new SimpleGrantedAuthority(STUDENT.toString()));

		if (applicationProperties.getAdministrators().contains(login)) {
			// TODO – The "ADMINISTRATOR" role should only be given to a specific user if no administrator is registered.
			authorities.add(new SimpleGrantedAuthority(ADMINISTRATOR.toString()));
		}

		return new User(login, "", authorities);
	}

}
