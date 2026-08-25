package org.esup_portail.esup_stage.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import org.esup_portail.esup_stage.model.Convention;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Les repositories de pagination sont des singletons Spring : deux requêtes HTTP simultanées
 * traversent la même instance. Tant que l'état de construction de la requête (filtres, jointures,
 * noms de paramètres) était porté par des champs d'instance, cela produisait des
 * ConcurrentModificationException et, plus grave, des filtres d'un appel appliqués à un autre.
 *
 * <p>L'EntityManager est simulé par des proxies sans état partagé mutable (les mocks Mockito ne
 * sont pas conçus pour être invoqués depuis plusieurs threads). La requête simulée renvoie le JPQL
 * généré dans le sujet de stage de l'unique résultat : chaque thread récupère ainsi la requête
 * réellement construite pour son propre appel.</p>
 */
class PaginationRepositoryConcurrencyTest {

    private static final int THREADS = 16;
    private static final int ITERATIONS = 60;

    /** Filtre spécifique "avenant" : ajoute une jointure et une clause propres à cet appel. */
    private static final String FILTRES_AVEC_AVENANT = "{\"avenant\":{\"type\":\"boolean\",\"value\":true,\"specific\":true}}";
    /** Filtre standard : ni jointure, ni clause sur les avenants. */
    private static final String FILTRES_SANS_AVENANT = "{\"sujetStage\":{\"type\":\"text\",\"value\":\"robotique\"}}";

    private static final String JOINTURE_AVENANT = "LEFT JOIN c.avenants avenant";

    @Test
    void lesAppelsConcurrentsNeSeMelangentPas() throws Exception {
        ConventionRepository repository = new ConventionRepository(entityManagerSimule());

        List<String> resultats = enParallele(THREADS, indexThread -> () -> {
            for (int i = 0; i < ITERATIONS; i++) {
                boolean avecAvenant = (indexThread + i) % 2 == 0;
                String jpql = jpqlGenere(repository, avecAvenant ? FILTRES_AVEC_AVENANT : FILTRES_SANS_AVENANT);

                // le JPQL rendu doit correspondre aux filtres passés par CE thread, et à eux seuls
                if (avecAvenant) {
                    assertThat(jpql).contains(JOINTURE_AVENANT)
                            .contains("avenant.id IS NOT NULL")
                            .doesNotContain("LOWER(c.sujetStage) LIKE");
                } else {
                    assertThat(jpql).doesNotContain(JOINTURE_AVENANT)
                            .doesNotContain("avenant.id")
                            .contains("LOWER(c.sujetStage) LIKE :filter0");
                }
            }
            return "ok";
        });

        assertThat(resultats).hasSize(THREADS);
    }

    @Test
    void lesNomsDeParametresRestentPropresAChaqueAppel() throws Exception {
        ConventionRepository repository = new ConventionRepository(entityManagerSimule());
        String filtresMultiples = "{\"sujetStage\":{\"type\":\"text\",\"value\":\"a\"},\"lieuStage\":{\"type\":\"text\",\"value\":\"b\"}}";

        enParallele(THREADS, indexThread -> () -> {
            for (int i = 0; i < ITERATIONS; i++) {
                // la numérotation repart de 0 à chaque appel : jamais de filter2, filter3...
                assertThat(jpqlGenere(repository, filtresMultiples))
                        .contains(":filter0")
                        .contains(":filter1")
                        .doesNotContain(":filter2");
            }
            return "ok";
        });
    }

    @Test
    void countEtFindPaginatedConcurrentsRestentIsoles() throws Exception {
        ConventionRepository repository = new ConventionRepository(entityManagerSimule());

        enParallele(THREADS, indexThread -> () -> {
            for (int i = 0; i < ITERATIONS; i++) {
                if (indexThread % 2 == 0) {
                    assertThat(repository.count(FILTRES_AVEC_AVENANT)).isEqualTo(1L);
                } else {
                    assertThat(jpqlGenere(repository, FILTRES_SANS_AVENANT)).doesNotContain(JOINTURE_AVENANT);
                }
            }
            return "ok";
        });
    }

    private String jpqlGenere(ConventionRepository repository, String filtres) {
        return repository.findPaginated(1, 10, null, "asc", filtres).get(0).getSujetStage();
    }

    /** Lance {@code threads} tâches simultanées et renvoie leurs résultats, en propageant la première erreur. */
    private <R> List<R> enParallele(int threads, IntFunction<Callable<R>> tache) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch depart = new CountDownLatch(1);
        try {
            List<Future<R>> futures = IntStream.range(0, threads)
                    .mapToObj(indexThread -> pool.submit(() -> {
                        depart.await();
                        return tache.apply(indexThread).call();
                    }))
                    .toList();
            depart.countDown();
            List<R> resultats = new ArrayList<>();
            for (Future<R> future : futures) {
                resultats.add(future.get(60, TimeUnit.SECONDS));
            }
            return resultats;
        } finally {
            pool.shutdownNow();
        }
    }

    // ------------------------------------------------------------------
    // EntityManager simulé, sans état partagé mutable
    // ------------------------------------------------------------------

    private EntityManager entityManagerSimule() {
        Object attribut = proxy(Attribute.class, (p, methode, args) -> null);
        Object managedType = proxy(ManagedType.class, (p, methode, args) ->
                methode.getName().equals("getAttribute") ? attribut : null);
        Object metamodel = proxy(Metamodel.class, (p, methode, args) ->
                methode.getName().equals("managedType") ? managedType : null);

        return (EntityManager) proxy(EntityManager.class, (p, methode, args) -> switch (methode.getName()) {
            case "getMetamodel" -> metamodel;
            case "createQuery" -> requeteSimulee((String) args[0]);
            default -> null;
        });
    }

    /** Requête simulée : renvoie le JPQL qui l'a créée, porté par le sujet de stage du résultat. */
    private Object requeteSimulee(String jpql) {
        return proxy(TypedQuery.class, (p, methode, args) -> switch (methode.getName()) {
            case "getResultList" -> List.of(conventionPortant(jpql));
            case "getSingleResult" -> 1L;
            case "getParameters" -> Collections.emptySet();
            case "setParameter", "setFirstResult", "setMaxResults", "setHint", "setFlushMode" -> p;
            default -> null;
        });
    }

    private Convention conventionPortant(String jpql) {
        Convention convention = new Convention();
        convention.setSujetStage(jpql);
        return convention;
    }

    private Object proxy(Class<?> type, InvocationHandler handler) {
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (p, methode, args) -> {
            if (methode.getDeclaringClass() == Object.class) {
                return switch (methode.getName()) {
                    case "toString" -> type.getSimpleName() + "@simule";
                    case "hashCode" -> System.identityHashCode(p);
                    case "equals" -> p == args[0];
                    default -> null;
                };
            }
            return handler.invoke(p, methode, args);
        });
    }
}
