package org.esup_portail.esup_stage.service.crontask;

import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.repository.CronTaskJpaRepository;
import org.esup_portail.esup_stage.scheduler.CronScheduler;
import org.esup_portail.esup_stage.security.ServiceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class CronTaskService {

    @Autowired
    private CronTaskJpaRepository cronTaskJpaRepository;

    @Autowired
    private CronTaskJpaRepository CronTaskJpaRepository;

    public List<CronTask> getActiveTasks() {
        return CronTaskJpaRepository.findByActiveTrue();
    }

    public CronTask updateLastExecution(Integer id) {
        CronTask task = CronTaskJpaRepository.findById(id).orElseThrow();
        task.setDateDernierExecution(new Date());
        return CronTaskJpaRepository.save(task);
    }

    public CronTask update(Integer id, String Nom, String ExpressionCron, Boolean Active) {
        CronTask existingCronTask = cronTaskJpaRepository.findById(id).orElse(null);
        if(existingCronTask == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }
        existingCronTask.setActive(Active);
        existingCronTask.setExpressionCron(ExpressionCron);
        existingCronTask.setNom(Nom);
        existingCronTask.setDateModification(new Date());
        existingCronTask.setLoginModification(ServiceContext.getUtilisateur().getLogin());
        return cronTaskJpaRepository.save(existingCronTask);
    }

    public CronTask getById(Integer id) {
        return cronTaskJpaRepository.findById(id).orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CronTask not found"));
    }

}
