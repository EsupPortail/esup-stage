import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConventionDocumentsResponse } from '../models/convention-document.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConventionDocumentService {

  private readonly baseUrl = `${environment.apiUrl}/conventions`;

  constructor(private readonly http: HttpClient) {}

  list(idConvention: number): Observable<ConventionDocumentsResponse> {
    return this.http.get<ConventionDocumentsResponse>(
      `${this.baseUrl}/${idConvention}/documents-etudiant`
    );
  }

  upload(idConvention: number, file: File, remplacer = false): Observable<ConventionDocumentsResponse> {
    const formData = new FormData();
    const params = new HttpParams().set('remplacer', remplacer);
    formData.append('doc', file, file.name);
    return this.http.post<ConventionDocumentsResponse>(
      `${this.baseUrl}/${idConvention}/documents-etudiant`,
      formData,
      { params }
    );
  }

  preview(idConvention: number, idDocument: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/${idConvention}/documents-etudiant/${idDocument}/preview`,
      { responseType: 'blob' }
    );
  }

  download(idConvention: number, idDocument: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/${idConvention}/documents-etudiant/${idDocument}/download`,
      { responseType: 'blob' }
    );
  }

  delete(idConvention: number, idDocument: number): Observable<ConventionDocumentsResponse> {
    return this.http.delete<ConventionDocumentsResponse>(
      `${this.baseUrl}/${idConvention}/documents-etudiant/${idDocument}`
    );
  }
}
