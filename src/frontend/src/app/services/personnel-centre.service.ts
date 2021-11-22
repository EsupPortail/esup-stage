import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class PersonnelCentreService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/personnel-centre`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getDroitsAdmin() {
    return this.http.get(`${environment.apiUrl}/personnel-centre/droits-admin`)
  }
}
