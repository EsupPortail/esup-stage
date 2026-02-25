import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface AppPropertyDto {
  key: string;
  value: string;
}

export interface RequiredKeysResponse {
  required: string[];
}

export interface MissingKeysResponse {
  missing: string[];
}

@Injectable({
  providedIn: 'root'
})
export class ConfigAppService {

  private readonly baseUrl = `${environment.apiUrl}/admin/config`;

  constructor(private http: HttpClient) { }

  getProperties(): Observable<AppPropertyDto[]> {
    return this.http.get<AppPropertyDto[]>(`${this.baseUrl}/properties`);
  }

  saveProperties(properties: AppPropertyDto[]): Observable<AppPropertyDto[]> {
    return this.http.post<AppPropertyDto[]>(`${this.baseUrl}/properties`, properties);
  }

  getRequiredKeys(): Observable<RequiredKeysResponse> {
    return this.http.get<RequiredKeysResponse>(`${this.baseUrl}/required-keys`);
  }

  getMissingKeys(): Observable<MissingKeysResponse> {
    return this.http.get<MissingKeysResponse>(`${this.baseUrl}/missing`);
  }
}
