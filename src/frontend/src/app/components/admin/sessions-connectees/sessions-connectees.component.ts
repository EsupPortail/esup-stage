import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription, interval } from "rxjs";
import { startWith, switchMap } from "rxjs/operators";
import { ActiveSession, AdminSessionService } from "../../../services/admin-session.service";
import { MessageService } from "../../../services/message.service";

const REFRESH_INTERVAL_MS = 20000;

@Component({
  selector: 'app-sessions-connectees',
  templateUrl: './sessions-connectees.component.html',
  styleUrls: ['./sessions-connectees.component.scss'],
  standalone: false
})
export class SessionsConnecteesComponent implements OnInit, OnDestroy {

  columns = ['login', 'nom', 'prenom', 'roles', 'lastRequest', 'action'];
  sessions: ActiveSession[] = [];
  loading = true;

  private refreshSubscription?: Subscription;

  constructor(
    private adminSessionService: AdminSessionService,
    private messageService: MessageService
  ) {}

  ngOnInit(): void {
    this.refreshSubscription = interval(REFRESH_INTERVAL_MS).pipe(
      startWith(0),
      switchMap(() => this.adminSessionService.getSessions())
    ).subscribe({
      next: (sessions: ActiveSession[]) => {
        this.sessions = sessions;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    this.refreshSubscription?.unsubscribe();
  }

  refresh(): void {
    this.adminSessionService.getSessions().subscribe((sessions: ActiveSession[]) => {
      this.sessions = sessions;
      this.loading = false;
    });
  }

  closeSession(row: ActiveSession): void {
    this.adminSessionService.closeSession(row.sessionId).subscribe(() => {
      this.messageService.setSuccess(`La session de ${row.prenom || ''} ${row.nom || row.login} a été fermée`);
      this.refresh();
    });
  }

  formatRoles(row: ActiveSession): string {
    return row.roles && row.roles.length > 0 ? row.roles.join(', ') : '-';
  }
}
