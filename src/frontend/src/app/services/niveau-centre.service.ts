import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class NiveauCentreService {

  constructor(private http: HttpClient) { }

  findList(): Observable<any> {
    return this.http.get(environment.apiUrl + "/niveau-centre/centre-gestion-list");
  }
}
