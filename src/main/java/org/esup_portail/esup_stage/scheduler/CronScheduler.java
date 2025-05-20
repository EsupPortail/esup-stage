package org.esup_portail.esup_stage.scheduler;

import jakarta.annotation.PostConstruct;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.service.crontask.CronTaskService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class CronScheduler {

    private final CronTaskService taskService;
    private final TaskScheduler scheduler = new ConcurrentTaskScheduler();

    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public CronScheduler(CronTaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    public void scheduleTasksAtStartup() {
        taskService.getActiveTasks().forEach(this::scheduleTask);
    }

    public void scheduleTask(CronTask task) {
        Runnable runnable = () -> {
            System.out.println("Exécution tâche : " + task.getNom());
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
        // Annuler l’ancienne tâche si elle existe
        ScheduledFuture<?> future = scheduledTasks.get(taskId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(taskId);
        }

        // Récupérer la tâche depuis le service
        CronTask task = taskService.getById(taskId);

        // Reprogrammer la tâche si elle est active
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
}
