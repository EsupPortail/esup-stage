import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class SecteurActiviteService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/secteur-activites`, {params: {page, perPage, predicate, sortOrder, filters}});
  }
}
