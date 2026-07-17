package org.esup_portail.esup_stage.pojo;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exerce toutes les énumérations de l'application : values(), valueOf(),
 * toString() et les accesseurs propres à chaque constante.
 */
class EnumsTest {

    @TestFactory
    List<DynamicTest> enumsDeLApplication() {
        List<Class<?>> enums = scanEnums();
        assertThat(enums).as("énumérations détectées").hasSizeGreaterThanOrEqualTo(10);
        List<DynamicTest> tests = new ArrayList<>();
        for (Class<?> enumClass : enums) {
            tests.add(DynamicTest.dynamicTest(enumClass.getSimpleName(), () -> exerciseEnum(enumClass)));
        }
        return tests;
    }

    private static List<Class<?>> scanEnums() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter((metadataReader, metadataReaderFactory) ->
                "java.lang.Enum".equals(metadataReader.getClassMetadata().getSuperClassName()));
        List<Class<?>> result = new ArrayList<>();
        for (BeanDefinition bd : provider.findCandidateComponents("org.esup_portail.esup_stage")) {
            String className = bd.getBeanClassName();
            if (className == null || className.startsWith("org.esup_portail.esup_stage.docaposte.gen")) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isEnum()) {
                    result.add(clazz);
                }
            } catch (ClassNotFoundException ignored) {
                // classe non chargeable : ignorée
            }
        }
        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void exerciseEnum(Class<?> enumClass) throws Exception {
        Object[] constants = enumClass.getEnumConstants();
        assertThat(constants).as("%s doit déclarer au moins une constante", enumClass.getSimpleName()).isNotEmpty();

        for (Object constant : constants) {
            Enum<?> value = (Enum<?>) constant;
            assertThat(Enum.valueOf((Class) enumClass, value.name())).isSameAs(value);
            assertThat(value.toString()).isNotNull();

            // exécute les accesseurs spécifiques déclarés par l'enum (getValue(), getLibelle()…)
            for (Method method : enumClass.getDeclaredMethods()) {
                boolean accessor = Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == 0
                        && (method.getName().startsWith("get") || method.getName().startsWith("is"));
                if (accessor) {
                    method.invoke(value);
                }
            }
        }
    }
}
