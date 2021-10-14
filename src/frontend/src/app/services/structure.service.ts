import { Injectable } from '@angular/core';
import { PaginatedService } from "./paginated.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class StructureService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/structures`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/structures/${id}`);
  }

}
