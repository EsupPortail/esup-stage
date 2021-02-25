package fr.esupportail.esupstage.configuration.jpa;

import java.util.Locale;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class ESUPPhysicalNamingStrategy implements PhysicalNamingStrategy {

	@Override
	public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	@Override
	public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment jdbcEnvironment) {
		return apply(name, jdbcEnvironment);
	}

	private Identifier apply(Identifier name, JdbcEnvironment jdbcEnvironment) {
		if (name == null) {
			return null;
		}
		final StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
		return getIdentifier(builder.toString(), name.isQuoted(), jdbcEnvironment);
	}

	/**
	 * Get an identifier for the specified details. By default this method will return an identifier with the name adapted based on the result of
	 * {@link #isCaseInsensitive(JdbcEnvironment)}
	 *
	 * @param name            the name of the identifier
	 * @param quoted          if the identifier is quoted
	 * @param jdbcEnvironment the JDBC environment
	 * @return an identifier instance
	 */
	protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
		name = name.toLowerCase(Locale.ROOT);
		return new Identifier(name, quoted);
	}

}
