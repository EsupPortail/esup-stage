package org.esup_portail.esup_stage.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.scheduler.SchedulableTasks.SchedulableTask;
import org.esup_portail.esup_stage.service.crontask.CronTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class CronScheduler {

    private final CronTaskService taskService;
    private final TaskScheduler scheduler = new ConcurrentTaskScheduler();
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    private ApplicationContext applicationContext; // Pour injecter dynamiquement les tasks

    public CronScheduler(CronTaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    public void scheduleTasksAtStartup() {
        taskService.getActiveTasks().forEach(this::scheduleTask);
    }

    public void scheduleTask(CronTask task) {
        // Trouve la task par son nom (le champ 'nom' doit matcher le bean Spring !)
        SchedulableTask schedulableTask = null;
        try {
            schedulableTask = (SchedulableTask) applicationContext.getBean(task.getNom());
        } catch (Exception e) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            return;
        }

        SchedulableTask finalSchedulableTask = schedulableTask;
        Runnable runnable = () -> {
            log.info("Exécution de la tâche : {}", task.getNom());
            finalSchedulableTask.getRunnable().run();
            taskService.updateLastExecution(task.getId());
        };
        ScheduledFuture<?> future = scheduler.schedule(runnable, new CronTrigger(task.getExpressionCron()));
        scheduledTasks.put(task.getId(), future);
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

        SchedulableTask schedulableTask = null;
        try {
            schedulableTask = (SchedulableTask) applicationContext.getBean(task.getNom());
        } catch (Exception e) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            throw new AppException(HttpStatus.NOT_FOUND, "SchedulableTask not found: " + task.getNom());
        }

        log.info("Exécution manuelle de la tâche : {}", task.getNom());
        schedulableTask.getRunnable().run();
        taskService.updateLastExecution(task.getId());
    }
}
