import { Injectable, OnDestroy } from '@angular/core';
import { environment } from "../../environments/environment";
import { MessageService } from "./message.service";

const FORCE_LOGOUT_KEY = 'forceLogoutByAdmin';

/**
 * Écoute le flux SSE des sessions : lorsqu'un administrateur ferme la session de
 * l'utilisateur courant, l'événement "force-logout" est reçu et l'utilisateur est
 * immédiatement redirigé vers la page de login avec un message explicite.
 */
@Injectable({
  providedIn: 'root'
})
export class ForceLogoutService implements OnDestroy {
  private readonly streamUrl = `${environment.apiUrl}/sessions/stream`;

  private eventSource?: EventSource;

  constructor(private messageService: MessageService) {}

  /** Affiche le message de déconnexion forcée mémorisé avant la redirection vers le login. */
  consumePendingMessage(): void {
    if (sessionStorage.getItem(FORCE_LOGOUT_KEY)) {
      sessionStorage.removeItem(FORCE_LOGOUT_KEY);
      this.messageService.setWarning("Votre session a été fermée par un administrateur");
    }
  }

  start(): void {
    this.connectStream();
  }

  stop(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }
  }

  ngOnDestroy(): void {
    this.stop();
  }

  private connectStream(): void {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      return;
    }

    this.eventSource = new EventSource(this.streamUrl, { withCredentials: true });

    this.eventSource.addEventListener('force-logout', () => {
      this.handleForceLogout();
    });

    this.eventSource.onerror = () => {
      // Browser-native SSE auto-reconnect handles transient failures.
    };
  }

  private handleForceLogout(): void {
    this.redirectToRenewLogin();
  }

  /**
   * Redirige vers le login CAS avec renew=1 : le CAS redemande les identifiants même si le
   * SSO est encore actif, pour empêcher une reconnexion silencieuse après une déconnexion forcée.
   */
  redirectToRenewLogin(): void {
    this.stop();
    sessionStorage.setItem(FORCE_LOGOUT_KEY, '1');
    const loginUrl = new URL(environment.loginUrl, window.location.href);
    loginUrl.searchParams.set('renew', '1');
    window.location.href = loginUrl.toString();
  }
}
