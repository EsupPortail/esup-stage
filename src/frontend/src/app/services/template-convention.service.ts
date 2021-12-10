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

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/template-convention/${id}`, data);
  }

  getDefaultTemplateConvention(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-convention`, { responseType: 'text' });
  }

  getDefaultTemplateAvenant(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-avenant`, { responseType: 'text' });
  }
}
