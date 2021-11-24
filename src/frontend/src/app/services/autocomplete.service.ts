import { Observable } from "rxjs";

export interface AutocompleteService {
  getAutocompleteData(search: string): Observable<any>;
}
