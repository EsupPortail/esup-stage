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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class CronScheduler {

    private final CronTaskService taskService;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    // Identifiants des tâches en cours d'exécution (planifiées ou manuelles). Ce composant est un
    // singleton Spring : l'état est donc partagé par toutes les requêtes, tous utilisateurs confondus,
    // ce qui permet à l'écran d'administration de montrer à chacun qu'une tâche est en cours.
    private final Set<Integer> runningTaskIds = ConcurrentHashMap.newKeySet();

    // Exécuteur dédié aux lancements manuels : le thread de la requête HTTP n'est pas bloqué (retour
    // immédiat) et les lancements manuels n'entrent pas en concurrence avec les threads de planification.
    private final ExecutorService manualExecutor = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "cron-manual-");
        thread.setDaemon(true);
        return thread;
    });

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
        manualExecutor.shutdownNow();
    }

    public void scheduleTask(CronTask task) {
        // Trouve la task par son nom (le champ 'nom' doit matcher le bean Spring !)
        SchedulableTask schedulableTask = resolveTask(task.getNom());
        if (schedulableTask == null) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            return;
        }

        Runnable runnable = () -> {
            if (!runTask(task.getId(), task.getNom(), schedulableTask, false)) {
                log.warn("Tâche {} déjà en cours : exécution planifiée ignorée", task.getNom());
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

    /**
     * Lance une exécution manuelle de la tâche en arrière-plan et rend la main immédiatement : la
     * requête HTTP n'attend pas la fin du traitement. L'avancement est suivi via l'état « en cours »
     * partagé ({@link #getRunningTaskIds()}). Une tâche déjà en cours ne peut être relancée.
     */
    public void executeTaskNow(int taskId) {
        CronTask task = taskService.getById(taskId);
        if (task == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }

        SchedulableTask schedulableTask = resolveTask(task.getNom());
        if (schedulableTask == null) {
            log.error("Aucune tâche SchedulableTask trouvée pour le nom : {}", task.getNom());
            throw new AppException(HttpStatus.NOT_FOUND, "SchedulableTask not found: " + task.getNom());
        }

        if (!runTask(taskId, task.getNom(), schedulableTask, true)) {
            throw new AppException(HttpStatus.CONFLICT, "La tâche " + task.getNom() + " est déjà en cours d'exécution");
        }
    }

    /**
     * Marque la tâche comme « en cours » puis l'exécute. En mode manuel l'exécution est déléguée à un
     * thread dédié (retour immédiat de l'appelant) ; en mode planifié elle a lieu sur le thread
     * appelant (déjà un thread de planification). L'état « en cours » est retiré une fois le
     * traitement terminé, y compris en cas d'erreur.
     *
     * @return false si la tâche était déjà en cours (aucun nouveau lancement), true sinon
     */
    private boolean runTask(int taskId, String nom, SchedulableTask schedulableTask, boolean manuel) {
        // add() est atomique : garantit qu'une seule exécution de cette tâche est en cours à la fois
        if (!runningTaskIds.add(taskId)) {
            return false;
        }

        Runnable body = () -> {
            log.info("Exécution {} de la tâche : {}", manuel ? "manuelle" : "planifiée", nom);
            try {
                schedulableTask.getRunnable().run();
            } catch (Exception e) {
                log.error("Erreur lors de l'exécution de la tâche {} : {}", nom, e.getMessage(), e);
            } finally {
                taskService.updateLastExecution(taskId);
                runningTaskIds.remove(taskId);
            }
        };

        if (manuel) {
            try {
                manualExecutor.submit(body);
            } catch (RuntimeException e) {
                // Soumission refusée (arrêt en cours...) : on ne laisse pas la tâche marquée en cours
                runningTaskIds.remove(taskId);
                throw e;
            }
        } else {
            body.run();
        }
        return true;
    }

    private SchedulableTask resolveTask(String nom) {
        try {
            return (SchedulableTask) applicationContext.getBean(nom);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Identifiants des tâches actuellement en cours d'exécution (planifiées ou manuelles).
     */
    public Set<Integer> getRunningTaskIds() {
        return new HashSet<>(runningTaskIds);
    }
}
