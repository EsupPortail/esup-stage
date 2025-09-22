import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { TokenAuthService } from '../services/token-auth.service';

@Injectable({
  providedIn: 'root'
})
export class TokenAuthGuard implements CanActivate {

  constructor(
    private tokenAuthService: TokenAuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> {

    const token = route.queryParams['token'] || route.params['token'];

    // Si pas de token, rediriger vers erreur
    if (!token) {
      console.warn('Accès refusé: Token manquant');
      this.router.navigate(['/error'], {
        queryParams: {
          message: 'Token d\'accès manquant',
          code: 'MISSING_TOKEN'
        }
      });
      return of(false);
    }

    // Valider le token auprès du backend
    return this.tokenAuthService.validateToken(token).pipe(
      map(isValid => {
        if (isValid) {
          console.log('Token validé avec succès');
          return true;
        } else {
          console.warn('Token invalide');
          this.redirectToError('Token invalide ou expiré', 'INVALID_TOKEN');
          return false;
        }
      }),
      catchError(error => {
        console.error('Erreur lors de la validation du token:', error);

        // Gestion des différents codes d'erreur
        let message = 'Erreur de validation du token';
        let code = 'VALIDATION_ERROR';

        if (error.status === 403) {
          message = 'Token invalide ou expiré';
          code = 'FORBIDDEN';
        } else if (error.status === 404) {
          message = 'Service de validation non disponible';
          code = 'SERVICE_UNAVAILABLE';
        }

        this.redirectToError(message, code);
        return of(false);
      })
    );
  }

  private redirectToError(message: string, code: string): void {
    this.router.navigate(['/error'], {
      queryParams: { message, code }
    });
  }
}
