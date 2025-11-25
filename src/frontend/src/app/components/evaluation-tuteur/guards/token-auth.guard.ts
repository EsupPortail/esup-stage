import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { TokenAuthService } from '../services/token-auth.service';

@Injectable({ providedIn: 'root' })
export class TokenAuthGuard implements CanActivate {

  constructor(
    private tokenAuthService: TokenAuthService
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {

    const token = route.queryParams['token'] || route.params['token'];

    return this.tokenAuthService.validateToken(token).pipe(
      map(isValid => {
        return isValid;
      }),
      catchError(error => {
        console.error('Erreur lors de la validation du token:', error);

        // LOG des cas typés; pas de navigation
        if (error?.status === 403) {
          console.warn('FORBIDDEN - Token invalide ou expiré');
        } else if (error?.status === 404) {
          console.warn('SERVICE_UNAVAILABLE - Service de validation non disponible');
        }else{
          console.log(error)
        }

        return of(false);
      })
    );
  }
}
