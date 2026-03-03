import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import {PaginatedResponse, PaginatedService} from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class EtudiantService implements PaginatedService<Etudiant> {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<PaginatedResponse<Etudiant>> {
    return this.http.get<PaginatedResponse<Etudiant>>(environment.apiUrl + "/etudiants", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/etudiants/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getApogeeData(numEtudiant: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etudiants/${numEtudiant}/apogee-data`);
  }

  getApogeeInscriptions(numEtudiant: string, annee: string): Observable<any> {
    const a = annee ? annee.split('/')[0] : '';
    return this.http.get(`${environment.apiUrl}/etudiants/${numEtudiant}/apogee-inscriptions`, {params: {annee: a ?? null} });
  }

  getByLogin(login: string): Observable<Etudiant> {
    return this.http.get<Etudiant>(`${environment.apiUrl}/etudiants/by-login/${login}`);
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.nom} ${row.prenom}`;
  }

  searchEtudiantsDiplomeEtape(filters: EtudiantDiplomeEtapeSearch): Observable<EtudiantDiplomeEtapeResponse[]> {
    return this.http.post<EtudiantDiplomeEtapeResponse[]>(`${environment.apiUrl}/etudiants/diplome-etape`, filters);
  }

}
export type EtudiantDiplomeEtapeSearch = {
  annee: string;
  codeEtape: string;
  versionEtape: string;
  codeDiplome: string;
  versionDiplome: string;
  codEtu?: string;
  nom?: string;
  prenom?: string;
}
export type EtudiantDiplomeEtapeResponse = {
  codEtu: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  numeroIne: string;
  mail: string;
  codeComposante: string;
  libelleComposante: string;
  codeDiplome: string;
  versionDiplome: string;
  codeEtape: string;
  versionEtape: string;
  libelleEtape: string;
  annee: string;
}
export type Etudiant = {
  id: number;
  nom: string;
  prenom: string;
  mail: string;
  codeUniversite: string;
  identEtudiant: string;
  nomMarital: string;
  numEtudiant: string;
  codeSexe: string;
  dateNais: string;
}
