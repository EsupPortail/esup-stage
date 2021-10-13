import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";
import { TokenService } from "./token.service";
import { Role } from "../constants/role";
import { AppFonction } from "../constants/app-fonction";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  userConnected: any = undefined;
  appVersion: any = undefined;

  constructor(private http: HttpClient, private tokenService: TokenService) { }

  getCurrentUser(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected");
  }

  getAppVersion(): Observable<any> {
    return this.http.get(environment.apiUrl + "/version", { responseType: 'text' });
  }

  logout() {
    this.userConnected = undefined;
    this.tokenService.logout();
    window.location.href = environment.logoutUrl;
  }

  async secure(right: any) {
    if (this.appVersion === undefined) {
      this.appVersion = await this.getAppVersion().toPromise();
    }

    if (this.userConnected !== undefined) {
      return this.checkRights(right);
    } else {
      let user = await this.getCurrentUser().toPromise();
      this.createUser(user);
      return this.checkRights(right);
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

  isEtudiant(): boolean {
    return this.userConnected && this.userConnected.roles.find((r: any) => r.code === 'ETU') !== undefined;
  }

}
