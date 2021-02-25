package fr.esupportail.esupstage.property;

import java.io.Serializable;

import org.springframework.boot.context.properties.NestedConfigurationProperty;

import lombok.Getter;
import lombok.Setter;

/**
 * Properties link to the application security.
 *
 * @author vdubus
 */
@Getter
@Setter
public class SecurityProperties implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The user DN pattern to login with LDAP.<br>
	 * Example: uid={0},ou=people,dc=uphf,dc=fr
	 */
	private String ldapUserDnPatterns;

	@NestedConfigurationProperty
	private CASProperties cas = new CASProperties();

}
