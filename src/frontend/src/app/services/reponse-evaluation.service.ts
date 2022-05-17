import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class ReponseEvaluationService {

  constructor(private http: HttpClient) { }

  getByConvention(id: number): Observable<any> {
    return this.http.get(`${environment.apiUrl}/reponseEvaluation/getByConvention/${id}`);
  }

  create(data: any): Observable<any> {
    return this.http.post(environment.apiUrl + "/reponseEvaluation", data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/reponseEvaluation/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(environment.apiUrl + "/reponseEvaluation" + id);
  }
}
