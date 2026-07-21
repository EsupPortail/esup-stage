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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
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
    void executeTaskNowExecuteLaTacheEtTraceLaDerniereExecution() {
        CronTask cronTask = tache(3, "ArchiverConventions", "0 0 2 * * ?");
        when(cronTaskService.getById(3)).thenReturn(cronTask);
        AtomicInteger executions = new AtomicInteger();
        beanTache("ArchiverConventions", executions::incrementAndGet);

        scheduler.executeTaskNow(3);

        assertThat(executions.get()).isEqualTo(1);
        verify(cronTaskService).updateLastExecution(3);
    }

    @Test
    void executeTaskNowRemonteUneErreurTechniqueEnTracantLExecution() {
        CronTask cronTask = tache(4, "PurgerConventions", "0 0 3 * * ?");
        when(cronTaskService.getById(4)).thenReturn(cronTask);
        beanTache("PurgerConventions", () -> {
            throw new IllegalStateException("échec de la purge");
        });

        assertThatThrownBy(() -> scheduler.executeTaskNow(4))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
        // Même en échec, la date de dernière exécution est tracée
        verify(cronTaskService).updateLastExecution(4);
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
}
