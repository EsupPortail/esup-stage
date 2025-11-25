import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {ExcelExportEval} from "../models/excel-export-eval.model";

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {

  constructor(private http: HttpClient) { }

  getEvaluations(idConventions:number[]):Observable<any>{
    return this.http.get(`${environment.apiUrl}/evaluations`,{params:{idConventions}})
  }

  getExportExcel(idConventions: number[], typeFiche: number, columns?: string[]): Observable<any> {
    let body:ExcelExportEval = {
      idConventions,
      typeFiche,
      ...(columns && columns.length ? { colonnes: columns } : {})
    };
    console.log(body);

    return this.http.post<Blob>(`${environment.apiUrl}/evaluations/excel`,
      body,
      {responseType: 'blob' as 'json', observe: 'response'}
    );
  }



}
