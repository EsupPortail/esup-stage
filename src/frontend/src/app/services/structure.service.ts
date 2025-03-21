import { Injectable } from '@angular/core';
import { PaginatedService } from "./paginated.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { AutocompleteService } from "./autocomplete.service";

@Injectable({
  providedIn: 'root'
})
export class StructureService implements PaginatedService, AutocompleteService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/structures`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/structures/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/structures/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/structures/${id}`, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/structures`, data);
  }

  import(file: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/structures/import", file);
  }

  getAutocompleteData(search: string): Observable<any> {
    const filters = {
      raisonSociale: {
        value: search,
        type: 'text',
      }
    };
    return this.getPaginated(1, 0, '', '', JSON.stringify(filters));
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.raisonSociale}`;
  }

  getOrCreate(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/structures/getOrCreate`, data);
  }

}
