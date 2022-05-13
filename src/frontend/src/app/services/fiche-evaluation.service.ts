import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class FicheEvaluationService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/ficheEvaluation", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getByCentreGestion(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/ficheEvaluation/getByCentreGestion/${id}`);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/ficheEvaluation/" + id);
  }

  saveAndValidateFicheEtudiant(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/ficheEvaluation/saveAndValidateFicheEtudiant/${id}`, data);
  }

  saveAndValidateFicheEnseignant(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/ficheEvaluation/saveAndValidateFicheEnseignant/${id}`, data);
  }

  saveAndValidateFicheEntreprise(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/ficheEvaluation/saveAndValidateFicheEntreprise/${id}`, data);
  }

  getQuestionsSupplementaires(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/ficheEvaluation/getQuestionsSupplementaires/${id}`);
  }

  addQuestionSupplementaire(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/ficheEvaluation/addQuestionSupplementaire/${id}`, data);
  }

  editQuestionSupplementaire(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/ficheEvaluation/editQuestionSupplementaire/${id}`, data);
  }

  deleteQuestionSupplementaire(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/ficheEvaluation/deleteQuestionSupplementaire/" + id);
  }
}
