import { Injectable } from '@angular/core';
import { PaginatedService } from "./paginated.service";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { AutocompleteService } from "./autocomplete.service";

@Injectable({
  providedIn: 'root'
})
export class EtapeService implements PaginatedService, AutocompleteService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etapes`, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etapes/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getAutocompleteData(search: string): Observable<any> {
    const filters = {
      search: {
        value: search,
        type: 'text',
        specific: true,
      },
    };
    return this.getPaginated(1, 0, '', '', JSON.stringify(filters));
  }
}
