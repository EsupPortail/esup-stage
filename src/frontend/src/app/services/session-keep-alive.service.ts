import { Injectable, NgZone, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { MessageService } from './message.service';

/**
 * Options de configuration d'une session de keep-alive.
 */
export interface KeepAliveOptions {
  /** Intervalle entre deux tentatives de ping (ms). Doit rester bien en dessous
   *  du server.servlet.session.timeout. Défaut : 4 min. */
  pingIntervalMs?: number;
  /** Durée d'inactivité au-delà de laquelle on cesse de pinger (ms). Un onglet
   *  abandonné laisse ainsi la session expirer. Défaut : identique à l'intervalle. */
  activityWindowMs?: number;
}

/**
 * Maintient vivante la session HTTP côté serveur pendant qu'un écran d'édition
 * longue (CKEditor, etc.) est ouvert ET que l'utilisateur est réellement actif.
 *
 * Problème résolu : pendant la rédaction, aucune requête n'est émise ; si la
 * durée dépasse le timeout de session, le « Valider » final renvoie un 401 et le
 * travail est perdu. Le keep-alive pingue périodiquement un endpoint authentifié
 * léger (GET /api/users/connected) pour rafraîchir le lastAccessedTime.
 *
 * Le ping n'est émis que si l'utilisateur a interagi récemment (clavier, souris,
 * molette, tactile) : un onglet laissé à l'abandon cesse d'être pingé et la
 * session finit par expirer normalement (le logout d'inactivité est préservé).
 *
 * Réutilisable : injecter le service, appeler {@link start} en entrant dans
 * l'écran d'édition et {@link stop} en le quittant (ngOnDestroy, changement
 * d'onglet, etc.).
 */
@Injectable({
  providedIn: 'root'
})
export class SessionKeepAliveService implements OnDestroy {

  private static readonly DEFAULT_PING_INTERVAL_MS = 4 * 60 * 1000;

  /** Évènements DOM considérés comme une activité utilisateur réelle.
   *  La saisie dans CKEditor remonte jusqu'au document via keydown. */
  private static readonly ACTIVITY_EVENTS = ['keydown', 'mousedown', 'mousemove', 'wheel', 'touchstart'];

  private intervalId: any = null;
  private pingSub?: Subscription;
  private lastActivityAt = 0;
  private pingIntervalMs = SessionKeepAliveService.DEFAULT_PING_INTERVAL_MS;
  private activityWindowMs = SessionKeepAliveService.DEFAULT_PING_INTERVAL_MS;
  private expiredNotified = false;

  private readonly activityListener = (): void => {
    this.lastActivityAt = Date.now();
  };

  constructor(
    private authService: AuthService,
    private messageService: MessageService,
    private ngZone: NgZone,
  ) { }

  ngOnDestroy(): void {
    this.stop();
  }

  isRunning(): boolean {
    return this.intervalId !== null;
  }

  /**
   * Démarre le keep-alive. Idempotent : un appel alors que le service tourne
   * déjà est ignoré.
   */
  start(options: KeepAliveOptions = {}): void {
    if (this.isRunning()) {
      return;
    }
    this.pingIntervalMs = options.pingIntervalMs ?? SessionKeepAliveService.DEFAULT_PING_INTERVAL_MS;
    this.activityWindowMs = options.activityWindowMs ?? this.pingIntervalMs;
    this.expiredNotified = false;
    // Entrer dans l'écran d'édition compte comme une activité.
    this.lastActivityAt = Date.now();

    // Hors zone Angular : les évènements de souris/clavier et le timer ne doivent
    // pas déclencher de détection de changement à chaque frappe/déplacement.
    this.ngZone.runOutsideAngular(() => {
      SessionKeepAliveService.ACTIVITY_EVENTS.forEach(event => {
        window.addEventListener(event, this.activityListener, { passive: true });
      });
      this.intervalId = setInterval(() => this.tick(), this.pingIntervalMs);
    });
  }

  /**
   * Arrête le keep-alive et nettoie timer, écouteurs et souscription en cours.
   * Sûr à appeler même si le service n'a jamais démarré (idempotent).
   */
  stop(): void {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
    SessionKeepAliveService.ACTIVITY_EVENTS.forEach(event => {
      window.removeEventListener(event, this.activityListener);
    });
    this.pingSub?.unsubscribe();
    this.pingSub = undefined;
  }

  private tick(): void {
    // Pas d'activité récente : on laisse la session expirer (onglet abandonné).
    if (Date.now() - this.lastActivityAt > this.activityWindowMs) {
      return;
    }
    this.pingSub?.unsubscribe();
    this.pingSub = this.authService.pingSession().subscribe({
      error: (err: any) => this.handlePingError(err),
    });
  }

  private handlePingError(err: any): void {
    // Erreur réseau transitoire : on réessaiera au prochain tick.
    if (err?.status !== 401 && err?.status !== 403) {
      return;
    }
    // Une redirection CAS est déjà gérée ailleurs : ne pas interférer.
    if (this.authService.isCasRedirectInProgress()) {
      return;
    }
    if (this.expiredNotified) {
      return;
    }
    this.expiredNotified = true;
    // Session expirée côté serveur : inutile de continuer à pinger. On prévient
    // l'utilisateur SANS vider l'éditeur, pour qu'il puisse se reconnecter et
    // sauvegarder son travail.
    this.stop();
    this.ngZone.run(() => {
      this.messageService.setWarning(
        'Votre session a expiré. Reconnectez-vous (dans un autre onglet) avant de réessayer d’enregistrer : votre travail en cours n’a pas été perdu.'
      );
    });
  }
}
