import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConventionDocumentsResponse } from '../models/convention-document.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ConventionDocumentService {

  private readonly baseUrl = `${environment.apiUrl}/conventions`;

  constructor(private http: HttpClient) {}

  list(idConvention: number): Observable<ConventionDocumentsResponse> {
    return this.http.get<ConventionDocumentsResponse>(
      `${this.baseUrl}/${idConvention}/documents-etudiant`
    );
  }

  upload(idConvention: number, file: File): Observable<ConventionDocumentsResponse> {
    const formData = new FormData();
    formData.append('doc', file, file.name);
    return this.http.post<ConventionDocumentsResponse>(
      `${this.baseUrl}/${idConvention}/documents-etudiant`,
      formData
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
