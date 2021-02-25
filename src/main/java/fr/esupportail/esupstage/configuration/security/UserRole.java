package fr.esupportail.esupstage.configuration.security;

/**
 * Enumeration of the different role available for an user.
 *
 * @author vdubus
 */
public enum UserRole {

	USER;

	private static final String ROLE_PREFIX = "ROLE_";

	public static UserRole findBy(final String name) {
		if (null == name) {
			throw new IllegalArgumentException();
		}
		final String value;
		if (name.startsWith(ROLE_PREFIX)) {
			value = name.replaceFirst(ROLE_PREFIX, "");
		} else {
			value = name;
		}
		return valueOf(value);
	}

	@Override
	public String toString() {
		return ROLE_PREFIX + super.toString();
	}

}
