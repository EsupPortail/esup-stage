package org.esup_portail.esup_stage.model;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exerce les implémentations de {@link Exportable#getExportValue(String)} :
 * chaque clé d'export connue (extraite des {@code case "..."} du code, voir
 * src/test/resources/exportable-keys.properties) est appelée sur une instance
 * vide puis sur une instance remplie.
 *
 * Contrats vérifiés :
 * - une clé inconnue renvoie une valeur vide (branche default) sans lever d'erreur ;
 * - l'appel des clés connues ne lève pas d'erreur sur une entité remplie de
 *   valeurs simples (les chaînons profonds non remplis sont tolérés et comptés).
 */
class ExportableEntitiesTest {

    @TestFactory
    List<DynamicTest> exportsDesEntites() throws Exception {
        Properties keysByClass = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/exportable-keys.properties")) {
            keysByClass.load(in);
        }
        assertThat(keysByClass).as("clés d'export extraites du code").isNotEmpty();

        List<DynamicTest> tests = new ArrayList<>();
        for (String className : keysByClass.stringPropertyNames()) {
            String[] keys = keysByClass.getProperty(className).split(",");
            tests.add(DynamicTest.dynamicTest(className, () -> exerciseExportable(className, keys)));
        }
        return tests;
    }

    private void exerciseExportable(String className, String[] keys) throws Exception {
        Class<?> clazz = Class.forName("org.esup_portail.esup_stage.model." + className);
        Exportable vide = (Exportable) clazz.getDeclaredConstructor().newInstance();
        Exportable rempli = (Exportable) filledInstance(clazz);

        // branche default : clé inconnue → valeur vide, jamais d'exception
        String inconnue = rempli.getExportValue("__cle_inconnue__");
        assertThat(inconnue == null || inconnue.isEmpty())
                .as("%s : une clé inconnue doit renvoyer une valeur vide", className)
                .isTrue();

        int erreursInstanceRemplie = 0;
        for (String key : keys) {
            try {
                vide.getExportValue(key);
            } catch (RuntimeException ignored) {
                // instance vide : les NPE de chaînons absents sont attendues
            }
            try {
                rempli.getExportValue(key);
            } catch (RuntimeException e) {
                erreursInstanceRemplie++;
            }
        }
        // la majorité des clés doit être exploitable sur une entité remplie
        assertThat(erreursInstanceRemplie)
                .as("%s : trop de clés d'export en erreur sur une instance remplie", className)
                .isLessThanOrEqualTo(keys.length / 2);
    }

    /** Remplit l'entité de valeurs simples via ses setters (profondeur 1). */
    private Object filledInstance(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                continue;
            }
            Object sample = sample(property.getPropertyType(), 0);
            if (sample == null) {
                continue;
            }
            try {
                setter.invoke(instance, sample);
            } catch (ReflectiveOperationException ignored) {
                // propriété non remplissable : ignorée
            }
        }
        return instance;
    }

    private static final Map<String, Object> CACHE = new HashMap<>();

    private Object sample(Class<?> type, int depth) {
        if (type == String.class) return "O"; // "O" pour satisfaire les tests temEnServ.equals("O")
        if (type == int.class || type == Integer.class) return 7;
        if (type == long.class || type == Long.class) return 7L;
        if (type == double.class || type == Double.class) return 7.5d;
        if (type == float.class || type == Float.class) return 7.5f;
        if (type == boolean.class || type == Boolean.class) return true;
        if (type == BigDecimal.class) return BigDecimal.TEN;
        if (type == BigInteger.class) return BigInteger.TEN;
        if (type == Date.class) return new Date(1700000000000L);
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? null : constants[0];
        }
        if (type == List.class) return new ArrayList<>();
        if (type.getName().startsWith("org.esup_portail.esup_stage") && depth < 2
                && !type.isInterface() && !Modifier.isAbstract(type.getModifiers())) {
            String key = type.getName() + "#" + depth;
            if (CACHE.containsKey(key)) {
                return CACHE.get(key);
            }
            try {
                Object nested = type.getDeclaredConstructor().newInstance();
                // remplit aussi les objets imbriqués pour couvrir les branches non nulles
                if (depth == 0) {
                    BeanInfo beanInfo = Introspector.getBeanInfo(type, Object.class);
                    for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
                        Method setter = property.getWriteMethod();
                        if (setter == null) {
                            continue;
                        }
                        Object sample = sample(property.getPropertyType(), depth + 1);
                        if (sample != null) {
                            try {
                                setter.invoke(nested, sample);
                            } catch (ReflectiveOperationException ignored) {
                                // propriété imbriquée non remplissable : ignorée
                            }
                        }
                    }
                }
                CACHE.put(key, nested);
                return nested;
            } catch (Exception e) {
                CACHE.put(key, null);
                return null;
            }
        }
        return null;
    }
}
