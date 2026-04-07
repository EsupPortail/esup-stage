import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FileElement, FileContent, ZipResult } from '../models/file-element.model';

@Injectable({ providedIn: 'root' })
export class FileExplorerService {
  private readonly API = '/api/admin/logs';

  constructor(private http: HttpClient) {}

  listFolder(path: any): Observable<FileElement[]> {
    if(path === null) {
      return this.http.get<FileElement[]>(`${this.API}/list`)
    }
    const params = new HttpParams().set('path', path);
    return this.http.get<FileElement[]>(`${this.API}/list`, { params });
  }

  getFileContent(path: string, page = 0, pageSize = 500): Observable<FileContent> {
    const params = new HttpParams()
      .set('path', path)
      .set('page', page)
      .set('pageSize', pageSize);
    return this.http.get<FileContent>(`${this.API}/content`, { params });
  }

  /** Téléchargement direct d'un fichier unique (format original) */
  exportSingle(path: string): Observable<Blob> {
    const params = new HttpParams().set('path', path);
    return this.http.get(`${this.API}/export/single`, { params, responseType: 'blob' });
  }

  /** Exporte un fichier log au format CSV */
  exportAsCsv(path: string): Observable<Blob> {
    const params = new HttpParams().set('path', path);
    return this.http.get(`${this.API}/export/csv`, { params, responseType: 'blob' });
  }

  /** Téléchargement interne (explorateur) */
  downloadFile(path: string): Observable<Blob> {
    const params = new HttpParams().set('path', path);
    return this.http.get(`${this.API}/download`, { params, responseType: 'blob' });
  }
}
