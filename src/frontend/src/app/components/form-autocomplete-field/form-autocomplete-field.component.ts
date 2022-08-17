import { Component,Input, OnInit, OnChanges } from '@angular/core';
import { PaginatedService } from "../../services/paginated.service";
import { FormGroup } from "@angular/forms";
import { Observable } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-form-autocomplete-field',
  templateUrl: './form-autocomplete-field.component.html',
  styleUrls: ['./form-autocomplete-field.component.scss']
})
export class FormAutocompleteFieldComponent implements OnInit {

  @Input() service: PaginatedService;
  @Input() field: any;
  @Input() fieldLabel: any;
  @Input() startWith: boolean;

  filteredItems: Observable<string[]>;
  items: any[] = [];

  constructor() { }

  ngOnInit(): void {
    this.service.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.items = response;
    });
  }

  ngOnChanges(): void {
    if(this.startWith){
      this.filteredItems = this.field.valueChanges.pipe(
        startWith(''),
        map((value: string) => this._filterStartWith(value || '', this.items)),
      );
    }else{
      this.filteredItems = this.field.valueChanges.pipe(
        startWith(''),
        map((value: string) => this._filter(value || '', this.items)),
      );
    }
  }

  private _filter(value: string, options : any[]): string[] {
    const filterValue = value.toLowerCase();
    const maxSize = 200;

    return options.filter(option => option.libelle.toLowerCase().includes(filterValue)).slice(0, maxSize);
  }

  private _filterStartWith(value: string, options : any[]): string[] {
    const filterValue = value.toLowerCase();
    const maxSize = 200;

    return options.filter(option => option.libelle.toLowerCase().startsWith(filterValue)).slice(0, maxSize);
  }
}
