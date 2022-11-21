import { Observable } from "rxjs";

export interface PaginatedService {
  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any>;
  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any>;
  getMobileTitle(row: any): string;
}
