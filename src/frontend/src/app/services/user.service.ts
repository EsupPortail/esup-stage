import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class UserService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/users", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/users/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + '/users/' + id, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + '/users', data);
  }

  findOneByLogin(login: string) {
    return this.http.get(environment.apiUrl + '/users/' + login);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.login}`;
  }
}
