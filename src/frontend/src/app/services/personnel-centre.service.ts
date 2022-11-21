import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class PersonnelCentreService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/personnel-centre`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/personnel-centre/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  create(data: any, idCentre: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/personnel-centre/${idCentre}`, data);
  }

  update(data: any, id: number): Observable<any> {
    return this.http.put(`${environment.apiUrl}/personnel-centre/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/personnel-centre/${id}`);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.nom} ${row.prenom}`;
  }
}
