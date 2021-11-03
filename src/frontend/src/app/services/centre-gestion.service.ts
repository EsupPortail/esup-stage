import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class CentreGestionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/centre-gestion", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getBrouillonByLogin(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/centre-gestion/creation-brouillon`);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/centre-gestion`, data);
  }

  update(data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/centre-gestion`, data);
  }
}
