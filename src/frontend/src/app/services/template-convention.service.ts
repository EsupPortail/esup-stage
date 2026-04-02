import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class TemplateConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/template-convention`, data);
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/template-convention/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/template-convention/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/template-convention/${id}`);
  }

  getDefaultTemplateConvention(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-convention`, { responseType: 'text' });
  }

  getDefaultTemplateAvenant(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-avenant`, { responseType: 'text' });
  }

  getMobileTitle(row: any): string {
    return `${row.typeConvention.libelle} - ${row.langueConvention.libelle}`;
  }

  getAll(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/all`);
  }
}
