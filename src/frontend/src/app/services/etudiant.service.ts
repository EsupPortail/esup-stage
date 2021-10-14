import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { HttpClient } from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class EtudiantService {

  constructor(private http: HttpClient) { }

  searchEtudiants(filters: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/etudiants/ldap-search`, filters);
  }

  getApogeeData(numEtudiant: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/etudiants/${numEtudiant}/apogee-data`);
  }

}