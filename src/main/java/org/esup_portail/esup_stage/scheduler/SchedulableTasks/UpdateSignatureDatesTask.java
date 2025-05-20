package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.repository.CronTaskJpaRepository;
import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("UpdateSignature")
public class UpdateSignatureDatesTask implements SchedulableTask {

    @Autowired
    private CronTaskJpaRepository cronTaskJpaRepository;

    @Autowired
    private SignatureService signatureService;

    private CronTask cronTask;

    @Override
    public void init() {
        cronTask = cronTaskJpaRepository.findById(1).orElseThrow();
    }


    @Override
    public String getName() {
        return cronTask.getNom();
    }

    @Override
    public String getCronExpression() {
        return cronTask.getExpressionCron();
    }

    @Override
    public boolean isEnable() {
        return cronTask.isActive();
    }

    @Override
    public Runnable getRunnable() {
        return () -> {
            signatureService.updateAuto();
        };
    }
}
