import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";
import {Etudiant, EtudiantDiplomeEtapeResponse} from "./etudiant.service";

@Injectable({
  providedIn: 'root'
})
export class GroupeEtudiantService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.nom}`;
    }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/groupeEtudiant", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/groupeEtudiant/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getBrouillon(): Observable<GroupeEtudiant> {
    return this.http.get<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/brouillon`);
  }

  getById(id: number): Observable<GroupeEtudiant> {
    return this.http.get<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}`);
  }

  getHistoriqueGroupeMail(id: number): Observable<HistoriqueMailGroupe[]> {
    return this.http.get<HistoriqueMailGroupe[]>(`${environment.apiUrl}/groupeEtudiant/historique/${id}`);
  }

  duplicate(id: number): Observable<GroupeEtudiant> {
    return this.http.get<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/duplicate/${id}`);
  }

  setInfosStageValid(id: number, valid: boolean): Observable<GroupeEtudiant> {
    return this.http.patch<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}/setInfosStageValid/${valid}`, {});
  }

  setTypeConventionGroupe(id: number, typeConventionId: number): Observable<GroupeEtudiant> {
    return this.http.patch<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}/setTypeConventionGroupe/${typeConventionId}`, {});
  }

  validateBrouillon(id: number): Observable<GroupeEtudiant> {
    return this.http.patch<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}/validateBrouillon`, {});
  }

  mergeAndValidateConventions(id: number): Observable<GroupeEtudiant> {
    return this.http.patch<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}/mergeAndValidateConventions`, {});
  }

  create(data: GroupeEtudiantDto): Observable<GroupeEtudiant> {
    return this.http.post<GroupeEtudiant>(environment.apiUrl + "/groupeEtudiant", data);
  }

  update(id: number, data: GroupeEtudiantDto): Observable<GroupeEtudiant> {
    return this.http.put<GroupeEtudiant>(`${environment.apiUrl}/groupeEtudiant/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/groupeEtudiant/" + id);
  }

  import(file: any, id: number): Observable<any> {
    return this.http.post(environment.apiUrl + "/groupeEtudiant/import/" + id, file);
  }

  sendMail(id: number, template: string, data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/groupeEtudiant/sendMail/${id}/templateMail/${template}`,data);
  }

  getConventionPDF(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/groupeEtudiant/pdf-convention`,data , { responseType: 'blob'});
  }
}
export type GroupeEtudiantDto = {
  codeGroupe: string;
  nomGroupe: string;
  etudiantRemovedIds: number[];
  etudiantAdded: EtudiantDiplomeEtapeResponse[];
}
export type GroupeEtudiant = {
  id: number;
  code: string;
  nom: string;
  etudiantGroupeEtudiants: EtudiantGroupeEtudiant[];
  historiqueMailGroupes: HistoriqueMailGroupe[];
  convention: any;
  validationCreation: boolean;
  infosStageValid: boolean;
}
export type EtudiantGroupeEtudiant = {
  id: number;
  etudiant: Etudiant;
  convention: any;
  mergedConvention: any;
}
export type HistoriqueMailGroupe = {
  id: number;
  login: string;
  date: string;
  mailto: string;
}
