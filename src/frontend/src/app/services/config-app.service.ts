import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface AppPropertyDto {
  key: string;
  value: string | null;
  isSecret?: boolean | null;
  hasValue: boolean | null;
}

export interface RequiredKeysResponse {
  required: string[];
}

export interface MissingKeysResponse {
  missing: string[];
}

export interface ConfigTestResult {
  result: 'success' | 'error';
  message: string;
}

export interface MailerTestRequest {
  protocol: string;
  host: string;
  port: number;
  auth: string;
  username: string;
  password: string;
  mailto?: string;
  subject?: string;
  content?: string;
}

export interface ReferentielTestRequest {
  login: string;
  password: string;
  ldapUrl: string;
  apogeeUrl: string;
}

export interface WebhookTestRequest {
  uri: string;
  token: string;
}

export interface SireneTestRequest {
  url: string;
  token: string;
  siret?: string;
}

export interface DocaposteTestRequest {
  uri: string;
  siren: string;
  keystorePath: string;
  keystorePassword: string;
  truststorePath: string;
  truststorePassword: string;
}

export interface EsupSignatureTestRequest {
  uri: string;
  circuit: string;
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

  saveProperties(properties: { key: string; value: string | null }[]): Observable<AppPropertyDto[]> {
    return this.http.post<AppPropertyDto[]>(`${this.baseUrl}/properties`, properties);
  }

  getRequiredKeys(): Observable<RequiredKeysResponse> {
    return this.http.get<RequiredKeysResponse>(`${this.baseUrl}/required-keys`);
  }

  getMissingKeys(): Observable<MissingKeysResponse> {
    return this.http.get<MissingKeysResponse>(`${this.baseUrl}/missing`);
  }

  testMailer(request: MailerTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/mailer`, request);
  }

  testReferentiel(request: ReferentielTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/referentiel`, request);
  }

  testWebhook(request: WebhookTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/webhook`, request);
  }

  testSirene(request: SireneTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/sirene`, request);
  }

  testDocaposte(request: DocaposteTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/docaposte`, request);
  }

  testEsupSignature(request: EsupSignatureTestRequest): Observable<ConfigTestResult> {
    return this.http.post<ConfigTestResult>(`${this.baseUrl}/test/esupsignature`, request);
  }
}
