import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { map, Observable } from "rxjs";
import { environment } from "../../environments/environment";
import { PaginatedService } from "./paginated.service";

@Injectable({
  providedIn: 'root'
})
export class MaintenanceAdminService implements PaginatedService {

  private readonly baseUrl = `${environment.apiUrl}/admin/maintenance`;

  constructor(private http: HttpClient) {}

  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get<any>(this.baseUrl, { params: { page, perPage, predicate, sortOrder, filters } }).pipe(
      map((response: any) => ({
        ...response,
        data: Array.isArray(response?.data) ? response.data.map((row: any) => this.normalizeRow(row)) : []
      }))
    );
  }

  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/export/${format}`, { params: { headers, predicate, sortOrder, filters }, responseType: 'blob' });
  }

  getMobileTitle(row: any): string {
    return `${row.id} - ${row.message ?? ''}`;
  }

  create(data: any): Observable<any> {
    return this.http.post(this.baseUrl, data);
  }

  update(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/${id}`, data);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  activate(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/activate`, {});
  }

  deactivate(id: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/deactivate`, {});
  }

  private normalizeRow(row: any): any {
    return {
      ...row,
      datDebMaint: this.normalizeDate(row?.datDebMaint),
      datFinMaint: this.normalizeDate(row?.datFinMaint),
      datAlertMaint: this.normalizeDate(row?.datAlertMaint),
      createdAt: this.normalizeDate(row?.createdAt),
    };
  }

  private normalizeDate(value: any): Date | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? null : value;
    }

    if (Array.isArray(value)) {
      const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = value;
      if (!year || !month || !day) {
        return null;
      }
      const milliseconds = Math.floor((Number(nano) || 0) / 1_000_000);
      const date = new Date(
        Number(year),
        Number(month) - 1,
        Number(day),
        Number(hour) || 0,
        Number(minute) || 0,
        Number(second) || 0,
        milliseconds
      );
      return Number.isNaN(date.getTime()) ? null : date;
    }

    if (typeof value === 'number') {
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    if (typeof value === 'string') {
      const raw = value.trim();
      if (!raw) {
        return null;
      }

      const commaSeparated = raw.replace(/^\[|\]$/g, '');
      if (/^\d{4},\d{1,2},\d{1,2}(,\d{1,2}){0,4}$/.test(commaSeparated)) {
        const parts = commaSeparated.split(',').map((part) => Number(part.trim()));
        const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = parts;
        if (!year || !month || !day) {
          return null;
        }

        const milliseconds = Math.floor((Number(nano) || 0) / 1_000_000);
        const dateFromParts = new Date(
          Number(year),
          Number(month) - 1,
          Number(day),
          Number(hour) || 0,
          Number(minute) || 0,
          Number(second) || 0,
          milliseconds
        );
        return Number.isNaN(dateFromParts.getTime()) ? null : dateFromParts;
      }

      const normalized = raw.includes('T') ? raw : raw.replace(' ', 'T');
      const date = new Date(normalized);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    return null;
  }
}
