import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";
import { stringify } from "@angular/compiler/src/util";

@Injectable({
  providedIn: 'root'
})
export class TypeConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/type-convention", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  getListActive(): Observable<any> {
    const filters = {
      temEnServ: {value: 'O', type: 'text'},
    };
    return this.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify(filters));
  }

  getListActiveWithTemplate(): Observable<any> {
    const filters = {
      temEnServ: {value: 'O', type: 'text'},
      templatePDF: {value: true, type: 'boolean', specific: true}
    };
    return this.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify(filters));
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/type-convention", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + '/type-convention/' + id, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + '/type-convention/' + id);
  }
}
