import { Observable } from "rxjs";

export interface PaginatedService {
  getPaginated(page: number, perPage: number, predicate: string, sortOrder: string, filters: string): Observable<any>;
}
