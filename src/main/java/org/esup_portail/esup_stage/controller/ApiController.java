package org.esup_portail.esup_stage.controller;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface ApiController {
    @AliasFor(annotation = RestController.class)
    String value() default "";
}
