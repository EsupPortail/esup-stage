import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";

export interface ActiveSession {
  sessionId: string;
  login: string;
  nom: string;
  prenom: string;
  roles: string[];
  lastRequest: string | number;
  current: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminSessionService {

  constructor(private http: HttpClient) { }

  getSessions(): Observable<ActiveSession[]> {
    return this.http.get<ActiveSession[]>(environment.apiUrl + "/admin/sessions");
  }

  closeSession(sessionId: string): Observable<void> {
    return this.http.post<void>(environment.apiUrl + "/admin/sessions/" + encodeURIComponent(sessionId) + "/close", null);
  }

  closeAllSessions(message: string): Observable<number> {
    return this.http.post<number>(environment.apiUrl + "/admin/sessions/close-all", { message });
  }
}
