import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ContenuService implements PaginatedService {

  contenus: any;

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contenus`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contenus/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  async getAllLibelle(forceReload: boolean|undefined = undefined) {
    if (this.contenus === undefined || forceReload) {
      this.contenus = await this.http.get(`${environment.apiUrl}/contenus/libelle`).toPromise();
    }
  }

  update(code: string, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/contenus/${code}`, data);
  }

  get(code: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/contenus/${code}`);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.libelle}`;
  }
}
