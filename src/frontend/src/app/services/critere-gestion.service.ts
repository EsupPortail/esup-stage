import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class CritereGestionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/critere-gestion/centre-etapes-paginated`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/critere-gestion/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.libelle}`;
  }
}
