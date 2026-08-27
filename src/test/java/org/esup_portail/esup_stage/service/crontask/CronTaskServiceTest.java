package org.esup_portail.esup_stage.service.crontask;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.repository.CronTaskJpaRepository;
import org.esup_portail.esup_stage.scheduler.SchedulableTasks.SchedulableTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CronTaskServiceTest {

    private CronTaskService service;
    private CronTaskJpaRepository cronTaskJpaRepository;
    private ApplicationContext applicationContext;

    @BeforeEach
    void setUp() {
        service = new CronTaskService();
        cronTaskJpaRepository = mock(CronTaskJpaRepository.class);
        applicationContext = mock(ApplicationContext.class);
        ReflectionTestUtils.setField(service, "cronTaskJpaRepository", cronTaskJpaRepository);
        ReflectionTestUtils.setField(service, "applicationContext", applicationContext);
        when(cronTaskJpaRepository.save(any(CronTask.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CronTask tache(int id, String nom) {
        CronTask cronTask = new CronTask();
        cronTask.setId(id);
        cronTask.setNom(nom);
        cronTask.setExpressionCron("0 0 2 * * ?");
        when(cronTaskJpaRepository.findById(id)).thenReturn(Optional.of(cronTask));
        return cronTask;
    }

    private void beanExiste(String nom) {
        SchedulableTask bean = mock(SchedulableTask.class);
        when(applicationContext.containsBean(nom)).thenReturn(true);
        when(applicationContext.getBean(nom)).thenReturn(bean);
    }

    @Test
    void updateModifieLaTacheEtTraceLaModification() {
        tache(1, "ArchiverConventions");
        beanExiste("ArchiverConventions");

        CronTask updated = service.update(1, "ArchiverConventions", "0 15 4 * * ?", false);

        assertThat(updated.getExpressionCron()).isEqualTo("0 15 4 * * ?");
        assertThat(updated.isActive()).isFalse();
        assertThat(updated.getDateModification()).isNotNull();
        // Sans utilisateur connecté (exécution technique), le login de modification est tracé "(auto)"
        assertThat(updated.getLoginModification()).isEqualTo("(auto)");
    }

    @Test
    void updateRefuseUnNomSansBeanCorrespondant() {
        tache(1, "ArchiverConventions");
        when(applicationContext.containsBean("Inconnue")).thenReturn(false);

        assertThatThrownBy(() -> service.update(1, "Inconnue", "0 0 2 * * ?", true))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateRefuseUnBeanQuiNestPasUneTachePlanifiable() {
        tache(1, "ArchiverConventions");
        when(applicationContext.containsBean("autreBean")).thenReturn(true);
        when(applicationContext.getBean("autreBean")).thenReturn(new Object());

        assertThatThrownBy(() -> service.update(1, "autreBean", "0 0 2 * * ?", true))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateRefuseUneExpressionCronInvalide() {
        tache(1, "ArchiverConventions");
        beanExiste("ArchiverConventions");

        assertThatThrownBy(() -> service.update(1, "ArchiverConventions", "pas une expression", true))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateRenvoie404SiLaTacheNexistePas() {
        when(cronTaskJpaRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99, "ArchiverConventions", "0 0 2 * * ?", true))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateLastExecutionPoseLaDateDeDerniereExecution() {
        CronTask cronTask = tache(1, "ArchiverConventions");

        service.updateLastExecution(1);

        assertThat(cronTask.getDateDernierExecution()).isNotNull();
        assertThat(cronTask.getDateDernierExecution()).isCloseTo(new Date(), 60_000);
    }

    @Test
    void getByIdRenvoie404SiIntrouvable() {
        when(cronTaskJpaRepository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(42))
                .isInstanceOf(AppException.class)
                .satisfies(e -> assertThat(((AppException) e).getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
