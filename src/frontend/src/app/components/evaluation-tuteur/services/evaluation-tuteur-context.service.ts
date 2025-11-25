import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ConventionEvaluationTuteur } from '../models/convention-evaluation-tuteur.model';

/**
 * Service de contexte pour l'évaluation tuteur.
 *
 * - Expose des flux (Observables) pour le `token` et la `convention`.
 * - Permet de pousser de nouvelles valeurs via des setters dédiés.
 * - Utilise `BehaviorSubject` afin que les nouveaux abonnés reçoivent
 *   immédiatement la dernière valeur émise (ou `null` au démarrage).
 */
@Injectable({ providedIn: 'root' })
export class EvaluationTuteurContextService {
  /**
   * Sujet interne qui conserve la dernière valeur du token.
   */
  private tokenSubject = new BehaviorSubject<string | null>(null);

  /**
   * Flux public (lecture seule) du token.
   */
  readonly token$ = this.tokenSubject.asObservable();

  /**
   * Sujet interne qui conserve la dernière convention d'évaluation du tuteur.
   */
  private conventionSubject = new BehaviorSubject<ConventionEvaluationTuteur | null>(null);

  /**
   * Flux public (lecture seule) de la convention.
   */
  readonly convention$ = this.conventionSubject.asObservable();

  /**
   * Met à jour le token courant.
   * @param t Nouveau token (ou `null` pour réinitialiser).
   */
  setToken(t: string | null): void {
    this.tokenSubject.next(t);
  }

  /**
   * Met à jour la convention courante.
   * @param c Nouvelle convention (ou `null` pour réinitialiser).
   */
  setConvention(c: ConventionEvaluationTuteur | null): void {
    this.conventionSubject.next(c);
  }
}
