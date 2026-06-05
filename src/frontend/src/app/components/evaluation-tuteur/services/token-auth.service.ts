import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import {ConventionEvaluationTuteur} from "../models/convention-evaluation-tuteur.model";
import {environment} from "../../../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class TokenAuthService {


  constructor(private http: HttpClient) {}

  /**
   * Valide un token auprès du backend et récupère les données de convention
   * @param token Le token à valider
   * @returns Observable<ConventionEvaluationTuteurDto> les données si valide, erreur sinon
   */
  validateTokenAndGetConvention(token: string): Observable<ConventionEvaluationTuteur> {
    return this.http.get<ConventionEvaluationTuteur>(environment.apiUrl+`/evaluation-tuteur/access`, {
      params: { token }
    }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Valide uniquement un token (pour le guard)
   * @param token Le token à valider
   * @returns Observable<boolean> true si valide, false sinon
   */
  validateToken(token: string): Observable<boolean> {
    return this.validateTokenAndGetConvention(token).pipe(
      map(() => true),
      catchError((error) => {
        // Pour le guard, on retourne false au lieu de propager l'erreur
        console.error('Token validation failed:', error);
        return of(false);
      })
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

      // Ajouter le message du serveur si disponible
      if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      }
    }

    console.error(errorMessage);
    return throwError(() => error);
  }
}
