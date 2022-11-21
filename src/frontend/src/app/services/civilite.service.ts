import { Injectable } from '@angular/core';
import { PaginatedService } from "./paginated.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class CiviliteService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/civilites`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/civilites/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/civilites/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/civilites/${id}`, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/civilites`, data);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.libelle}`;
  }

}
