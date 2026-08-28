import { Injectable } from '@angular/core';
import { HttpClient, HttpContext } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable, firstValueFrom, of, EMPTY } from "rxjs";
import { catchError, tap } from "rxjs/operators";
import { TokenService } from "./token.service";
import { Role } from "../constants/role";
import { SILENT_REQUEST } from "../interceptors/http-context.tokens";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private static readonly SESSION_ESTABLISHED_KEY = 'sessionEstablished';

  userConnected: any = undefined;
  appVersion: any = undefined;
  private refreshPromise?: Promise<void>;
  private redirecting = false;
  private sessionDialogPending = false;
  private sessionEstablished = false;
  private static readonly CAS_REDIRECT_FLAG = 'casRedirectAttempted';

  private adminTechList: string[] = [];
  private adminTechLoaded = false;
  private adminTechLoadingPromise?: Promise<void>;

  constructor(private http: HttpClient, private tokenService: TokenService) {
    // Restauration depuis le bfcache : le navigateur rend le tas JS tel qu'il était, drapeaux
    // compris. Sans cette remise à zéro, la page reviendrait en paraissant connectée alors que
    // la session est morte, sans plus jamais pouvoir rediriger.
    window.addEventListener('pageshow', (event: PageTransitionEvent) => {
      if (event.persisted) {
        this.redirecting = false;
        this.sessionDialogPending = false;
      }
    });
  }

  getCurrentUser(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected").pipe(
      tap(() => this.markSessionEstablished()),
      catchError((error) => {
        if (error?.status === 401 || error?.status === 403) {
          this.redirectToLogin();
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

  private getAdminTechList(): Observable<string[]> {
    return this.http.get<string[]>(environment.apiUrl + "/users/admintech").pipe(
      catchError((error) => {
        if (error?.status === 401 || error?.status === 403) {
          this.redirectToLogin();
        }
        return of([]);
      })
    );
  }

  private resolvePath(url: string): string {
    return new URL(url, window.location.href).pathname;
  }

  private resolveUrl(url: string): string {
    return new URL(url, window.location.href).toString();
  }

  /**
   * Vrai si une session applicative a déjà été ouverte dans cet onglet. C'est ce qui distingue
   * « ma session vient de mourir » (fenêtre d'avertissement justifiée) de « je n'ai jamais été
   * connecté » : au premier accès l'application démarre sans authentification, /frontend/** étant
   * permitAll, et le 401 du premier appel API fait partie du démarrage normal.
   */
  hasEstablishedSession(): boolean {
    if (this.sessionEstablished) {
      return true;
    }
    try {
      return sessionStorage.getItem(AuthService.SESSION_ESTABLISHED_KEY) === '1';
    } catch {
      return false;
    }
  }

  /**
   * Le drapeau est doublé en sessionStorage pour survivre à un rechargement de l'onglet, sans
   * fuiter vers un autre onglet ni vers une fenêtre de navigation privée.
   */
  private markSessionEstablished(): void {
    this.sessionEstablished = true;
    try {
      sessionStorage.setItem(AuthService.SESSION_ESTABLISHED_KEY, '1');
    } catch {
      // Ignore sessionStorage errors.
    }
  }

  private clearSessionEstablished(): void {
    this.sessionEstablished = false;
    try {
      sessionStorage.removeItem(AuthService.SESSION_ESTABLISHED_KEY);
    } catch {
      // Ignore sessionStorage errors.
    }
  }

  /**
   * Routes ouvertes à des visiteurs sans compte. Le routeur est en mode hash
   * (app-routing.module.ts) : on lit window.location.hash et non Router.url, qui n'est pas encore
   * résolu quand AppComponent.ngOnInit s'exécute.
   */
  isAnonymousPublicRoute(): boolean {
    const route = (window.location.hash || '').replace(/^#\/?/, '').split(/[?;#]/)[0];
    return route === 'evaluation-tuteur' || route.startsWith('evaluation-tuteur/');
  }

  /**
   * Signale qu'une fenêtre « session expirée » est ouverte. Tant qu'elle l'est, les redirections
   * silencieuses déclenchées par les appels en échec sont suspendues : sans ça l'utilisateur est
   * renvoyé vers le CAS avant même d'avoir pu lire le message.
   */
  markSessionDialogPending(): void {
    this.sessionDialogPending = true;
  }

  redirectToLogin() {
    if (this.sessionDialogPending) {
      return;
    }
    this.navigateToLogin(false);
  }

  /**
   * Reconnexion demandée par l'utilisateur depuis la fenêtre de session expirée.
   * Le paramètre renew=1 est relu par le back (SecurityConfiguration#casEntryPoint), qui envoie
   * alors renew=true au CAS. Sans lui, le CAS réutilise sa propre session SSO, réauthentifie
   * l'utilisateur sans rien lui demander et le dépose sur l'accueil.
   */
  reconnect(): void {
    this.sessionDialogPending = false;
    // Reconnexion demandée explicitement : la garde anti-boucle ne doit pas la bloquer.
    sessionStorage.removeItem(AuthService.CAS_REDIRECT_FLAG);
    this.navigateToLogin(true);
  }

  private navigateToLogin(renew: boolean) {
    if (this.isAnonymousPublicRoute() && !this.hasEstablishedSession()) {
      return;
    }
    if (this.redirecting || this.isCasRedirectInProgress()) {
      return;
    }
    this.redirecting = true;
    const currentPath = window.location.pathname;
    const loginPath = this.resolvePath(environment.loginUrl);
    if (currentPath !== loginPath) {
      this.clearSessionEstablished();
      sessionStorage.setItem(AuthService.CAS_REDIRECT_FLAG, '1');
      sessionStorage.setItem('redirectUrl', currentPath);
      const loginUrl = renew ? environment.loginUrl + '?renew=1' : environment.loginUrl;
      this.replaceLocation(this.resolveUrl(loginUrl));
    }
  }

  /**
   * replace() plutôt que href= : la page expirée n'est pas empilée dans l'historique, un retour
   * arrière depuis le CAS ne peut donc plus y ramener. Isolé pour rester observable en test.
   */
  private replaceLocation(url: string): void {
    window.location.replace(url);
  }

  logout() {
    this.userConnected = undefined;
    this.clearSessionEstablished();
    this.adminTechList = [];
    this.adminTechLoaded = false;
    this.adminTechLoadingPromise = undefined;
    this.tokenService.logout();
    window.location.href = this.resolveUrl(environment.logoutUrl);
    sessionStorage.clear();
  }

  async secure(right: any): Promise<boolean> {
    try {
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
    this.adminTechLoaded = false;
    this.adminTechLoadingPromise = undefined;
    void this.ensureAdminTechListLoaded();
    sessionStorage.removeItem(AuthService.CAS_REDIRECT_FLAG);
  }

  checkRights(right: any) {
    if (this.isAdmin()) {
      return true;
    }

    let hasRight = true;
    if (right.fonction && right.droits) {
      hasRight = false;
      this.getEffectiveRoles(right).forEach((r: any) => {
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

  private getEffectiveRoles(right?: any): any[] {
    const globalRoles = this.userConnected?.roles || [];
    if (right?.fonction !== 'PARAM_CENTRE' && right?.fonction !== 'CONVENTION') {
      return globalRoles;
    }

    const centreRoles = (this.userConnected?.centreRoles || [])
      .map((centreRole: any) => centreRole.role)
      .filter((role: any) => !!role);
    return [...globalRoles, ...centreRoles];
  }

  async ensureAdminTechListLoaded(): Promise<void> {
    if (!this.userConnected) {
      return;
    }

    if (this.adminTechLoaded) {
      return;
    }

    if (this.adminTechLoadingPromise) {
      return this.adminTechLoadingPromise;
    }

    this.adminTechLoadingPromise = firstValueFrom(this.getAdminTechList())
      .then((list: string[]) => {
        this.adminTechList = Array.isArray(list) ? list : [];
        this.adminTechLoaded = true;
      })
      .catch(() => {
        this.adminTechList = [];
        this.adminTechLoaded = true;
      })
      .finally(() => {
        this.adminTechLoadingPromise = undefined;
      });

    return this.adminTechLoadingPromise;
  }

  private async ensureFreshUser(): Promise<void> {
    if (this.userConnected !== undefined) {
      await this.ensureAdminTechListLoaded();
      return;
    }

    if (this.refreshPromise) {
      return this.refreshPromise;
    }

    this.refreshPromise = firstValueFrom(this.getCurrentUser())
      .then(async user => {
        if (!user) {
          throw new Error('Utilisateur introuvable');
        }
        this.userConnected = user;
        await this.ensureAdminTechListLoaded();
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
    if (!this.userConnected) {
      return false;
    }
    // Gestionnaire au niveau global OU au niveau d'au moins un centre de gestion
    const globalGestionnaire = (this.userConnected.roles || []).find((r: any) => [Role.GES, Role.RESP_GES].indexOf(r.code) > -1) !== undefined;
    const centreGestionnaire = (this.userConnected.centreRoles || [])
      .some((cr: any) => cr.role && [Role.GES, Role.RESP_GES].indexOf(cr.role.code) > -1);
    return globalGestionnaire || centreGestionnaire;
  }

  isEnseignant(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => [Role.ENS].indexOf(r.code) > -1) !== undefined;
  }

  /**
   * Rôles qui font foi pour un centre de gestion donné : les rôles définis sur ce centre
   * s'ils existent (ils priment sur le rôle global), sinon les rôles globaux.
   */
  getEffectiveRolesForCentre(idCentreGestion: number): any[] {
    const centreRoles = (this.userConnected?.centreRoles || [])
      .filter((cr: any) => cr.idCentreGestion === idCentreGestion)
      .map((cr: any) => cr.role)
      .filter((r: any) => !!r);
    if (centreRoles.length > 0) {
      return centreRoles;
    }
    return this.userConnected?.roles || [];
  }

  /**
   * Vrai si, pour le centre de gestion donné, l'utilisateur n'agit qu'en tant qu'enseignant
   * (rôle enseignant sans rôle gestionnaire), auquel cas il n'a que la validation pédagogique.
   */
  isEnseignantOnlyForCentre(idCentreGestion: number): boolean {
    const roles = this.getEffectiveRolesForCentre(idCentreGestion);
    const hasEnseignant = roles.some((r: any) => r.code === Role.ENS);
    const hasGestionnaire = roles.some((r: any) => [Role.GES, Role.RESP_GES, Role.ADM].indexOf(r.code) > -1);
    return hasEnseignant && !hasGestionnaire;
  }

  /**
   * Vrai si, pour le centre de gestion donné, l'utilisateur est gestionnaire : rôle gestionnaire
   * défini sur ce centre, ou à défaut rôle gestionnaire global.
   */
  isGestionnaireForCentre(idCentreGestion: number): boolean {
    return this.getEffectiveRolesForCentre(idCentreGestion)
      .some((r: any) => [Role.GES, Role.RESP_GES, Role.ADM].indexOf(r.code) > -1);
  }

  private hasAdminRole(): boolean {
    return !!(this.userConnected && (this.userConnected.roles || []).find((r: any) => [Role.ADM].indexOf(r.code) > -1) !== undefined);
  }

  isAdminTech(): boolean {
    if (!this.userConnected?.login) {
      return false;
    }
    return this.adminTechList.includes(this.userConnected.login);
  }

  isAdmin(): boolean {
    return this.hasAdminRole() || this.isAdminTech();
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
