import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { Sort, SortDirection } from "@angular/material/sort";
import { PageEvent } from "@angular/material/paginator";

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit {

  @Input() service: any;
  @Input() columns: any;
  @Input() sortColumn: string = '';
  @Input() sortOrder: SortDirection = 'asc';
  @Input() actionTemplate: TemplateRef<any> | null = null;
  @Input() filters: any[] = [];

  @Output() onUpdated = new EventEmitter<any>();

  displayedColumns = [];
  total: number = 0;
  data: any;
  page: number = 1;
  pageSize: number = 50;
  filterValues: any = [];

  constructor() { }

  ngOnInit(): void {
    this.displayedColumns = this.columns.map((c: any) => c.id);
    this.initFilters();
    this.update();
  }

  update(): void {
    let f: any = {};
    for (let key of Object.keys(this.filterValues)) {
      if (this.filterValues[key].value !== undefined) {
        f[key] = this.filterValues[key];
        if (f[key].specific === undefined) {
          delete f[key].specific;
        }
      }
    }

    this.service.getPaginated(this.page, this.pageSize, this.sortColumn, this.sortOrder, JSON.stringify(f)).subscribe((results: any) => {
      this.total = results.total;
      this.data = results.data;
    });
  }

  changePaginator(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.page = event.pageIndex + 1;
    this.update();
  }

  sort(event: Sort): void {
    this.sortColumn = event.active;
    this.sortOrder = event.direction;
    this.update();
  }

  initFilters(): void {
    for (const filter of this.filters) {
      this.filterValues[filter.id] = {
        type: filter.type ?? 'text',
        value: filter.value,
        specific: filter.specific
      }
    }
  }

  reset(): void {
    this.initFilters();
    this.update();
  }

}
