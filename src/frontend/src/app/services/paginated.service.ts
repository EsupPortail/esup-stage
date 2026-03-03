import { Observable } from "rxjs";

export interface PaginatedService<T=any> {
  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<PaginatedResponse<T>>;
  exportData(format: string, headers: string, predicate: string, sortOrder: string, filters: string): Observable<any>;
  getMobileTitle(row: any): string;
}
export type PaginatedResponse<T> = {
  total: number;
  data: T[];
}
