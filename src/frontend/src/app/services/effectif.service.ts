import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class EffectifService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/effectifs`);
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/effectifs/search", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/effectifs", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + '/effectifs/' + id, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + '/effectifs/' + id);
  }
}
