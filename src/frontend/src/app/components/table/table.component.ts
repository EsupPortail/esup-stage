import {
  AfterContentInit,
  Component, ContentChildren,
  EventEmitter,
  Input, OnChanges,
  OnInit,
  Output, QueryList, SimpleChanges, TemplateRef,
  ViewChild
} from '@angular/core';
import { Sort, SortDirection } from "@angular/material/sort";
import { MatPaginator, PageEvent } from "@angular/material/paginator";
import { MatColumnDef, MatTable } from "@angular/material/table";
import { PaginatedService } from "../../services/paginated.service";
import { Observable, Subject } from "rxjs";
import { debounceTime } from "rxjs/operators";
import * as _ from "lodash";
import { MatAutocompleteSelectedEvent } from "@angular/material/autocomplete";

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit, AfterContentInit, OnChanges {

  @Input() service: PaginatedService;
  @Input() columns: any;
  @Input() sortColumn: string = '';
  @Input() sortOrder: SortDirection = 'asc';
  @Input() filters: any[] = [];
  @Input() pagination: boolean = true;
  @Input() actionButton: any;
  @Input() hideDeleteFilters: boolean;
  @Input() selectedRow: any;
  @Input() noResultText: string = 'Aucun élément trouvé';
  @Input() customTemplateRef: TemplateRef<any>|undefined;

  @Output() onUpdated = new EventEmitter<any>();

  @ViewChild(MatTable, { static: true }) table: MatTable<any> | undefined;
  @ViewChild("paginatorTop") paginatorTop: MatPaginator;
  @ViewChild("paginatorBottom") paginatorBottom: MatPaginator;
  @ContentChildren(MatColumnDef) columnDefs: QueryList<MatColumnDef> | undefined;

  total: number = 0;
  data: any;
  page: number = 1;
  pageSize: number = 50;
  filterValues: any = [];
  filterChanged = new Subject();
  autocmpleteChanged: any = [];
  autocompleteData: any = [];

  constructor() {
    this.filterChanged.pipe(debounceTime(500)).subscribe(() => {
      let f: any = {};
      for (let key of Object.keys(this.filterValues)) {
        if (this.filterValues[key].value !== undefined && this.filterValues[key].value !== '' && this.filterValues[key].value !== null && (!Array.isArray(this.filterValues[key].value) || this.filterValues[key].value.length > 0)) {
          f[key] = {...this.filterValues[key]};
          if (['date', 'date-min', 'date-max'].indexOf(f[key].type) > -1) {
            f[key].value = f[key].value.getTime();
          }
          // conversion de l'array d'objets en array de keyId
          if (['autocomplete'].indexOf(f[key].type) > -1) {
            f[key].type = 'list';
            const filter = this.filters.find((filter: any) => { return filter.id === key; });
            if (filter) {
              f[key].value = f[key].value.map((v: any) => { return v[filter.keyId]; });
            }
          }
          if (f[key].specific === undefined) {
            delete f[key].specific;
          }
        }
      }

      for (const key of Object.keys(this.autocmpleteChanged)) {
        this.autocmpleteChanged[key].pipe(debounceTime(500)).subscribe(async (event: any) => {
          if (event.value.length >= 2) {
            this.autocompleteData[event.filter.id] = await event.filter.autocompleteService.getAutocompleteData(event.value).toPromise();
            this.autocompleteData[event.filter.id] = this.autocompleteData[event.filter.id].data;
          }
        });
      }

      this.service.getPaginated(this.page, this.pageSize, this.sortColumn, this.sortOrder, JSON.stringify(f)).subscribe((results: any) => {
        this.total = results.total;
        this.data = results.data;
        this.onUpdated.emit(results);
      });
    });
  }

  ngOnInit(): void {
    if (!this.pagination) {
      this.pageSize = 0;
    }
    this.initFilters(false);
    this.update();
  }

  ngAfterContentInit() {
    if (this.columnDefs) {
      this.columnDefs.forEach(columnDef => {
        if (this.table) {
          this.table.addColumnDef(columnDef)
        }
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  update(): void {
    this.filterChanged.next();
  }

  changePaginator(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.page = event.pageIndex + 1;
    this.update();
  }

  sorting(event: Sort): void {
    this.sortColumn = event.active;
    this.sortOrder = event.direction;
    this.update();
  }

  initFilters(emptyValues: boolean): void {
    for (const filter of this.filters) {
      this.filterValues[filter.id] = {
        type: filter.type ?? 'text',
        value: emptyValues ? undefined : filter.value,
        specific: filter.specific
      }
      if (filter.type === 'autocomplete') {
        this.autocmpleteChanged[filter.id] = new Subject();
      }
    }
  }

  reset(): void {
    this.initFilters(true);
    this.update();
  }

  isSelected(row: any): boolean {
    return _.isEqual(row, this.selectedRow);
  }

  setFilter(filter: any): void {
    this.filterValues[filter.id] = {
      type: filter.type ?? 'text',
      value: filter.value,
      specific: filter.specific
    };
  }

  setFilterValue(id: string, value: any): void {
    this.filterValues[id].value = value;
  }

  setFilterOption(id: string, options: any[]): void {
    const filter = this.filters.find((f: any) => { return f.id === id; });
    if (filter) {
      filter.options = options;
    }
  }

  searchAutocomplete(filter: any, value: string): void {
    this.autocmpleteChanged[filter.id].next({filter, value});
  }

  autocompleteSelected(filter: any, event: MatAutocompleteSelectedEvent): void {
    this.filterValues[filter.id].value.push({...event.option.value});
    this.filterValues[filter.id].autocomplete = null;
    this.filterChanged.next();
  }

  removeAutocomplete(filter: any, value: any): void {
    const index = this.filterValues[filter.id].value.findIndex((v: any) => { return v[filter.keyId] === value[filter.keyId]; });
    if (index > -1) {
      this.filterValues[filter.id].value.splice(index, 1);
      this.filterChanged.next();
    }
  }

}
