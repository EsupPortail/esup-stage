import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {

  constructor(private http: HttpClient) { }

  getEvaluations(idConventions:number[]):Observable<any>{
    return this.http.get(`${environment.apiUrl}/evaluations`,{params:{idConventions}})
  }

// service
  getExportExcel(idConventions: number[], typeFiche: number) {
    let params = new HttpParams().set('typeFiche', String(typeFiche));
    idConventions.forEach(id => params = params.append('idConventions', String(id)));

    // On observe la réponse et on attend un Blob
    return this.http.get<Blob>(`${environment.apiUrl}/evaluations/excel`, {
      params,
      responseType: 'blob' as 'json',
      observe: 'response'
    });
  }



}
