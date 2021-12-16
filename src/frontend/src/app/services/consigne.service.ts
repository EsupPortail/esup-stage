import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ConsigneService {

  constructor(private http: HttpClient) { }

  getConsigneByCentre(idCentreGestion: number|null): Observable<any> {
    return this.http.get(`${environment.apiUrl}/consignes/${idCentreGestion ? 'centres/' + idCentreGestion : 'etablissement'}`);
  }

  updateConsigne(idConsigne: number, data: any): Observable<any> {
    return this.http.patch(`${environment.apiUrl}/consignes/${idConsigne}`, data);
  }

  addDoc(idConsigne: number, docData: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/consignes/${idConsigne}/add-doc`, docData)
  }

  deleteDoc(idConsigne: number, idDoc: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/consignes/${idConsigne}/documents/${idDoc}`)
  }

  getDocument(idConsigne: number, idDoc: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/consignes/${idConsigne}/documents/${idDoc}/download`, { responseType: 'blob'});
  }
}
