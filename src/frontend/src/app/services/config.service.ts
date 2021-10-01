import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { BehaviorSubject, Observable, Subject } from "rxjs";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ConfigService {

  private themeSubject = new Subject();
  themeModified = this.themeSubject.asObservable();
  configTheme: any = undefined;

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

  async getConfigTheme(): Promise<any> {
    if (!this.configTheme) {
      this.configTheme = await this.http.get(environment.apiUrl + "/config/theme").toPromise();
      this.themeSubject.next(this.configTheme.dateModification);
    }
    return this.configTheme;
  }

  async updateTheme(data: any): Promise<any> {
    this.configTheme = await this.http.post(environment.apiUrl + `/config/theme`, data).toPromise();
    this.themeSubject.next(this.configTheme.dateModification);
    return this.configTheme;
  }

  async rollbackTheme(): Promise<any> {
    this.configTheme = await this.http.delete(environment.apiUrl + `/config/theme`).toPromise();
    this.themeSubject.next(this.configTheme.dateModification);
    return this.configTheme;
  }

}
