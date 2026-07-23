import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from "@angular/common/http";
import { environment } from "../../environments/environment";
import {Observable, firstValueFrom, of, EMPTY} from "rxjs";
import { catchError } from "rxjs/operators";
import { TokenService } from "./token.service";
import { Role } from "../constants/role";
import { SILENT_REQUEST } from "../interceptors/http-context.tokens";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  userConnected: any = undefined;
  appVersion: any = undefined;
  private refreshPromise?: Promise<void>;
  // Marqueur persistant (survit au rechargement provoqué par la redirection) empêchant
  // toute boucle de redirection vers CAS.
  private static readonly CAS_REDIRECT_FLAG = 'casRedirectAttempted';

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  getCurrentUser(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected").pipe(
      catchError((error) => {
        if (error?.status === 401) {
          this.handleUnauthorized();
        }
        return EMPTY;
      })
    );
  }

  /**
   * Ping léger et silencieux d'un endpoint authentifié.
   *
   * Sert au keep-alive : l'appel rafraîchit le lastAccessedTime de la session
   * HTTP côté serveur sans afficher de loader ni de popup d'erreur (voir
   * {@link SILENT_REQUEST}). Ne met volontairement pas à jour userConnected.
   */
  pingSession(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected", {
      context: new HttpContext().set(SILENT_REQUEST, true),
    });
  }

  /**
   * Indique qu'une redirection CAS est déjà en cours (garde anti-boucle).
   * S'appuie sur le marqueur posé par {@link handleUnauthorized} (valeur '1').
   */
  isCasRedirectInProgress(): boolean {
    return sessionStorage.getItem(AuthService.CAS_REDIRECT_FLAG) === '1';
  }

  getAppVersion(): Observable<any> {
    return this.http.get(environment.apiUrl + "/version", { responseType: 'text' }).pipe(
      catchError(() => EMPTY)
    );
  }

  private handleUnauthorized() {
    const currentPath = window.location.pathname;
    if (currentPath === '/login/cas') {
      return;
    }
    if (sessionStorage.getItem(AuthService.CAS_REDIRECT_FLAG) === '1') {
      return;
    }
    sessionStorage.setItem(AuthService.CAS_REDIRECT_FLAG, '1');
    sessionStorage.setItem('redirectUrl', currentPath);
    window.location.href = '/login/cas';
  }

  logout() {
    this.userConnected = undefined;
    this.tokenService.logout();
    window.location.href = environment.logoutUrl;
    sessionStorage.clear();
  }

  async secure(right: any): Promise<boolean> {
    try {
      // Chargement lazy de la version de l'app
      if (this.appVersion === undefined) {
        this.appVersion = await firstValueFrom(this.getAppVersion());
      }

      await this.ensureFreshUser();

      return this.checkRights(right);
    } catch (error) {
      this.logError('Erreur dans secure', error);
      return false;
    }
  }


  createUser(user: any) {
    this.userConnected = user;
    sessionStorage.removeItem(AuthService.CAS_REDIRECT_FLAG);
  }

  checkRights(right: any) {
    let hasRight = true;
    if (right.fonction && right.droits) {
      hasRight = false;
      this.userConnected.roles.forEach((r: any) => {
        r.roleAppFonctions.forEach((ha: any) => {
          if (ha.appFonction.code === right.fonction) {
            right.droits.forEach((d: any) => {
              if (ha[d.toLowerCase()]) {
                hasRight = true;
              }
            });
          }
        });
      });
    }
    return hasRight;
  }

  private async ensureFreshUser(): Promise<void> {
    if (this.userConnected !== undefined) {
      return;
    }

    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = firstValueFrom(this.getCurrentUser())
      .then(user => {
        if (!user) {
          throw new Error('Utilisateur introuvable');
        }
        this.createUser(user);
      })
      .catch(error => {
        this.logError('Erreur rafraichissement user', error);
        throw error;
      })
      .finally(() => {
        this.refreshPromise = undefined;
      });

    return this.refreshPromise;
  }

  getUserConnectedLogin(): string {
    return this.userConnected.login;
  }

  isEtudiant(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => r.code === Role.ETU) !== undefined;
  }

  isGestionnaire(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => [Role.GES, Role.RESP_GES].indexOf(r.code) > -1) !== undefined;
  }

  isEnseignant(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => [Role.ENS].indexOf(r.code) > -1) !== undefined;
  }

  isAdmin(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => [Role.ADM].indexOf(r.code) > -1) !== undefined;
  }

  canAccess(roleData: any) {
    if (!roleData || !this.userConnected) {
      return true;
    }
    return this.checkRights(roleData);
  }

  private logError(message: string, error?: unknown): void {
    const args: unknown[] = [message];
    if (!environment.production && error !== undefined) {
      args.push(error);
    }
    console.error(...args);
  }
}
