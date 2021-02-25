package fr.esupportail.esupstage.domain.ldap;

import java.io.Serializable;
import java.util.List;

import javax.naming.Name;

import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * @author david
 */
@Data
@Entry(objectClasses = { "supannPerson" }, base = "ou=people,dc=uphf,dc=fr")
public class LdapUser implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@JsonIgnore
	private Name dn;

	@Attribute(name = "uid")
	private String login;

	@Attribute(name = "supannAliasLogin")
	private String aliasLogin;

	@Attribute(name = "sn")
	private String nom;

	@Attribute(name = "givenName")
	private String prenom;

	@Attribute(name = "mail")
	private String mail;

	@Attribute(name = "memberOf")
	private List<String> groupes;

}
