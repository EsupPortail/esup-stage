package org.esup_portail.esup_stage.service.crontask;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.model.Utilisateur;
import org.esup_portail.esup_stage.repository.CronTaskJpaRepository;
import org.esup_portail.esup_stage.scheduler.SchedulableTasks.SchedulableTask;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class CronTaskService {

    @Autowired
    private CronTaskJpaRepository cronTaskJpaRepository;

    @Autowired
    private ApplicationContext applicationContext;

    public List<CronTask> getActiveTasks() {
        return cronTaskJpaRepository.findByActiveTrue();
    }

    public CronTask updateLastExecution(Integer id) {
        CronTask task = cronTaskJpaRepository.findById(id).orElseThrow();
        task.setDateDernierExecution(new Date());
        return cronTaskJpaRepository.save(task);
    }

    public CronTask update(Integer id, String nom, String expressionCron, Boolean active) {
        CronTask existingCronTask = cronTaskJpaRepository.findById(id).orElse(null);
        if (existingCronTask == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }
        // Le nom doit correspondre à un bean SchedulableTask, sinon la tâche ne sera plus jamais exécutée
        checkSchedulableTaskExists(nom);
        if (!CronExpression.isValidExpression(expressionCron)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Expression cron invalide : " + expressionCron);
        }
        existingCronTask.setActive(active);
        existingCronTask.setExpressionCron(expressionCron);
        existingCronTask.setNom(nom);
        existingCronTask.setDateModification(new Date());
        Utilisateur utilisateur = ServiceContext.getUtilisateur();
        existingCronTask.setLoginModification(utilisateur != null ? utilisateur.getLogin() : "(auto)");
        return cronTaskJpaRepository.save(existingCronTask);
    }

    public CronTask getById(Integer id) {
        return cronTaskJpaRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CronTask not found"));
    }

    public CronTask getByNom(String nom) {
        return cronTaskJpaRepository.findByNom(nom);
    }

    /**
     * Prochaine date d'exécution planifiée d'une tâche, calculée depuis son expression cron.
     *
     * @return null si la tâche est absente, inactive ou porte une expression invalide
     */
    public Date getProchaineExecution(CronTask cronTask) {
        if (cronTask == null || !cronTask.isActive() || cronTask.getExpressionCron() == null) {
            return null;
        }
        try {
            LocalDateTime prochaine = CronExpression.parse(cronTask.getExpressionCron()).next(LocalDateTime.now());
            return prochaine != null ? Date.from(prochaine.atZone(ZoneId.systemDefault()).toInstant()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void checkSchedulableTaskExists(String nom) {
        if (nom == null || !applicationContext.containsBean(nom) || !(applicationContext.getBean(nom) instanceof SchedulableTask)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Aucune tâche planifiable ne correspond au nom : " + nom);
        }
    }
}
