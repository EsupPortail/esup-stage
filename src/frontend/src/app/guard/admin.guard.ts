import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from "../services/auth.service";

@Injectable({
  providedIn: 'root'
})
export class AdminGuard {

  constructor(private authService: AuthService, private router: Router) {}

  async canActivate(): Promise<boolean | UrlTree> {
    // AuthGuard est passé avant : l'utilisateur connecté et la liste admin tech sont chargés
    await this.authService.ensureAdminTechListLoaded();
    if (this.authService.isAdmin()) {
      return true;
    }
    return this.router.parseUrl('/');
  }
}
