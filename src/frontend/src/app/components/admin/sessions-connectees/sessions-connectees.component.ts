import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
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
  closeAllMessage = '';

  @ViewChild('closeAllDialog') closeAllDialog!: TemplateRef<any>;
  private closeAllDialogRef?: MatDialogRef<any>;

  private refreshSubscription?: Subscription;

  constructor(
    private adminSessionService: AdminSessionService,
    private messageService: MessageService,
    private dialog: MatDialog
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

  get userCount(): number {
    return new Set(this.sessions.map((s: ActiveSession) => s.login)).size;
  }

  openCloseAllDialog(): void {
    this.closeAllMessage = '';
    this.closeAllDialogRef = this.dialog.open(this.closeAllDialog, { width: '500px' });
  }

  confirmCloseAll(): void {
    this.adminSessionService.closeAllSessions(this.closeAllMessage.trim()).subscribe((closed: number) => {
      this.messageService.setSuccess(closed > 1 ? `${closed} sessions fermées` : `${closed} session fermée`);
      this.closeAllDialogRef?.close();
      this.refresh();
    });
  }
}
