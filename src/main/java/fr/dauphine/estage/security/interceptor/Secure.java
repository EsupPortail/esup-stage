package fr.dauphine.estage.security.interceptor;

import fr.dauphine.estage.model.RoleEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface Secure {
    RoleEnum[] roles();
}
