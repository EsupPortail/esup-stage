import { Component, OnDestroy, OnInit } from '@angular/core';
import { CronService } from '../../../services/cron.service';
import { forkJoin, Subject, timer } from 'rxjs';
import { catchError, switchMap, takeUntil } from 'rxjs/operators';
import { of } from 'rxjs';
import { MessageService } from '../../../services/message.service';
import { PageEvent } from '@angular/material/paginator';

@Component({
    selector: 'app-config',
    templateUrl: './tache-planifie.component.html',
    styleUrl: './tache-planifie.component.scss',
    standalone: false
})
export class TachePlanifieComponent implements OnInit, OnDestroy {
  cronTasks: any[] = [];
  private originalCronTasks: any;
  page: number = 1;
  itemsPerPage: number = 10;
  totalTasks: number = 0;
  sortPredicate: string = 'id';
  sortOrder: string = 'asc';
  filtersObj: any = {};
  private filterTimeout: any;

  // État « en cours » partagé, renvoyé par le serveur : visible par tous les utilisateurs
  runningTaskIds: Set<number> = new Set();
  // Tâches lancées depuis cet écran, pour n'afficher le message de fin qu'à celui qui l'a lancée
  private launchedByMe: Set<number> = new Set();
  private readonly destroy$ = new Subject<void>();
  private readonly pollIntervalMs = 4000;

  constructor(private cronService: CronService, private messageService: MessageService) {}

  ngOnInit() {
    this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
    this.startPollingRunning();
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Interroge périodiquement l'état « en cours » du serveur (sans loader plein écran)
  private startPollingRunning() {
    timer(0, this.pollIntervalMs)
      .pipe(
        switchMap(() => this.cronService.getRunning().pipe(catchError(() => of(null)))),
        takeUntil(this.destroy$)
      )
      .subscribe((ids: number[] | null) => {
        if (ids === null) {
          return;
        }
        this.onRunningUpdated(new Set(ids));
      });
  }

  private onRunningUpdated(nouvelEtat: Set<number>) {
    // Tâches qui viennent de se terminer (présentes avant, absentes maintenant)
    const terminees: number[] = [];
    this.runningTaskIds.forEach(id => {
      if (!nouvelEtat.has(id)) {
        terminees.push(id);
      }
    });
    this.runningTaskIds = nouvelEtat;

    if (terminees.length > 0) {
      // Une exécution s'est achevée : on rafraîchit pour afficher la nouvelle date de dernière exécution
      this.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters);
      terminees.forEach(id => {
        if (this.launchedByMe.has(id)) {
          const task = this.cronTasks.find(t => t.id === id);
          this.messageService.setSuccess(`Tâche "${task?.nom ?? id}" terminée`);
          this.launchedByMe.delete(id);
        }
      });
    }
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
    if (this.isExecuting(taskId)) {
      return;
    }

    this.cronService.executeNow(taskId).subscribe({
      next: () => {
        // Marquage optimiste : le polling confirmera et détectera la fin
        this.runningTaskIds.add(taskId);
        this.launchedByMe.add(taskId);
        this.messageService.setSuccess(`Tâche "${taskName}" lancée`);
      },
      error: err => {
        if (err?.status === 409) {
          this.messageService.setWarning(`La tâche "${taskName}" est déjà en cours d'exécution`);
        } else {
          this.messageService.setError(`Erreur lors du lancement de la tâche "${taskName}" : ${err.error?.message || err.message}`);
        }
      }
    });
  }

  isExecuting(taskId: number): boolean {
    return this.runningTaskIds.has(taskId);
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
