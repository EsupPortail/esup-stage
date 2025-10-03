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

  getExportExcel(idConventions:number[],typeFiche:number):Observable<any>{
    const params = new HttpParams()
      .set('idConventions', idConventions.join(','))
      .set('typeFiche', String(typeFiche));

    return this.http.get(`${environment.apiUrl}/evaluations`,{params:{idConventions,typeFiche}})
  }

}
