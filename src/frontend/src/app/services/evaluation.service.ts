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

  getExportExcel(payload: ExcelExportEval): Observable<any> {
    return this.http.post<Blob>(`${environment.apiUrl}/evaluations/excel`,
      payload,
      {responseType: 'blob' as 'json', observe: 'response'}
    );
  }

}
