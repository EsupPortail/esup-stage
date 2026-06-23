import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {environment} from "../../environments/environment";

@Injectable({ providedIn: 'root' })
export class QuestionsEvaluationService {
  constructor(private http: HttpClient) {}

  getQuestions():Observable<any> {
    return this.http.get(`${environment.apiUrl}/questions`);
  }

  getQuestion(code: string):Observable<any> {
    return this.http.get(`${environment.apiUrl}/questions/${code}`);
  }

  getQuestionsEtu():Observable<any> {
    return this.http.get(`${environment.apiUrl}/questions/etu`);
  }

  getQuestionsEns():Observable<any> {
    return this.http.get(`${environment.apiUrl}/questions/ens`);
  }

  getQuestionsEnt():Observable<any> {
    return this.http.get(`${environment.apiUrl}/questions/ent`);
  }

}
