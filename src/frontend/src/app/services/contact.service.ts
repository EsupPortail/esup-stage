import { Injectable } from '@angular/core';
import { PaginatedService } from "./paginated.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ContactService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contacts`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contacts/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contacts/${id}`);
  }

  getByService(id: number, idCentreGestion: any): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contacts/getByService/${id}`, {params: {idCentreGestion}});
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/contacts/${id}`, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/contacts`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/contacts/" + id);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.nom} ${row.prenom}`;
  }
}
