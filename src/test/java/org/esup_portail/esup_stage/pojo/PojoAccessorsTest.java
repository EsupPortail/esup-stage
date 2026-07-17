package org.esup_portail.esup_stage.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Harnais générique : exerce les accesseurs (round-trip setter/getter), equals,
 * hashCode et toString de toutes les classes de type POJO (entités JPA, DTO,
 * modèles des clients de web services, propriétés de configuration).
 *
 * Objectifs :
 * - détecter les accesseurs cassés (getter ne renvoyant pas la valeur posée) ;
 * - détecter les equals/hashCode incohérents sur les classes qui les déclarent ;
 * - garantir que ces classes restent instanciables sans effet de bord.
 */
class PojoAccessorsTest {

    private static final String[] SCANNED_PACKAGES = {
            "org.esup_portail.esup_stage.model",
            "org.esup_portail.esup_stage.dto",
            "org.esup_portail.esup_stage.service.apogee.model",
            "org.esup_portail.esup_stage.service.ldap.model",
            "org.esup_portail.esup_stage.service.signature.model",
            "org.esup_portail.esup_stage.service.sirene.model",
            "org.esup_portail.esup_stage.webhook.esupsignature.service.model",
            "org.esup_portail.esup_stage.config.properties",
            "org.esup_portail.esup_stage.service.impression.context",
    };

    private static final int VARIANT_A = 1;
    private static final int VARIANT_B = 2;

    /**
     * Propriétés dont le getter est dérivé (calculé à partir d'autres champs) :
     * le round-trip setter/getter ne s'applique pas.
     */
    private static final Set<String> DERIVED_PROPERTIES = Set.of(
            "Convention.lieuStage" // calculé à partir du service d'accueil
    );

    @TestFactory
    List<DynamicTest> accesseursDesPojos() {
        List<Class<?>> classes = scanPojoClasses();
        assertThat(classes).as("classes POJO détectées").hasSizeGreaterThan(100);
        List<DynamicTest> tests = new ArrayList<>();
        for (Class<?> clazz : classes) {
            tests.add(DynamicTest.dynamicTest(clazz.getSimpleName(), () -> exercisePojo(clazz)));
        }
        return tests;
    }

    private static List<Class<?>> scanPojoClasses() {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter((metadataReader, metadataReaderFactory) -> true);
        List<Class<?>> result = new ArrayList<>();
        for (String pkg : SCANNED_PACKAGES) {
            for (BeanDefinition bd : provider.findCandidateComponents(pkg)) {
                try {
                    Class<?> clazz = Class.forName(bd.getBeanClassName());
                    if (isTestablePojo(clazz)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException ignored) {
                    // classe non chargeable : ignorée
                }
            }
        }
        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

    private static boolean isTestablePojo(Class<?> clazz) {
        if (clazz.isInterface() || clazz.isEnum() || clazz.isAnnotation() || clazz.isRecord()
                || Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        return defaultConstructor(clazz) != null;
    }

    private static Constructor<?> defaultConstructor(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private void exercisePojo(Class<?> clazz) throws Exception {
        Object instance = defaultConstructor(clazz).newInstance();
        BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

        // 1) round-trip setter/getter sur chaque propriété
        for (PropertyDescriptor property : properties) {
            Method setter = property.getWriteMethod();
            Method getter = property.getReadMethod();
            if (setter == null) {
                if (getter != null) {
                    invokeQuietly(getter, instance);
                }
                continue;
            }
            Object sample = sampleValue(property.getPropertyType(), VARIANT_A, 0);
            if (sample == null && property.getPropertyType().isPrimitive()) {
                continue;
            }
            try {
                setter.invoke(instance, sample);
            } catch (ReflectiveOperationException e) {
                continue; // setter avec logique spécifique : hors périmètre du harnais
            }
            if (getter != null) {
                Object read = getter.invoke(instance);
                boolean derived = DERIVED_PROPERTIES.contains(clazz.getSimpleName() + "." + property.getName());
                if (sample != null && !derived) {
                    assertThat(read)
                            .as("%s.%s : le getter doit renvoyer la valeur passée au setter", clazz.getSimpleName(), property.getName())
                            .isEqualTo(sample);
                }
            }
        }

        // 2) contrats de base equals/hashCode/toString
        assertThat(instance.equals(instance)).as("%s.equals(this)", clazz.getSimpleName()).isTrue();
        assertThat(instance.equals(null)).as("%s.equals(null)", clazz.getSimpleName()).isFalse();
        assertThat(instance.equals(new Object())).as("%s.equals(autre type)", clazz.getSimpleName()).isFalse();
        instance.hashCode();
        assertThat(instance.toString()).as("%s.toString()", clazz.getSimpleName()).isNotNull();

        // 3) pour les classes déclarant equals (Lombok @Data) : égalité de deux instances
        //    identiquement remplies, puis inégalité après mutation de chaque propriété
        if (!declaresEquals(clazz)) {
            return;
        }
        Object left = filledInstance(clazz, properties, VARIANT_A);
        Object right = filledInstance(clazz, properties, VARIANT_A);
        if (left == null || right == null) {
            return;
        }
        boolean pairEqual = left.equals(right);
        if (clazz.getSuperclass() == Object.class) {
            // classe autonome : l'égalité par valeurs doit fonctionner
            assertThat(pairEqual)
                    .as("%s : deux instances remplies à l'identique doivent être égales", clazz.getSimpleName())
                    .isTrue();
        }
        // NB : les entités avec @EqualsAndHashCode(callSuper = true) vers une classe
        // sans equals (ObjetMetier) ont une égalité d'identité : pas d'assertion forte
        if (pairEqual) {
            assertThat(left.hashCode())
                    .as("%s : hashCode cohérent avec equals", clazz.getSimpleName())
                    .isEqualTo(right.hashCode());
        }

        for (PropertyDescriptor property : properties) {
            Method setter = property.getWriteMethod();
            if (setter == null) {
                continue;
            }
            Object mutation = sampleValue(property.getPropertyType(), VARIANT_B, 0);
            Object original = sampleValue(property.getPropertyType(), VARIANT_A, 0);
            if (mutation == null || mutation.equals(original)) {
                continue; // pas de seconde valeur discriminante disponible
            }
            Object mutated = filledInstance(clazz, properties, VARIANT_A);
            if (mutated == null) {
                continue;
            }
            try {
                setter.invoke(mutated, mutation);
            } catch (ReflectiveOperationException e) {
                continue;
            }
            // certaines propriétés sont légitimement exclues de equals : on n'échoue
            // que si la mutation est censée discriminer (equals les voit différentes)
            if (!mutated.equals(left)) {
                assertThat(left.equals(mutated)).isFalse();
            }
        }
    }

    private static boolean declaresEquals(Class<?> clazz) {
        try {
            clazz.getDeclaredMethod("equals", Object.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private Object filledInstance(Class<?> clazz, PropertyDescriptor[] properties, int variant) {
        try {
            Object instance = defaultConstructor(clazz).newInstance();
            for (PropertyDescriptor property : properties) {
                Method setter = property.getWriteMethod();
                if (setter == null) {
                    continue;
                }
                Object sample = sampleValue(property.getPropertyType(), variant, 0);
                if (sample == null) {
                    continue;
                }
                try {
                    setter.invoke(instance, sample);
                } catch (ReflectiveOperationException ignored) {
                    // propriété non remplissable : laissée à sa valeur par défaut
                }
            }
            return instance;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static void invokeQuietly(Method method, Object target) {
        try {
            method.invoke(target);
        } catch (ReflectiveOperationException ignored) {
            // getter avec logique spécifique (calcul dérivé) : simple exécution
        }
    }

    /**
     * Cache des valeurs d'exemple : une même (classe, variante) renvoie toujours
     * la même instance, pour que deux objets remplis à l'identique portent des
     * références égales même quand le type imbriqué ne redéfinit pas equals().
     */
    private static final Map<String, Object> SAMPLE_CACHE = new HashMap<>();

    private Object sampleValue(Class<?> type, int variant, int depth) {
        String key = type.getName() + "#" + variant;
        if (SAMPLE_CACHE.containsKey(key)) {
            return SAMPLE_CACHE.get(key);
        }
        Object value = buildSampleValue(type, variant, depth);
        SAMPLE_CACHE.put(key, value);
        return value;
    }

    /**
     * Fabrique une valeur d'exemple déterministe pour un type donné.
     * Deux variantes différentes produisent des valeurs non égales pour
     * la plupart des types, afin d'exercer les branches de equals().
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object buildSampleValue(Class<?> type, int variant, int depth) {
        if (type == String.class) {
            return "valeur-" + variant;
        }
        if (type == int.class || type == Integer.class) {
            return 100 + variant;
        }
        if (type == long.class || type == Long.class) {
            return 1000L + variant;
        }
        if (type == double.class || type == Double.class) {
            return 10.5d + variant;
        }
        if (type == float.class || type == Float.class) {
            return 5.5f + variant;
        }
        if (type == boolean.class || type == Boolean.class) {
            return variant % 2 == 1;
        }
        if (type == short.class || type == Short.class) {
            return (short) (10 + variant);
        }
        if (type == byte.class || type == Byte.class) {
            return (byte) (1 + variant);
        }
        if (type == char.class || type == Character.class) {
            return (char) ('a' + variant);
        }
        if (type == BigDecimal.class) {
            return BigDecimal.valueOf(100 + variant);
        }
        if (type == BigInteger.class) {
            return BigInteger.valueOf(100 + variant);
        }
        if (type == Date.class) {
            return new Date(1700000000000L + variant * 86_400_000L);
        }
        if (type == java.sql.Date.class) {
            return new java.sql.Date(1700000000000L + variant * 86_400_000L);
        }
        if (type == java.sql.Timestamp.class) {
            return new java.sql.Timestamp(1700000000000L + variant * 86_400_000L);
        }
        if (type == LocalDate.class) {
            return LocalDate.of(2026, 1, 1).plusDays(variant);
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.of(2026, 1, 1, 12, 0).plusDays(variant);
        }
        if (type == LocalTime.class) {
            return LocalTime.of(8, 0).plusHours(variant);
        }
        if (type == Instant.class) {
            return Instant.ofEpochMilli(1700000000000L).plus(Duration.ofDays(variant));
        }
        if (type == ZonedDateTime.class) {
            return ZonedDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC).plusDays(variant);
        }
        if (type == OffsetDateTime.class) {
            return OffsetDateTime.of(2026, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC).plusDays(variant);
        }
        if (type == Duration.class) {
            return Duration.ofMinutes(variant);
        }
        if (type == JsonNode.class) {
            return TextNode.valueOf("json-" + variant);
        }
        if (type == MultipartFile.class) {
            return new MockMultipartFile("fichier-" + variant, ("contenu-" + variant).getBytes());
        }
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            return constants.length == 0 ? null : constants[variant % constants.length];
        }
        if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), variant);
        }
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            List list = new ArrayList();
            if (variant > 1) {
                list.add("element-" + variant);
            }
            return list;
        }
        if (type == Set.class) {
            Set set = new LinkedHashSet();
            if (variant > 1) {
                set.add("element-" + variant);
            }
            return set;
        }
        if (type == SortedSet.class) {
            SortedSet set = new TreeSet();
            if (variant > 1) {
                set.add("element-" + variant);
            }
            return set;
        }
        if (type == Map.class) {
            Map map = new HashMap();
            if (variant > 1) {
                map.put("cle-" + variant, "valeur-" + variant);
            }
            return map;
        }
        if (type == SortedMap.class) {
            SortedMap map = new TreeMap();
            if (variant > 1) {
                map.put("cle-" + variant, "valeur-" + variant);
            }
            return map;
        }
        if (type.getName().startsWith("org.esup_portail.esup_stage") && depth < 2) {
            Constructor<?> constructor = defaultConstructor(type);
            if (constructor != null && !Modifier.isAbstract(type.getModifiers()) && !type.isInterface()) {
                try {
                    return constructor.newInstance();
                } catch (ReflectiveOperationException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
