import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class GroupeEtudiantService {

  constructor(private http: HttpClient) { }

  getBrouillon(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/groupeEtudiant/brouillon`);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/groupeEtudiant", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/groupeEtudiant/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/groupeEtudiant" + id);
  }
}
