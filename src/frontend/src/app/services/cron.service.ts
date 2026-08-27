import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {AuthService} from "./auth.service";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {PaginatedService} from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class CronService implements PaginatedService {

  constructor(private http: HttpClient, private authService: AuthService) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/cron", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + '/cron/' + id, data);
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/cron/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getMobileTitle(row: any): string {
    return "";
  }

  executeNow(id: number): Observable<any> {
    // Lancement asynchrone côté serveur : la réponse est immédiate, pas de loader plein écran
    return this.http.post(`${environment.apiUrl}/cron/${id}/execute`, {}, {headers: new HttpHeaders({'X-No-Loader': 'true'})});
  }

  getRunning(): Observable<number[]> {
    // Polling de fond : pas de loader plein écran
    return this.http.get<number[]>(`${environment.apiUrl}/cron/running`, {headers: new HttpHeaders({'X-No-Loader': 'true'})});
  }

}
