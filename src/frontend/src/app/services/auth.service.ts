import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  userConnected: any = undefined;

  constructor(private http: HttpClient) { }

  getCurrentUser(): Observable<any> {
    return this.http.get(environment.apiUrl + "/users/connected");
  }

  async secure(rights: any) {
    if (this.userConnected != undefined) {
      return this.checkRights(rights);
    }
    else {
      let user = await this.getCurrentUser().toPromise();
      this.createUser(user);
      return this.checkRights(rights);
    }
  }

  createUser(user: any) {
    this.userConnected = user;
  }

  checkRights(rights: any) {
    let hasRight = true;
    if (Array.isArray(rights) && rights.length > 0) {
      hasRight = false;
      rights.forEach(right => {
        if (right == this.userConnected.role) {
          hasRight = true;
        }
      });
    }
    return hasRight;
  }
}
