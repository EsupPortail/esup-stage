package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

/**
 * Tâche exécutable par le {@link org.esup_portail.esup_stage.scheduler.CronScheduler}.
 *
 * Le nom du bean Spring ({@code @Component("...")}) doit correspondre à la colonne
 * {@code nom} de la table {@code CronTask} : c'est par ce nom que le scheduler retrouve
 * la tâche à exécuter. L'expression cron et l'activation sont portées par la table
 * {@code CronTask}, modifiables depuis l'écran d'administration des tâches planifiées.
 */
public interface SchedulableTask {

    Runnable getRunnable();
}
