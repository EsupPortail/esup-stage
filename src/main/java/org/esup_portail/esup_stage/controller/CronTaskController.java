package org.esup_portail.esup_stage.controller;

import org.esup_portail.esup_stage.dto.PaginatedResponse;
import org.esup_portail.esup_stage.exception.AppException;
import org.esup_portail.esup_stage.model.CronTask;
import org.esup_portail.esup_stage.repository.CronTaskJpaRepository;
import org.esup_portail.esup_stage.repository.CronTaskRepository;
import org.esup_portail.esup_stage.scheduler.CronScheduler;
import org.esup_portail.esup_stage.service.crontask.CronTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@ApiController
@RequestMapping("/cron")
public class CronTaskController {

    @Autowired
    private CronTaskRepository cronTaskRepository;

    @Autowired
    private CronTaskJpaRepository cronTaskJpaRepository;

    @Autowired
    private CronTaskService cronTaskService;

    @Autowired
    private CronScheduler cronScheduler;

    @GetMapping
    public PaginatedResponse<CronTask> search(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "perPage", defaultValue = "50") int perPage, @RequestParam("predicate") String predicate, @RequestParam(name = "sortOrder", defaultValue = "asc") String sortOrder, @RequestParam(name = "filters", defaultValue = "{}") String filters) {
        PaginatedResponse<CronTask> paginatedResponse = new PaginatedResponse<>();
        paginatedResponse.setTotal(cronTaskRepository.count(filters));
        paginatedResponse.setData(cronTaskRepository.findPaginated(page, perPage, predicate, sortOrder, filters));
        return paginatedResponse;
    }

    @GetMapping("/{id}")
    public CronTask get(@PathVariable("id") Integer id) {
        CronTask cronTask = cronTaskJpaRepository.findById(id).orElse(null);
        if (cronTask == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }
        return cronTask;
    }

    @PutMapping("/{id}")
    public CronTask update(@PathVariable("id") Integer id, @RequestBody CronTask cronTask) {
        CronTask cronTaskUpdated = cronTaskService.update(id, cronTask.getNom(), cronTask.getExpressionCron(), cronTask.isActive());
        if (cronTaskUpdated == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "CronTask not found");
        }
        cronScheduler.reloadTask(id);
        return cronTaskUpdated;
    }

    @PostMapping("/reload")
    public void reload() {
        cronScheduler.reloadTasks();
    }
}
