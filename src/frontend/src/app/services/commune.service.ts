import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class CommuneService {

  constructor(private http: HttpClient) { }

  findAll(): Observable<any> {
    return this.http.get(environment.apiUrl + "/commune/");
  }
}
