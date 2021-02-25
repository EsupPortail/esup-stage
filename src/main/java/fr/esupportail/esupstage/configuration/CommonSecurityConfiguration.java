package fr.esupportail.esupstage.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import fr.esupportail.esupstage.property.ApplicationProperties;

/**
 * @author vdubus
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
public class CommonSecurityConfiguration {

	private final ApplicationProperties applicationProperties;

	public CommonSecurityConfiguration(final ApplicationProperties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		final CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(this.applicationProperties.getCors().getAllowedOrigins());
		configuration.setAllowedMethods(this.applicationProperties.getCors().getAllowedMethods());
		configuration.setAllowedHeaders(this.applicationProperties.getCors().getAllowedHeaders());
		configuration.setExposedHeaders(this.applicationProperties.getCors().getExposedHeaders());
		configuration.setAllowCredentials(true);
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
