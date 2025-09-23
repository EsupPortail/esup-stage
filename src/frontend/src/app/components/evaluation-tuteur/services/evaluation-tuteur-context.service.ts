import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {ConventionEvaluationTuteur} from "../models/convention-evaluation-tuteur.model";

@Injectable({ providedIn: 'root' })
export class EvaluationTuteurContextService {
  private tokenSubject = new BehaviorSubject<string | null>(null);
  token$ = this.tokenSubject.asObservable();

  private conventionSubject = new BehaviorSubject<ConventionEvaluationTuteur | null>(null);
  convention$ = this.conventionSubject.asObservable();

  setToken(t: string | null) { this.tokenSubject.next(t); }
  setConvention(c: ConventionEvaluationTuteur | null) { this.conventionSubject.next(c); }
}
