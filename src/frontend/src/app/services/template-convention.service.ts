import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { PaginatedService } from './paginated.service';

@Injectable({
  providedIn: 'root'
})
export class TemplateConventionService implements PaginatedService {

  constructor(private http: HttpClient) { }

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get<any[]>(`${environment.apiUrl}/template-convention/all`).pipe(
      map((rows: any[]) => {
        const items = Array.isArray(rows) ? [...rows] : [];
        const sorted = this.sortRows(items, predicate, sortOrder);
        const total = sorted.length;

        if (!perPage || perPage <= 0) {
          return { total, data: sorted };
        }

        const start = Math.max(0, (Math.max(page, 1) - 1) * perPage);
        const end = start + perPage;
        return {
          total,
          data: sorted.slice(start, end)
        };
      })
    );
  }

  create(data: any): Observable<any> {
    return this.http.post(`${environment.apiUrl}/template-convention`, data);
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(environment.apiUrl + `/template-convention/export/${format}`, {params: {headers, predicate, sortOrder, filters}, responseType: 'blob'});
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${environment.apiUrl}/template-convention/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${environment.apiUrl}/template-convention/${id}`);
  }

  getDefaultTemplateConvention(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-convention`, { responseType: 'text' });
  }

  getDefaultTemplateAvenant(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/default-avenant`, { responseType: 'text' });
  }

  getMobileTitle(row: any): string {
    const libelle = row?.libelle || '';
    const typeConventionLabel = row?.typeConvention?.libelle
      || (Array.isArray(row?.typeConventions) ? row.typeConventions.map((typeConvention: any) => typeConvention.libelle).join(', ') : '')
      || 'Sans type';
    const baseTitle = `${typeConventionLabel} - ${row?.langueConvention?.libelle || ''}`;
    return libelle ? `${libelle} - ${baseTitle}` : baseTitle;
  }

  getAll(): Observable<any> {
    return this.http.get(`${environment.apiUrl}/template-convention/all`);
  }

  private sortRows(rows: any[], predicate: string, sortOrder: string): any[] {
    const direction = sortOrder === 'desc' ? -1 : 1;
    const key = predicate || 'libelle';

    return rows.sort((left: any, right: any) => {
      const a = this.getSortValue(left, key);
      const b = this.getSortValue(right, key);
      return a.localeCompare(b) * direction;
    });
  }

  private getSortValue(row: any, predicate: string): string {
    switch (predicate) {
      case 'libelle':
        return row?.libelle || '';
      case 'typeConvention.libelle':
      case 'typeConventionLabel':
        return row?.typeConvention?.libelle
          || (Array.isArray(row?.typeConventions) ? row.typeConventions.map((typeConvention: any) => typeConvention.libelle).join(', ') : '')
          || '';
      case 'langueConvention.code':
        return row?.langueConvention?.code || '';
      default:
        return '';
    }
  }
}
