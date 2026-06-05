import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";
import {ExcelExportEval} from "../models/excel-export-eval.model";

@Injectable({
  providedIn: 'root'
})
export class EvaluationService {

  constructor(private readonly http: HttpClient) { }

  getExportExcel(idConventions: number[], typeFiche: number, columns?: string[]): Observable<any> {
    let body:ExcelExportEval = {
      idConventions,
      typeFiche,
      ...(columns?.length ? { colonnes: columns } : {})
    };

    return this.http.post<Blob>(`${environment.apiUrl}/evaluations/excel`,
      body,
      {responseType: 'blob' as 'json', observe: 'response'}
    );
  }



}
