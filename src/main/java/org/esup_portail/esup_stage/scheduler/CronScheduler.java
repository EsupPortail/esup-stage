package org.esup_portail.esup_stage.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.scheduler.SchedulableTasks.SchedulableTask;
import org.esup_portail.esup_stage.service.crontask.CronTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class CronScheduler {

    private final CronTaskService taskService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext; // Pour injecter dynamiquement les tasks

    public CronScheduler(CronTaskService taskService) {
        this.taskService = taskService;
        this.scheduler.setPoolSize(2);
        this.scheduler.setThreadNamePrefix("cron-task-");
        this.scheduler.initialize();
    }

    @PostConstruct
    public void scheduleTasksAtStartup() {
        taskService.getActiveTasks().forEach(this::scheduleTask);
    }

    @PreDestroy
    public void shutdown() {
        cancelAll();
        scheduler.shutdown();
    }

    public void scheduleTask(CronTask task) {
        // Trouve la task par son nom (le champ 'nom' doit matcher le bean Spring !)
        SchedulableTask schedulableTask;
        try {
            schedulableTask = (SchedulableTask) applicationContext.getBean(task.getNom());
        } catch (Exception e) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            return;
        }

        Runnable runnable = () -> {
            log.info("Exécution de la tâche : {}", task.getNom());
            try {
                schedulableTask.getRunnable().run();
            } catch (Exception e) {
                log.error("Erreur lors de l'exécution de la tâche {} : {}", task.getNom(), e.getMessage(), e);
            } finally {
                taskService.updateLastExecution(task.getId());
            }
        };
        try {
            ScheduledFuture<?> future = scheduler.schedule(runnable, new CronTrigger(task.getExpressionCron()));
            scheduledTasks.put(task.getId(), future);
        } catch (IllegalArgumentException e) {
            // Une expression invalide ne doit pas empêcher le démarrage de l'application ni la planification des autres tâches
            log.error("Expression cron invalide pour la tâche {} : {}", task.getNom(), task.getExpressionCron());
        }
    }

    public void reloadTasks() {
        cancelAll();
        scheduleTasksAtStartup();
    }

    public void reloadTask(int taskId) {
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(taskId);
        }

        CronTask task = taskService.getById(taskId);

        if (task != null && task.isActive()) {
            scheduleTask(task);
        }
    }

    private void cancelAll() {
        scheduledTasks.values().forEach(future -> {
            if (future != null) future.cancel(false);
        });
        scheduledTasks.clear();
    }

    public void executeTaskNow(int taskId) {
        CronTask task = taskService.getById(taskId);
        if (task == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }

        SchedulableTask schedulableTask;
        try {
            schedulableTask = (SchedulableTask) applicationContext.getBean(task.getNom());
        } catch (Exception e) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            throw new AppException(HttpStatus.NOT_FOUND, "SchedulableTask not found: " + task.getNom());
        }

        log.info("Exécution manuelle de la tâche : {}", task.getNom());
        try {
            schedulableTask.getRunnable().run();
        } catch (Exception e) {
            log.error("Erreur lors de l'exécution manuelle de la tâche {} : {}", task.getNom(), e.getMessage(), e);
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'exécution de la tâche " + task.getNom());
        } finally {
            taskService.updateLastExecution(task.getId());
        }
    }
}
