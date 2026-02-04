import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class NafN5Service {

  constructor(private http: HttpClient) { }

  getByCode(code: string): Observable<any> {
    return this.http.get(`${environment.apiUrl}/nafn5/${code}`);
  }

  findAll(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/nafn5/all`);
  }

  findAllForCreation(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/nafn5/all/creation`);
  }

  findAllForModification(idEtabAccueil : number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/nafn5/all/modification/${idEtabAccueil}`);
  }
}
