import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

// Requêtes d'arrière-plan (état, compteurs, polling) : elles ne doivent pas afficher le loader
// plein écran, qui bloquerait toute la page pendant un dénombrement coûteux.
const SANS_LOADER = { headers: new HttpHeaders({ 'X-No-Loader': 'true' }) };

/**
 * Lancement du nettoyage (contacts ou services inutilisés) et suivi de progression / rapport.
 */
@Injectable({ providedIn: 'root' })
export class NettoyageService {

  constructor(private http: HttpClient) { }

  getResume(): Observable<any> {
    return this.http.get(environment.apiUrl + "/nettoyage/resume", SANS_LOADER);
  }

  /** Compteur d'inutilisés : requête coûteuse, chargée à l'ouverture de l'onglet concerné. */
  getNombreInutilises(type: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/nettoyage/nombre/" + type, SANS_LOADER);
  }

  executer(type: string): Observable<any> {
    return this.http.post(environment.apiUrl + "/nettoyage/executer/" + type, {});
  }

  getProgression(): Observable<any> {
    return this.http.get(environment.apiUrl + "/nettoyage/progression", SANS_LOADER);
  }

  annuler(): Observable<any> {
    return this.http.post(environment.apiUrl + "/nettoyage/annuler", {});
  }

  exportRapport(): Observable<Blob> {
    return this.http.get(environment.apiUrl + "/nettoyage/rapport/export/excel", { responseType: 'blob' });
  }
}

/** Tableau paginé des contacts inutilisés (que le nettoyage supprimerait). */
@Injectable({ providedIn: 'root' })
export class NettoyageContactsService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/nettoyage/simulation/contacts", { params: { page, perPage, predicate, sortOrder, filters } });
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/nettoyage/simulation/contacts/export/${format}`, { params: { headers, predicate, sortOrder, filters }, responseType: 'blob' });
  }

  getMobileTitle(row: any): string {
    return row.nom ? row.nom + ' ' + (row.prenom || '') : String(row.id);
  }
}

/** Tableau paginé des services inutilisés (que le nettoyage supprimerait). */
@Injectable({ providedIn: 'root' })
export class NettoyageServicesService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/nettoyage/simulation/services", { params: { page, perPage, predicate, sortOrder, filters } });
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/nettoyage/simulation/services/export/${format}`, { params: { headers, predicate, sortOrder, filters }, responseType: 'blob' });
  }

  getMobileTitle(row: any): string {
    return row.nom || String(row.id);
  }
}
