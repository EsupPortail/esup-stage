import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedResponse } from "./paginated.service";

export interface ApiToken {
  id: number;
  nom: string;
  nomApplication: string;
  actif: boolean;
  dateCreation: string | null;
  loginCreation: string | null;
  dateModification: string | null;
  loginModification: string | null;
}

/** Réponse contenant la valeur en clair du token (création, renouvellement, copie). */
export interface ApiTokenSecret {
  apiToken: ApiToken;
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiTokenService {

  private readonly baseUrl = `${environment.apiUrl}/admin/api-tokens`;

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<PaginatedResponse<ApiToken>> {
    return this.http.get<PaginatedResponse<ApiToken>>(this.baseUrl, {params: {page, perPage, predicate, sortOrder, filters}});
  }

  create(data: { nom: string, nomApplication: string }): Observable<ApiTokenSecret> {
    return this.http.post<ApiTokenSecret>(this.baseUrl, data);
  }

  update(id: number, data: { nom: string, nomApplication: string }): Observable<ApiToken> {
    return this.http.put<ApiToken>(`${this.baseUrl}/${id}`, data);
  }

  renew(id: number): Observable<ApiTokenSecret> {
    return this.http.post<ApiTokenSecret>(`${this.baseUrl}/${id}/renouveler`, {});
  }

  setActif(id: number, actif: boolean): Observable<ApiToken> {
    return this.http.put<ApiToken>(`${this.baseUrl}/${id}/actif`, {}, {params: {actif}});
  }

  reveal(id: number): Observable<ApiTokenSecret> {
    return this.http.get<ApiTokenSecret>(`${this.baseUrl}/${id}/valeur`);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
