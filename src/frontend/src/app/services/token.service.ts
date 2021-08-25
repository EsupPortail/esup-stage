import { Injectable } from '@angular/core';
import { CookieService } from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  constructor(private cookieService: CookieService) { }

  getToken(): string {
    return this.cookieService.get('idsToken');
  }

  logout() {
    this.cookieService.delete('idsToken');
  }
}
