package org.esup_portail.esup_stage.scheduler.SchedulableTasks;

import org.esup_portail.esup_stage.service.signature.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("UpdateSignature")
public class UpdateSignatureDatesTask implements SchedulableTask {

    @Autowired
    private SignatureService signatureService;

    @Override
    public Runnable getRunnable() {
        return () -> signatureService.updateAuto();
    }
}
