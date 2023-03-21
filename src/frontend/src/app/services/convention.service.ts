import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";
import { PaginatedService } from "./paginated.service";
import { AuthService } from "./auth.service";
import { DatePipe } from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class ConventionService implements PaginatedService {

  constructor(private http: HttpClient, private authService: AuthService, private datePipe: DatePipe) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/conventions", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/conventions/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getBrouillon(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/brouillon`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${id}`);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/conventions/${id}`, data);
  }

  patch(id: number, data: any): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/conventions/${id}`, data);
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions`, data);
  }

  countConventionEnAttenteAlerte(annee: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${annee}/en-attente-validation-alerte`);
  }

  getListAnnee(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/annees`);
  }

  validationCreation(id: number): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/conventions/validation-creation/${id}`, {});
  }

  validationAdministrative(ids: number[]): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/validation-administrative`, {ids});
  }

  validate(idConvention: number, validation: string): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/conventions/${idConvention}/valider/${validation}`, {});
  }

  unvalidate(idConvention: number, validation: string): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/conventions/${idConvention}/devalider/${validation}`, {});
  }

  getHistoriqueValidations(idConvention: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${idConvention}/historique-validations`);
  }

  getConventionPDF(id: number, isRecap : boolean): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${id}/pdf-convention`, { responseType: 'blob', params:{ isRecap}});
  }

  getAvenantPDF(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/conventions/${id}/pdf-avenant`, { responseType: 'blob'});
  }

  deleteConventionBrouillon(): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/conventions/brouillon`);
  }

  goToOnglet: number | null = null;

  setGoToOnglet(onglet:number): void {
    this.goToOnglet = onglet;
  }

  getGoToOnglet(): number | null  {
    const onglet = this.goToOnglet;
    this.goToOnglet = null;
    return onglet;
  }
  deleteConvention(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/conventions/${id}`);
  }

  controleChevauchement(id: number, dateDebut: Date|null, dateFin: Date|null): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/${id}/controle-chevauchement`, {dateDebut, dateFin});
  }

  controleSignatureElectronique(id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/${id}/controle-signature-electronique`, {});
  }

  envoiSignatureElectronique(ids: number[]): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/signature-electronique`, {ids});
  }

  updateSignatureInfo(id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/conventions/${id}/update-signature-electronique-info`, {});
  }

  getMobileTitle(row: any): string {
    return this.authService.isEtudiant() ? `Du ${this.datePipe.transform(row.dateDebutStage, 'shortDate')} au ${this.datePipe.transform(row.dateFinStage, 'shortDate')}` : `${row.id} - ${row.etudiant.nom} ${row.etudiant.prenom}`;
  }

}
