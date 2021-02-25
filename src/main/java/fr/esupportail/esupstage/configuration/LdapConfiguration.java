package fr.esupportail.esupstage.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;

@Configuration
@ConditionalOnClass(ContextSource.class)
@EnableLdapRepositories(basePackages = "fr.esupportail.esupstage.domain.ldap")
public class LdapConfiguration {

	@Bean
	public LdapTemplate ldapTemplate(final ContextSource contextSource) {
		final LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
		ldapTemplate.setDefaultCountLimit(10);
		ldapTemplate.setIgnoreSizeLimitExceededException(true);
		ldapTemplate.setDefaultTimeLimit(1000);
		return ldapTemplate;
	}

}
