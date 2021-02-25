package fr.esupportail.esupstage.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import fr.esupportail.esupstage.configuration.security.Http401UnauthorizedEntryPoint;
import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Configuration
@Profile("!cas")
public class LdapAuthenticationSecurityConfiguration extends WebSecurityConfigurerAdapter {

	private final ApplicationProperties applicationProperties;

	private final ContextSource contextSource;

	private final LdapAuthoritiesPopulator ldapAuthoritiesPopulator;

	public LdapAuthenticationSecurityConfiguration(final ApplicationProperties applicationProperties, final ContextSource contextSource,
			final LdapAuthoritiesPopulator ldapAuthoritiesPopulator) {
		this.applicationProperties = applicationProperties;
		this.contextSource = contextSource;
		this.ldapAuthoritiesPopulator = ldapAuthoritiesPopulator;
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception {
		// @formatter:off
		http.authorizeRequests().antMatchers("/swagger-ui/**", "/static/**").permitAll();
		http.authorizeRequests().antMatchers("/api/**").authenticated()
				.and().exceptionHandling().defaultAuthenticationEntryPointFor(new Http401UnauthorizedEntryPoint(), new AntPathRequestMatcher("/api/**"))
				.and().formLogin().successHandler(new SimpleUrlAuthenticationSuccessHandler(this.applicationProperties.getRedirectUrl()))
				.and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		;
		// @formatter:on
		if (this.applicationProperties.getCsrf().isEnable()) {
			final CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
			// It seems that as we don't deploy the front on the same path as the back,
			// we need to set the CSRF Token cookie on the root of the domain used by both applications.
			csrfTokenRepository.setCookiePath(this.applicationProperties.getCsrf().getContext());
			http.csrf().csrfTokenRepository(csrfTokenRepository);
		} else {
			http.csrf().disable();
		}
		http.cors();
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		// @formatter:off
		auth.ldapAuthentication()
				.ldapAuthoritiesPopulator(this.ldapAuthoritiesPopulator)
				.userDnPatterns(applicationProperties.getSecurity().getLdapUserDnPatterns())
				.contextSource((LdapContextSource) this.contextSource)
		;
		// @formatter:on
	}

}
