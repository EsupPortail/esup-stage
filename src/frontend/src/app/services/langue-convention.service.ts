import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class LangueConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/langue-convention", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/langue-convention/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getListActive(): Observable<any> {
    const filters = {
      temEnServ: {value: 'O', type: 'text'},
    };
    return this.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify(filters));
  }

  getListActiveByTypeConvention(idTypeConvention: number): Observable<any> {
    const filters = {
      temEnServ: {value: 'O', type: 'text'},
      typeConventionTemplate: {value: idTypeConvention, type: 'int', specific: true},
    };
    return this.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify(filters));
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/langue-convention", data);
  }

  update(code: string, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + '/langue-convention/' + code, data);
  }

  delete(code: string): Observable<any> {
    return this.http.delete(environment.apiUrl + '/langue-convention/' + code);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.libelle}`;
  }
}
