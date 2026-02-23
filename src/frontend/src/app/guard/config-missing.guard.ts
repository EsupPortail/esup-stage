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
      if (!isAdmin) {
        return true;
      }

      const response = await firstValueFrom(this.configMissingService.getMissing());
      const missing = response?.missing || [];

      if (missing.length === 0) {
        return true;
      }

      const onMissingPage = state.url.includes('admin/config-missing');
      if (isAdmin) {
        return onMissingPage ? true : this.router.createUrlTree(['/admin/config-missing']);
      }

      return false;
    } catch (e) {
      return true;
    }
  }
}
