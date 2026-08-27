import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

// Requêtes d'arrière-plan (statistiques, résumé, polling de progression) : pas de loader
// plein écran, qui bloquerait la page pendant des dénombrements coûteux.
const SANS_LOADER = { headers: new HttpHeaders({ 'X-No-Loader': 'true' }) };

@Injectable({
  providedIn: 'root'
})
export class ArchivageService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getStatistiques(): Observable<any> {
    return this.http.get(environment.apiUrl + "/archivage/statistiques", SANS_LOADER);
  }

  getSimulationResume(): Observable<any> {
    return this.http.get(environment.apiUrl + "/archivage/simulation/resume", SANS_LOADER);
  }

  executer(type: string): Observable<any> {
    return this.http.post(environment.apiUrl + "/archivage/executer/" + type, {});
  }

  getProgression(): Observable<any> {
    return this.http.get(environment.apiUrl + "/archivage/progression", SANS_LOADER);
  }

  annuler(): Observable<any> {
    return this.http.post(environment.apiUrl + "/archivage/annuler", {});
  }

  exportRapport(): Observable<Blob> {
    return this.http.get(environment.apiUrl + "/archivage/rapport/export/excel", {responseType: 'blob'});
  }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/archivage/simulation", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/archivage/simulation/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getMobileTitle(row: any): string {
    return row.etudiant ? row.etudiant.nom + ' ' + row.etudiant.prenom : String(row.id);
  }

}
