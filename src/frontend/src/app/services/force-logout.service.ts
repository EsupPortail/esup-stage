import { Injectable, OnDestroy } from '@angular/core';
import { environment } from "../../environments/environment";
import { MessageService } from "./message.service";

const FORCE_LOGOUT_KEY = 'forceLogoutByAdmin';
const DEFAULT_MESSAGE = "Votre session a été fermée par un administrateur";

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
    const stored = sessionStorage.getItem(FORCE_LOGOUT_KEY);
    if (stored !== null) {
      sessionStorage.removeItem(FORCE_LOGOUT_KEY);
      this.messageService.setWarning(stored && stored !== '1' ? stored : DEFAULT_MESSAGE);
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

    this.eventSource.addEventListener('force-logout', (event: Event) => {
      const messageEvent = event as MessageEvent<string>;
      this.handleForceLogout(this.extractMessage(messageEvent.data));
    });

    this.eventSource.onerror = () => {
      // Browser-native SSE auto-reconnect handles transient failures.
    };
  }

  private handleForceLogout(message?: string): void {
    this.redirectToRenewLogin(message);
  }

  private extractMessage(rawData: string): string | undefined {
    try {
      const parsed = JSON.parse(rawData);
      return parsed?.message || undefined;
    } catch {
      return undefined;
    }
  }

  /**
   * Redirige vers le login CAS avec renew=1 : le CAS redemande les identifiants même si le
   * SSO est encore actif, pour empêcher une reconnexion silencieuse après une déconnexion forcée.
   */
  redirectToRenewLogin(message?: string): void {
    this.stop();
    sessionStorage.setItem(FORCE_LOGOUT_KEY, message || DEFAULT_MESSAGE);
    const loginUrl = new URL(environment.loginUrl, window.location.href);
    loginUrl.searchParams.set('renew', '1');
    window.location.href = loginUrl.toString();
  }
}
