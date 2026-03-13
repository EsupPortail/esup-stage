import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';

interface MaintenanceStatusResponse {
  active?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class MaintenanceGuard {

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  async canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean | UrlTree> {
    const currentUrl = state?.url || '';
    const onMaintenanceRoute = currentUrl.startsWith('/maintenance');

    if (onMaintenanceRoute) {
      return true;
    }

    try {
      const status = await firstValueFrom(
        this.http.get<MaintenanceStatusResponse>(`${environment.apiUrl}/maintenance/status`)
      );

      if (status?.active) {
        return this.router.createUrlTree(['/maintenance']);
      }
    } catch {
      // Fail-open to preserve existing authentication flow when maintenance status is unavailable.
    }

    return true;
  }
}
