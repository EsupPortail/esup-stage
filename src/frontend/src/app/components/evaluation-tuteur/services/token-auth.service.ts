import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class TokenAuthService {

  private baseUrl = '/api'; // Ajustez selon votre configuration

  constructor(private http: HttpClient) {}

  /**
   * Valide un token auprès du backend
   * @param token Le token à valider
   * @returns Observable<boolean> true si valide, erreur sinon
   */
  validateToken(token: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.baseUrl}/evaluation-tuteur/access`, {
      params: { token }
    }).pipe(
      catchError(this.handleError)
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Erreur de validation du token';

    if (error.error instanceof ErrorEvent) {
      // Erreur côté client
      errorMessage = `Erreur client: ${error.error.message}`;
    } else {
      // Erreur côté serveur
      errorMessage = `Code d'erreur: ${error.status}, Message: ${error.message}`;
    }

    console.error(errorMessage);
    return throwError(() => error);
  }
}
