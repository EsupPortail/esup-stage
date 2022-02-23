import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class EnseignantService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/enseignant", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/enseignant/export/${format}", {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getByUid(uid: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/enseignant/getByUid/${uid}`);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/enseignant", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/enseignant/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/enseignant" + id);
  }
}
