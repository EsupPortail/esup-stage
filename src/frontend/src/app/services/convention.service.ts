import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class ConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/conventions", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getBrouillon(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/brouillon`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/conventions/${id}`, data);
  }

}
