import {
  AfterContentInit,
  Component, ContentChildren,
  EventEmitter,
  Input,
  OnInit,
  Output, QueryList,
  ViewChild
} from '@angular/core';
import { Sort, SortDirection } from "@angular/material/sort";
import { PageEvent } from "@angular/material/paginator";
import { MatColumnDef, MatTable } from "@angular/material/table";
import { PaginatedService } from "../../services/paginated.service";
import { Subject } from "rxjs";
import { debounceTime } from "rxjs/operators";

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit, AfterContentInit {

  @Input() service: PaginatedService;
  @Input() columns: any;
  @Input() sortColumn: string = '';
  @Input() sortOrder: SortDirection = 'asc';
  @Input() filters: any[] = [];
  @Input() pagination: boolean = true;
  @Input() actionButton: any;
  @Input() hideDeleteFilters: boolean;

  @Output() onUpdated = new EventEmitter<any>();

  @ViewChild(MatTable, { static: true }) table: MatTable<any> | undefined;
  @ContentChildren(MatColumnDef) columnDefs: QueryList<MatColumnDef> | undefined;

  total: number = 0;
  data: any;
  page: number = 1;
  pageSize: number = 50;
  filterValues: any = [];
  filterChanged = new Subject();

  constructor() {
    this.filterChanged.pipe(debounceTime(500)).subscribe(() => {
      let f: any = {};
      for (let key of Object.keys(this.filterValues)) {
        if (this.filterValues[key].value !== undefined && this.filterValues[key].value !== '' && this.filterValues[key].value.length > 0) {
          f[key] = this.filterValues[key];
          if (f[key].specific === undefined) {
            delete f[key].specific;
          }
        }
      }

      this.service.getPaginated(this.page, this.pageSize, this.sortColumn, this.sortOrder, JSON.stringify(f)).subscribe((results: any) => {
        this.total = results.total;
        this.data = results.data;
        this.onUpdated.emit(this.data);
      });
      })
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
    }
  }

  reset(): void {
    this.initFilters(true);
    this.update();
  }

}
