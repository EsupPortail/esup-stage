import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class TemplateMailGroupeService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.code}`;
    }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails-groupe`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails-groupe/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails-groupe/${id}`);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/template-mails-groupe`, data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/template-mails-groupe/${id}`, data);
  }

  getParams(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails/params`);
  }

  sendMailTest(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/template-mails/send-test`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/template-mails-groupe/${id}`);
  }
}
