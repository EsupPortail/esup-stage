import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";
import { DatePipe } from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class PeriodeInterruptionAvenantService implements PaginatedService {

  constructor(private http: HttpClient, private datePipe: DatePipe) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + "/periode-interruption-avenant", {params: {page, perPage, predicate, sortOrder, filters}});
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/periode-interruption-avenant/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  getByAvenant(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/periode-interruption-avenant/getByAvenant/${id}`);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/periode-interruption-avenant", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(environment.apiUrl + "/periode-interruption-avenant/" + id, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/periode-interruption-avenant/" + id);
  }

  deleteAll(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/periode-interruption-avenant/deleteAll/" + id);
  }

  getMobileTitle(row: any): string {
    return `Du ${this.datePipe.transform(row.dateDebutInterruption, 'shortDate')} au ${this.datePipe.transform(row.dateFinInterruption, 'shortDate')}`;
  }
}
