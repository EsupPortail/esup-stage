package org.esup_portail.esup_stage.security.interceptor;

import org.esup_portail.esup_stage.enums.AppFonctionEnum;
import org.esup_portail.esup_stage.enums.DroitEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target({ TYPE, METHOD })
@Retention(RUNTIME)
public @interface Secure {
    AppFonctionEnum fonction() default AppFonctionEnum.NONE;
    DroitEnum[] droits() default {};
}
