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

  constructor(private cronService: CronService,private messageService: MessageService) {}

  ngOnInit() {
    this.getPaginated(1, 10, 'id', 'asc', '');
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string) {
    this.cronService.getPaginated(page, perPage, predicate, sortOrder, filters).subscribe((response: any) => {
      this.cronTasks = response.data;
      console.log(this.cronTasks)
    });
  }

  saveAll() {
    const requests = this.cronTasks.map(task =>
      this.cronService.update(task.id, task)
    );

    forkJoin(requests).subscribe({
      next: () => this.messageService.setSuccess('Sauvegarde réussie'),
      error: err => this.messageService.setError('Erreur lors de la sauvegarde'),
    });
  }
}

