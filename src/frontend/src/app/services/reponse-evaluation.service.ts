import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class ReponseEvaluationService {

  constructor(private http: HttpClient) { }

  getByConvention(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/reponseEvaluation/getByConvention/${id}`);
  }

  createReponseEtudiant(id: number, data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/reponseEvaluation/etudiant/${id}`, data);
  }

  updateReponseEtudiant(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/reponseEvaluation/etudiant/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/reponseEvaluation" + id);
  }

  getReponseSupplementaire(idConvention: number,idQestion: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/reponseEvaluation/${idConvention}/reponseSupplementaire/${idQestion}`)
  }

  createReponseSupplementaire(idConvention: number,idQestion: number, data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/reponseEvaluation/${idConvention}/reponseSupplementaire/${idQestion}`, data);
  }

  updateReponseSupplementaire(idConvention: number,idQestion: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/reponseEvaluation/${idConvention}/reponseSupplementaire/${idQestion}`, data);
  }

  getFicheEtudiantPDF(id: number,typeFiche: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/reponseEvaluation/${id}/getFicheEtudiantPDF/typeFiche/${typeFiche}`, { responseType: 'blob'});
  }

  sendMailEvaluation(id: number,typeFiche: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/reponseEvaluation/${id}/sendMailEvaluation/typeFiche/${typeFiche}`);
  }
}
