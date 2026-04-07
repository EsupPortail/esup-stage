import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable, firstValueFrom, of, EMPTY } from "rxjs";
import { catchError } from "rxjs/operators";
import { TokenService } from "./token.service";
import { Role } from "../constants/role";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  userConnected: any = undefined;
  appVersion: any = undefined;
  private refreshPromise?: Promise<void>;
  private redirecting = false;

  private adminTechList: string[] = [];
  private adminTechLoaded = false;
  private adminTechLoadingPromise?: Promise<void>;

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  getCurrentUser(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected").pipe(
      catchError(() => {
        this.handleUnauthorized();
        return EMPTY;
      })
    );
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
          this.handleUnauthorized();
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

  private handleUnauthorized() {
    if (!this.redirecting) {
      this.redirecting = true;
      const currentPath = window.location.pathname;
      const loginPath = this.resolvePath(environment.loginUrl);
      if (currentPath !== loginPath) {
        sessionStorage.setItem('redirectUrl', currentPath);
        window.location.href = this.resolveUrl(environment.loginUrl);
      }
    }
  }

  logout() {
    this.userConnected = undefined;
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
      console.error('Erreur dans secure :', error);
      return false;
    }
  }

  createUser(user: any) {
    this.userConnected = user;
    this.adminTechLoaded = false;
    this.adminTechLoadingPromise = undefined;
    void this.ensureAdminTechListLoaded();
  }

  checkRights(right: any) {
    if (this.isAdmin()) {
      return true;
    }

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
        console.error('Erreur rafraîchissement user :', error);
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

  private hasAdminRole(): boolean {
    return !!(this.userConnected && this.userConnected.roles.find((r: any) => [Role.ADM].indexOf(r.code) > -1) !== undefined);
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
}
