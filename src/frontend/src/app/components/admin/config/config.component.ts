import {Component, OnInit,} from '@angular/core';
import { CronService } from '../../../services/cron.service';
import {forkJoin} from "rxjs";
import { MessageService } from '../../../services/message.service';

@Component({
  selector: 'app-config',
  templateUrl: './config.component.html',
  styleUrl: './config.component.scss'
})

// Ce composant est prévu pour la configuration technique de l'application
export class ConfigComponent implements OnInit {

  cronTasks: any[] = [];
  private originalCronTasks: any;

  constructor(private cronService: CronService,private messageService: MessageService) {}

  ngOnInit() {
    this.getPaginated(1, 10, 'id', 'asc', '');
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string) {
    this.cronService.getPaginated(page, perPage, predicate, sortOrder, filters).subscribe((response: any) => {
      this.cronTasks = response.data;
      // Deep clone pour garder l'original
      this.originalCronTasks = JSON.parse(JSON.stringify(response.data));
    });
  }

  resetChanges() {
    // Deep clone pour éviter que les deux tableaux ne pointent vers la même référence
    this.cronTasks = JSON.parse(JSON.stringify(this.originalCronTasks));
  }

  saveAll() {
    // Ne garde que les tâches qui ont été modifiées
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
        // Recharge l'original si tu veux garder le front synchro
        this.getPaginated(1, 10, 'id', 'asc', '');
      },
      error: err => this.messageService.setError('Erreur lors de la sauvegarde'),
    });
  }
}

