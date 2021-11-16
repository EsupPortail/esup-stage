import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class CentreGestionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/centre-gestion", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getBrouillonByLogin(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/creation-brouillon`);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/centre-gestion`, data);
  }

  update(data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/centre-gestion`, data);
  }

  getComposantes(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/${id}/composantes`);
  }

  getCentreComposante(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/${id}/composante`);
  }

  setComposante(data: any, id: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/centre-gestion/${id}/set-composante`, data);
  }

  getEtapes(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/${id}/etapes`);
  }

  addEtape(data: any, id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/centre-gestion/${id}/add-etape`, data);
  }

  deleteEtape(codeEtape: string, codeVersion: string): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/centre-gestion/delete-etape/${codeEtape}/${codeVersion}`);
  }

  findByEtape(codeEtape: string, codeVersion: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/by-etape/${codeEtape}/${codeVersion}`);
  }

  getCentreEtapes(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/${id}/centre-etapes`)
  }
}
