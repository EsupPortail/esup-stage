import { Component, OnInit } from '@angular/core';
import { CronService } from '../../../services/cron.service';
import { forkJoin } from 'rxjs';
import { MessageService } from '../../../services/message.service';
import { PageEvent } from '@angular/material/paginator';

@Component({
    selector: 'app-config',
    templateUrl: './tache-planifie.component.html',
    styleUrl: './tache-planifie.component.scss',
    standalone: false
})
export class TachePlanifieComponent implements OnInit {
  cronTasks: any[] = [];
  private originalCronTasks: any;
  page: number = 1;
  itemsPerPage: number = 10;
  totalTasks: number = 0;
  sortPredicate: string = 'id';
  sortOrder: string = 'asc';
  filtersObj: any = {};
  private filterTimeout: any;
  executingTasks: Set<number> = new Set(); // Pour tracker les tâches en cours d'exécution

  constructor(private cronService: CronService, private messageService: MessageService) {}

  ngOnInit() {
    this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string) {
    this.cronService.getPaginated(page, perPage, predicate, sortOrder, filters).subscribe((response: any) => {
      this.cronTasks = response.data;
      this.totalTasks = response.total;
      this.originalCronTasks = JSON.parse(JSON.stringify(response.data));
    });
  }

  resetChanges() {
    this.cronTasks = JSON.parse(JSON.stringify(this.originalCronTasks));
  }

  saveAll() {
    const modifiedTasks = this.cronTasks.filter((task, i) => {
      const original = this.originalCronTasks[i];
      return (
        task.nom !== original.nom ||
        task.expressionCron !== original.expressionCron ||
        task.active !== original.active
      );
    });

    if (modifiedTasks.length === 0) {
      this.messageService.setWarning('Aucune modification détectée.');
      return;
    }

    const requests = modifiedTasks.map(task =>
      this.cronService.update(task.id, task)
    );

    forkJoin(requests).subscribe({
      next: () => {
        this.messageService.setSuccess('Sauvegarde réussie');
        this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
      },
      error: err => this.messageService.setError('Erreur lors de la sauvegarde'),
    });
  }

  executeTask(taskId: number, taskName: string) {
    if (this.executingTasks.has(taskId)) {
      return;
    }

    this.executingTasks.add(taskId);

    this.cronService.executeNow(taskId).subscribe({
      next: () => {
        this.messageService.setSuccess(`Tâche "${taskName}" exécutée avec succès`);
        this.executingTasks.delete(taskId);
        // Rafraîchir pour mettre à jour la dernière date d'exécution
        this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
      },
      error: err => {
        this.messageService.setError(`Erreur lors de l'exécution de la tâche "${taskName}" : ${err.error?.message || err.message}`);
        this.executingTasks.delete(taskId);
      }
    });
  }

  isExecuting(taskId: number): boolean {
    return this.executingTasks.has(taskId);
  }

  onPageChange(event: PageEvent) {
    this.page = event.pageIndex + 1;
    this.itemsPerPage = event.pageSize;
    this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
  }

  get filters(): string {
    const f: any = {};
    if (this.filtersObj.id !== '' && this.filtersObj.id !== null && this.filtersObj.id !== undefined) {
      f.id = { type: 'int', value: this.filtersObj.id };
    }
    if (this.filtersObj.nom && this.filtersObj.nom.trim() !== '') {
      f.nom = { type: 'text', value: this.filtersObj.nom };
    }
    if (this.filtersObj.active !== '' && this.filtersObj.active !== null && this.filtersObj.active !== undefined) {
      f.active = { type: 'boolean', value: this.filtersObj.active };
    }
    return JSON.stringify(f);
  }

  onFilterChange() {
    if (this.filterTimeout) {
      clearTimeout(this.filterTimeout);
    }
    this.filterTimeout = setTimeout(() => {
      this.page = 1;
      this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
    }, 1000);
  }

  sort(column: string) {
    if (column === 'active') return;
    if (this.sortPredicate === column) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortPredicate = column;
      this.sortOrder = 'asc';
    }
    this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
  }
}
