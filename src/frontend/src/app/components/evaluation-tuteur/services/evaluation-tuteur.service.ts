import { Injectable } from '@angular/core';
import {environment} from "../../../../environments/environment";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";

@Injectable({
  providedIn: 'root'
})
export class EvaluationTuteurService {

  constructor(private http: HttpClient) { }

  createReponse(token : string, id: number, data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/evaluation-tuteur/${id}`, data, {params: {token}});
  }

  updateReponse(token : string, id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/evaluation-tuteur/${id}`, data, {params: {token}});
  }

  createReponseSupplementaire(token : string, idConvention: number,idQestion: number, data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/evaluation-tuteur/${idConvention}/reponseSupplementaire/${idQestion}`, data, {params: {token}});
  }

  updateReponseSupplementaire(token : string, idConvention: number,idQestion: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/evaluation-tuteur/${idConvention}/reponseSupplementaire/${idQestion}`, data, {params: {token}});
  }

  validate(token : string, id: number, valid: boolean): Observable<any>{
    return this.http.post(`${environment.apiUrl}/evaluation-tuteur/${id}/validate/${valid}`,null,{params: {token}})
  }

  getEvaluationPDF(token: string, id:number){
    return this.http.get(`${environment.apiUrl}/evaluation-tuteur/${id}/pdf`,{ responseType: 'blob',params:{token}})
  }

  getReouvellement(token: string, id: number){
     return this.http.get(`${environment.apiUrl}/evaluation-tuteur/${id}/renouvellement`,{params: {token}})
  }
}
