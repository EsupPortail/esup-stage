import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class EtudiantGroupeEtudiantService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/etudiantGroupeEtudiant", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/etudiantGroupeEtudiant/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getMobileTitle(row: any): string {
    const id = row?.convention?.id ?? '';
    const prenom = row?.etudiant?.prenom ?? '';
    const nom = row?.etudiant?.nom ?? '';
    const fullName = `${prenom} ${nom}`.trim();
    if (id && fullName) {
      return `${id} - ${fullName}`;
    }
    return fullName || `${id}` || '';
  }

}
