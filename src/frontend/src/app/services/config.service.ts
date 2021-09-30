import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  constructor(private http: HttpClient) { }

  getConfigGenerale(): Observable<any> {
    return this.http.get(environment.apiUrl + "/config/generale");
  }

  updateGenerale(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + `/config/generale`, data);
  }

  getConfigAlerteMail(): Observable<any> {
    return this.http.get(environment.apiUrl + "/config/alerte-mail");
  }

  updateAlerteMail(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + `/config/alerte-mail`, data);
  }

}
