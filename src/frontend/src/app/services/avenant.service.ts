import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AvenantService {

  constructor(private http: HttpClient) { }

  getByConvention(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/avenant/getByConvention/${id}`);
  }

  getById(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/avenant/${id}`);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/avenant", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + "/avenant/" + id, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/avenant/" + id);
  }

  validate(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/avenant/validate/${id}`);
  }

  cancelValidation(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/avenant/validate/cancel/${id}`);
  }

  controleSignatureElectronique(id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/avenant/${id}/controle-signature-electronique`, {});
  }

  envoiSignatureElectronique(ids: number[]): Observable<any> {
    return this.http.post(`${environment.apiUrl}/avenant/signature-electronique`, {ids});
  }

  updateSignatureInfo(id: number): Observable<any> {
    return this.http.post(`${environment.apiUrl}/avenant/${id}/update-signature-electronique-info`, {});
  }

  downloadSignedDoc(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/avenant/${id}/download-signed-doc`, { responseType: 'blob'});
  }

}
