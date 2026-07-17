package org.esup_portail.esup_stage.constants;

/**
 * Expressions régulières centralisées pour la validation des saisies.
 * <p>
 * Centraliser ces motifs évite leur dispersion dans les DTO et garantit
 * qu'une mise à jour (renforcement de sécurité, correction) s'applique
 * partout de façon cohérente.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /**
     * Validation syntaxique des adresses mail.
     * <p>
     * Doit rester aligné avec {@code REGEX.EMAIL} du frontend
     * (src/frontend/src/app/utils/regex.utils.ts).
     * <p>
     * Volontairement sans la limite RFC 5321 de 64 caractères sur la partie
     * locale, contrairement à l'annotation {@code @Email} d'Hibernate Validator,
     * afin d'autoriser le stockage d'adresses longues.
     */
    public static final String EMAIL = "^(?!.*\\.\\.)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
}
