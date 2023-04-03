import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class GroupeEtudiantService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.nom}`;
    }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/groupeEtudiant", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/groupeEtudiant/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getBrouillon(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/groupeEtudiant/brouillon`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/groupeEtudiant/${id}`);
  }

  getHistoriqueGroupeMail(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/groupeEtudiant/historique/${id}`);
  }

  duplicate(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/groupeEtudiant/duplicate/${id}`);
  }

  setInfosStageValid(id: number, valid: boolean): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/groupeEtudiant/${id}/setInfosStageValid/${valid}`, {});
  }

  setTypeConventionGroupe(id: number, typeConventionId: number): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/groupeEtudiant/${id}/setTypeConventionGroupe/${typeConventionId}`, {});
  }

  validateBrouillon(id: number): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/groupeEtudiant/${id}/validateBrouillon`, {});
  }

  mergeAndValidateConventions(id: number): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/groupeEtudiant/${id}/mergeAndValidateConventions`, {});
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/groupeEtudiant", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/groupeEtudiant/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/groupeEtudiant/" + id);
  }

  import(file: any, id: number): Observable<any> {
    return this.http.post(environment.apiUrl + "/groupeEtudiant/import/" + id, file);
  }

  sendMail(id: number, template: string, data: any): Observable<any> {
    console.log("DATA => ");
    console.log(data);
    
    return this.http.post(`${environment.apiUrl}/groupeEtudiant/sendMail/${id}/templateMail/${template}`,data);
  }

  getConventionPDF(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/groupeEtudiant/pdf-convention`,data , { responseType: 'blob'});
  }
}
