import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ConfigMissingService {

  constructor(private http: HttpClient) {}

  getMissing(): Observable<{ missing: string[] }> {
    return this.http.get<{ missing: string[] }>(environment.apiUrl + "/admin/config/missing");
  }
}
