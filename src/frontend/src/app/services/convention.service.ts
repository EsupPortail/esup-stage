import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class ConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/conventions", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getBrouillon(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/brouillon`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/conventions/${id}`, data);
  }

  patch(id: number, data: any): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/conventions/${id}`, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions`, data);
  }

  countConventionEnAttente(annee: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${annee}/en-attente-validation`);
  }

  getListAnnee(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/annees`);
  }

  validationAdministrative(ids: number[]): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/validation-administrative`, {ids});
  }

}
