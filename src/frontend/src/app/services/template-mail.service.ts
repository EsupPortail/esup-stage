import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class TemplateMailService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/template-mails/${id}`, data);
  }

  getParams(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails/params`);
  }

  sendMailTest(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/template-mails/send-test`, data);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.code}`;
  }

  getTemplateMailByType(type: number,idConvention: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-mails/type/${type}?idConvention=${idConvention}`);
  }

  getTemplateMailEvalStage(typeEnvoi: 1 | 2, typeFiche: 0 | 1 | 2) {
    return this.http.get(`${environment.apiUrl}/template-mails/template?type=${typeFiche}&rappel=${typeEnvoi}`);
  }
}
