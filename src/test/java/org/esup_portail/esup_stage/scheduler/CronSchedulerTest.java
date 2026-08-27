package org.esup_portail.esup_stage.scheduler;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.scheduler.SchedulableTasks.SchedulableTask;
import org.esup_portail.esup_stage.service.crontask.CronTaskService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CronSchedulerTest {

    private CronScheduler scheduler;
    private CronTaskService cronTaskService;
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        cronTaskService = mock(CronTaskService.class);
        applicationContext = mock(ApplicationContext.class);
        scheduler = new CronScheduler(cronTaskService);
        ReflectionTestUtils.setField(scheduler, "applicationContext", applicationContext);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdown();
    }

    private CronTask tache(int id, String nom, String expressionCron) {
        CronTask cronTask = new CronTask();
        cronTask.setId(id);
        cronTask.setNom(nom);
        cronTask.setExpressionCron(expressionCron);
        cronTask.setActive(true);
        return cronTask;
    }

    private SchedulableTask beanTache(String nom, Runnable runnable) {
        SchedulableTask schedulableTask = mock(SchedulableTask.class);
        when(schedulableTask.getRunnable()).thenReturn(runnable);
        when(applicationContext.getBean(nom)).thenReturn(schedulableTask);
        return schedulableTask;
    }

    // Attend qu'une condition devienne vraie (jusqu'à 2s) puis l'affirme
    private void attendreQue(BooleanSupplier condition) throws InterruptedException {
        long fin = System.currentTimeMillis() + 2000;
        while (System.currentTimeMillis() < fin && !condition.getAsBoolean()) {
            Thread.sleep(20);
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    @Test
    void unBeanInconnuNempechePasLaPlanification() {
        when(applicationContext.getBean("Inconnue")).thenThrow(new NoSuchBeanDefinitionException("Inconnue"));

        assertThatCode(() -> scheduler.scheduleTask(tache(1, "Inconnue", "0 0 2 * * ?")))
                .doesNotThrowAnyException();
    }

    @Test
    void uneExpressionCronInvalideNempechePasLeDemarrage() {
        beanTache("ArchiverConventions", () -> { });

        // Une expression corrompue en base ne doit pas faire échouer le démarrage de l'application
        assertThatCode(() -> scheduler.scheduleTask(tache(1, "ArchiverConventions", "expression invalide")))
                .doesNotThrowAnyException();
    }

    @Test
    void uneExpressionValideEstPlanifiee() {
        beanTache("ArchiverConventions", () -> { });

        assertThatCode(() -> scheduler.scheduleTask(tache(1, "ArchiverConventions", "0 0 2 * * ?")))
                .doesNotThrowAnyException();
    }

    @Test
    void executeTaskNowExecuteLaTacheEnArrierePlanEtTraceLaDerniereExecution() throws InterruptedException {
        CronTask cronTask = tache(3, "ArchiverConventions", "0 0 2 * * ?");
        when(cronTaskService.getById(3)).thenReturn(cronTask);
        CountDownLatch executee = new CountDownLatch(1);
        AtomicInteger executions = new AtomicInteger();
        beanTache("ArchiverConventions", () -> {
            executions.incrementAndGet();
            executee.countDown();
        });

        // Retour immédiat : le lancement est asynchrone
        scheduler.executeTaskNow(3);

        assertThat(executee.await(2, TimeUnit.SECONDS)).isTrue();
        assertThat(executions.get()).isEqualTo(1);
        // La date de dernière exécution est tracée dans le finally, une fois le traitement terminé
        verify(cronTaskService, timeout(2000)).updateLastExecution(3);
        // La tâche n'est plus marquée en cours
        attendreQue(() -> !scheduler.getRunningTaskIds().contains(3));
    }

    @Test
    void executeTaskNowTraceLExecutionMemeEnCasDErreurSansRemonterLException() throws InterruptedException {
        CronTask cronTask = tache(4, "PurgerConventions", "0 0 3 * * ?");
        when(cronTaskService.getById(4)).thenReturn(cronTask);
        CountDownLatch executee = new CountDownLatch(1);
        beanTache("PurgerConventions", () -> {
            try {
                throw new IllegalStateException("échec de la purge");
            } finally {
                executee.countDown();
            }
        });

        // L'exécution asynchrone ne remonte pas l'erreur à l'appelant
        assertThatCode(() -> scheduler.executeTaskNow(4)).doesNotThrowAnyException();

        assertThat(executee.await(2, TimeUnit.SECONDS)).isTrue();
        // Même en échec, la date de dernière exécution est tracée et l'état « en cours » est libéré
        verify(cronTaskService, timeout(2000)).updateLastExecution(4);
        attendreQue(() -> !scheduler.getRunningTaskIds().contains(4));
    }

    @Test
    void executeTaskNowRenvoie404SiLeBeanNexistePas() {
        CronTask cronTask = tache(5, "Disparue", "0 0 2 * * ?");
        when(cronTaskService.getById(5)).thenReturn(cronTask);
        when(applicationContext.getBean("Disparue")).thenThrow(new NoSuchBeanDefinitionException("Disparue"));

        assertThatThrownBy(() -> scheduler.executeTaskNow(5))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void uneTacheDejaEnCoursEstVisibleDansLEtatPartageEtNePeutEtreRelancee() throws InterruptedException {
        CronTask cronTask = tache(6, "ArchiverConventions", "0 0 2 * * ?");
        when(cronTaskService.getById(6)).thenReturn(cronTask);
        CountDownLatch demarree = new CountDownLatch(1);
        CountDownLatch liberer = new CountDownLatch(1);
        beanTache("ArchiverConventions", () -> {
            demarree.countDown();
            try {
                liberer.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        scheduler.executeTaskNow(6);
        assertThat(demarree.await(2, TimeUnit.SECONDS)).isTrue();

        // L'état « en cours » est partagé (visible par tous via le scheduler singleton)
        assertThat(scheduler.getRunningTaskIds()).contains(6);

        // Un second lancement pendant l'exécution est rejeté (conflit)
        assertThatThrownBy(() -> scheduler.executeTaskNow(6))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));

        liberer.countDown();
        attendreQue(() -> !scheduler.getRunningTaskIds().contains(6));
    }
}
