import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class EtudiantService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/etudiants", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/etudiants/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getApogeeData(numEtudiant: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etudiants/${numEtudiant}/apogee-data`);
  }

  getApogeeInscriptions(numEtudiant: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etudiants/${numEtudiant}/apogee-inscriptions`);
  }

  getByLogin(login: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etudiants/by-login/${login}`);
  }

}
