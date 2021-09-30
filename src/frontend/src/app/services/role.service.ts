import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class RoleService implements PaginatedService {

  constructor(private http: HttpClient) { }

  findAll(): Observable<any> {
    return this.getPaginated(1, 0, 'libelle', 'asc', '{}');
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/roles", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  findById(id: number): Observable<any> {
    return this.http.get(environment.apiUrl + "/roles/" + id);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/roles", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + "/roles/" + id, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/roles/" + id);
  }

  findAllAppFonction() {
    return this.http.get(environment.apiUrl + "/roles/appFonctions");
  }

}
