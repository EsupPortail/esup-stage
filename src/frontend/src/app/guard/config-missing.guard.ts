import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { firstValueFrom } from "rxjs";
import { AuthService } from "../services/auth.service";
import { ConfigMissingService } from "../services/config-missing.service";

@Injectable({
  providedIn: 'root'
})
export class ConfigMissingGuard {

  constructor(
    private authService: AuthService,
    private configMissingService: ConfigMissingService,
    private router: Router
  ) {}

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean | UrlTree> {
    try {
      if (!this.authService.userConnected) {
        const user = await firstValueFrom(this.authService.getCurrentUser());
        if (user) {
          this.authService.createUser(user);
        }
      }

      const isAdmin = this.authService.isAdmin();

      // Si l'utilisateur n'est pas admin, laisser passer
      if (!isAdmin) {
        console.log('[ConfigMissingGuard] Utilisateur non admin, accès autorisé');
        return true;
      }

      // L'utilisateur est admin, vérifier la configuration
      const response = await firstValueFrom(this.configMissingService.getMissing());
      const missing = response?.missing || [];

      if (missing.length === 0) {
        console.log('[ConfigMissingGuard] Config complète, accès autorisé');
        return true;
      }

      // Config manquante et utilisateur est admin
      console.log('[ConfigMissingGuard] Config manquante:', missing);
      const onMissingPage = state.url.includes('admin/config-missing');

      if (onMissingPage) {
        console.log('[ConfigMissingGuard] Déjà sur la page config-missing, accès autorisé');
        return true;
      } else {
        console.log('[ConfigMissingGuard] Redirection vers /admin/config-missing depuis', state.url);
        return this.router.createUrlTree(['/admin/config-missing']);
      }
    } catch (e) {
      console.error('[ConfigMissingGuard] Erreur:', e);
      return true;
    }
  }
}
