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

  getApogeeEtapes(codeAnnee: string, codeComposante: string): Observable<DiplomeEtape[]> {
    return this.http.get<DiplomeEtape[]>(`${environment.apiUrl}/etapes/apogee`, { params: {codeAnnee, codeComposante} });
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

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.libelle}`;
  }
}
export type EtapeV2Apogee = {
  codeEtp: string;
  codVrsVet: string;
  libWebVet: string;
};
export type DiplomeEtape = {
  codeDiplome: string;
  versionDiplome: string;
  libDiplome: string;
  listeEtapes: EtapeV2Apogee[];
};
