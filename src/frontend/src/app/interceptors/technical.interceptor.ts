import { Injectable, Injector } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable, ObservableInput } from 'rxjs';
import { TokenService } from "../services/token.service";
import { environment } from "../../environments/environment";
import { catchError, finalize } from "rxjs/operators";
import { MessageService } from "../services/message.service";
import { LoaderService } from "../services/loader.service";
import { AuthService } from "../services/auth.service";

@Injectable()
export class TechnicalInterceptor implements HttpInterceptor {

  private nbRequests: number = 0;
  private currentActiveElement :any;
  private unauthenticatedHandled: boolean = false;

  constructor(private tokenService: TokenService, private messageService: MessageService, private loaderService: LoaderService, private injector: Injector) {
    // Voir AuthService : au retour depuis le bfcache, le drapeau doit repartir de zéro, sinon
    // plus aucune fenêtre ne serait affichée sur les 401 suivants.
    window.addEventListener('pageshow', (event: PageTransitionEvent) => {
      if (event.persisted) {
        this.unauthenticatedHandled = false;
      }
    });
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const handleApogeeForbiddenLocally = request.headers.has('X-Handle-Apogee-Forbidden-Locally');
    // Requêtes de fond (polling...) : elles ne doivent pas afficher le loader plein écran
    const skipLoader = request.headers.has('X-No-Loader');
    let requestToHandle = handleApogeeForbiddenLocally
      ? request.clone({headers: request.headers.delete('X-Handle-Apogee-Forbidden-Locally')})
      : request;
    if (skipLoader) {
      requestToHandle = requestToHandle.clone({headers: requestToHandle.headers.delete('X-No-Loader')});
    }
    if (skipLoader) {
      return next.handle(requestToHandle)
        .pipe(catchError(error => this.handleError(error, requestToHandle, handleApogeeForbiddenLocally)));
    }
    const inputs = ['input', 'select', 'button', 'textarea'];
    if (document.activeElement instanceof HTMLElement && inputs.indexOf(document.activeElement.tagName.toLowerCase()) > -1) {
      this.currentActiveElement = document.activeElement;
      this.currentActiveElement.blur();
    }
    setTimeout(() => {
      this.loaderService.show();
    });
    this.nbRequests++;
    return next.handle(requestToHandle)
      .pipe(
        catchError(error => this.handleError(error, requestToHandle, handleApogeeForbiddenLocally))
      )
      .pipe(
        finalize(() => {
          this.nbRequests--;
          if (this.nbRequests === 0) {
            if (this.currentActiveElement) {
              this.currentActiveElement.focus();
              this.currentActiveElement = undefined;
            }
            setTimeout(() => {
              this.loaderService.hide();
            });
          }
        })
      )
    ;
  }

  handleError(error: any, request?: HttpRequest<unknown>, handleApogeeForbiddenLocally: boolean = false): ObservableInput<any> {
    if (handleApogeeForbiddenLocally && this.isApogeeStudentForbiddenError(error, request)) {
      throw error;
    }
    if (error?.status === 401) {
      this.handleUnauthenticated(error);
      throw error;
    }
    if (error.error instanceof Blob) {
      error.error.text().then((data: any) => {
        const message = JSON.parse(data).message;
        this.messageService.setError(message);
      })
    }
    else if (error.error && error.error.message) {
      this.messageService.setError(error.error.message);
    } else {
      switch (error.status) {
        case 400:
          this.messageService.setError("Données invalides");
          break;
        case 401:
          break;
        case 403:
          this.messageService.setError("Accès interdit");
          break;
        case 404:
          this.messageService.setError("Ressource introuvable");
          break;
        case 500:
          this.messageService.setError("Une erreur technique est survenue");
          break;
        default:
          this.messageService.setError("Une erreur non prévue est survenue");
          break;
      }
    }
    throw error;
  }

  /**
   * Une session expirée fait généralement échouer plusieurs requêtes en parallèle : on n'affiche
   * la fenêtre qu'une seule fois, et sa fermeture (OK, Échap ou clic hors de la fenêtre) renvoie
   * vers la page de connexion plutôt que de laisser l'utilisateur sur un écran inutilisable.
   * Un 401 reçu alors qu'aucune session n'a jamais été ouverte n'est pas une fin de session : il
   * ne donne lieu qu'à une redirection silencieuse.
   */
  private handleUnauthenticated(error: any): void {
    if (this.unauthenticatedHandled) {
      return;
    }
    this.unauthenticatedHandled = true;
    const authService = this.injector.get(AuthService);
    const authReason = error?.headers?.get?.("X-Auth-Reason");
    // Premier accès à l'application : le 401 fait partie du démarrage normal, l'application étant
    // servie sans authentification (/frontend/** est permitAll côté back). On part au CAS sans
    // fenêtre ; celle-ci est réservée à la perte d'une session réellement ouverte. Les deux tests
    // sont volontairement redondants : le motif couvre le cas d'un sessionStorage indisponible, le
    // drapeau celui d'un back non redéployé ou d'un proxy qui filtrerait l'en-tête.
    if (authReason === "no-session" || !authService.hasEstablishedSession()) {
      authService.redirectToLogin();
      return;
    }
    authService.markSessionDialogPending();
    this.messageService.setWarning(this.getUnauthenticatedMessage(authReason), true, () => {
      authService.reconnect();
    });
  }

  private getUnauthenticatedMessage(authReason: string | null | undefined): string {
    switch (authReason) {
      case "idle":
        return "<strong>Votre session a expiré.</strong><br>"
          + "Par mesure de sécurité, vous avez été déconnecté après une période d'inactivité prolongée.<br>"
          + "Cliquez sur OK pour revenir à la page de connexion.";
      case "admin-logout":
        return "<strong>Votre session a été fermée.</strong><br>"
          + "Une nouvelle connexion à votre compte a été détectée, ou un administrateur a mis fin à votre session.<br>"
          + "Cliquez sur OK pour revenir à la page de connexion.";
      default:
        return "<strong>Vous n'êtes plus authentifié.</strong><br>"
          + "Cliquez sur OK pour revenir à la page de connexion.";
    }
  }

  private isApogeeStudentForbiddenError(error: any, request?: HttpRequest<unknown>): boolean {
    const url = request?.url || '';
    return error.status === 403 && (url.includes('/apogee-data') || url.includes('/apogee-inscriptions'));
  }
}
