import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import {Observable, firstValueFrom, of, EMPTY} from "rxjs";
import { catchError } from "rxjs/operators";
import { TokenService } from "./token.service";
import { Role } from "../constants/role";
import { AppFonction } from "../constants/app-fonction";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  userConnected: any = undefined;
  appVersion: any = undefined;
  private refreshPromise?: Promise<void>;
  private redirecting = false;

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

  private handleUnauthorized() {
    if (!this.redirecting) {
      this.redirecting = true;
      const currentPath = window.location.pathname;
      if (currentPath !== '/login/cas') {
        sessionStorage.setItem('redirectUrl', currentPath);
        window.location.href = '/login/cas';
      }
    }
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
      console.error('Erreur dans secure :', error);
      return false;
    }
  }


  createUser(user: any) {
    this.userConnected = user;
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

  isAdmin(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => [Role.ADM].indexOf(r.code) > -1) !== undefined;
  }

  canAccess(roleData: any) {
    if (!roleData || !this.userConnected) {
      return true;
    }
    return this.checkRights(roleData);
  }
}
