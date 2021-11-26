import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class LdapService {

  constructor(private http: HttpClient) { }

  searchUsers(login: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/ldap", {params: {login}});
  }

  searchEtudiants(filters: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/ldap/etudiants`, filters);
  }
  searchEnseignants(filters: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/ldap/enseignants`, filters);
  }

  searchUsersByName(nom: string, prenom: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/ldap/search-by-name`, {params: {nom, prenom}});
  }
}
