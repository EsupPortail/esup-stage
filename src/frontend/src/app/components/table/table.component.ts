import {
  AfterContentInit,
  Component,
  ContentChildren,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  QueryList,
  SimpleChanges,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {Sort, SortDirection} from "@angular/material/sort";
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import {MatColumnDef, MatTable} from "@angular/material/table";
import {PaginatedService} from "../../services/paginated.service";
import {Subject} from "rxjs";
import {debounceTime} from "rxjs/operators";
import * as _ from "lodash";
import {MatAutocompleteSelectedEvent} from "@angular/material/autocomplete";
import {AuthService} from "../../services/auth.service";
import * as FileSaver from "file-saver";
import {TechnicalService} from "../../services/technical.service";
import { MatDialog } from '@angular/material/dialog';
import { ColumnSelectorComponent } from './column-selector/column-selector.component';

@Component({
  selector: 'app-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.scss']
})
export class TableComponent implements OnInit, AfterContentInit, OnChanges {

  @Input() service!: PaginatedService;
  @Input() columns: any;
  @Input() sortColumn: string = '';
  @Input() sortOrder: SortDirection = 'asc';
  @Input() filters: any[] = [];
  @Input() pagination: boolean = true;
  @Input() actionButton: any;
  @Input() hideDeleteFilters!: boolean;
  @Input() selectedRow: any;
  @Input() noResultText: string = 'Aucun élément trouvé';
  @Input() customTemplateRef: TemplateRef<any> | undefined;
  @Input() setAlerte: boolean = false;
  @Input() exportColumns: any = null;
  @Input() templateMobile?: TemplateRef<any>;
  @Input() loadWithoutFilters: boolean = true;
  @Input() confirmMessage: string = "";

  @Output() onUpdated = new EventEmitter<any>();

  @ViewChild(MatTable, {static: true}) table: MatTable<any> | undefined;
  @ViewChild("paginatorTop") paginatorTop!: MatPaginator;
  @ViewChild("paginatorBottom") paginatorBottom!: MatPaginator;
  @ContentChildren(MatColumnDef) columnDefs: QueryList<MatColumnDef> | undefined;

  total: number = 0;
  data: any;
  page: number = 1;
  pageSize: number = 50;
  filterValues: any = [];
  filterValuesToSend: any = [];
  filterChanged = new Subject();
  autocmpleteChanged: any = [];
  autocompleteData: any = [];
  backConfig: any;
  isMobile: boolean = false;

  constructor(
    private authService: AuthService,
    private technicalService: TechnicalService,
    private dialog: MatDialog,
  ) {
    this.technicalService.isMobile.subscribe((value: boolean) => {
      this.isMobile = value;
    });

    this.filterChanged.pipe(debounceTime(1000)).subscribe(() => {
      const filters = this.getFilters();
      if (this.backConfig) {
        this.page = this.backConfig.page;
        this.pageSize = this.backConfig.pageSize;
        this.sortColumn = this.backConfig.sortColumn;
        this.sortOrder = this.backConfig.sortOrder;
        if (this.paginatorTop) this.paginatorTop.pageIndex = this.page - 1;
        if (this.paginatorBottom) this.paginatorBottom.pageIndex = this.page - 1;
      } else {
        this.resetPage();
      }

      for (const key of Object.keys(this.autocmpleteChanged)) {
        this.autocmpleteChanged[key].pipe(debounceTime(1000)).subscribe(async (event: any) => {
          if (event.value?.length >= 2) {
            try {
              const result = await event.filter.autocompleteService.getAutocompleteData(event.value).toPromise();
              this.autocompleteData[event.filter.id] = result?.data || [];
            } catch (error) {
              console.error('Error fetching autocomplete data:', error);
              this.autocompleteData[event.filter.id] = [];
            }
          }
        });
      }

      const nonPermanentFiltersCount = Object.entries(filters)
        .filter(([key, val]) => {
          const f = val as any;
          return !f.permanent && f.value !== undefined && f.value !== null && f.value !== '';
        }).length;

      if (this.loadWithoutFilters || nonPermanentFiltersCount > 0) {
        this.filterValuesToSend = filters;
        this.getPaginated();
      }
    });
  }

  ngOnInit(): void {
    if (window.screen.width < TechnicalService.MAX_WIDTH) {
      this.isMobile = true;
    }
    if (!this.pagination) {
      this.pageSize = 0;
    }
    this.initFilters(false);
    if (this.loadWithoutFilters) {
      this.update();
    }
  }

  ngAfterContentInit() {
    if (this.columnDefs) {
      this.columnDefs.forEach(columnDef => {
        if (this.table) {
          this.table.addColumnDef(columnDef);
        }
      });
    }
  }

  ngOnChanges(changes: SimpleChanges): void {}

  getPaginated(): void {
    this.service.getPaginated(this.page, this.pageSize, this.sortColumn, this.sortOrder, JSON.stringify(this.filterValuesToSend)).subscribe((results: any) => {
      this.total = results.total;
      this.data = results.data;
      if (this.setAlerte) {
        this.data.forEach((row: any) => {
          row.depasseDelaiValidation = false;
          if (!this.isEtudiant()) {
            row.depasseDelaiValidation = (row.centreGestion.validationPedagogique && !row.validationPedagogique) ||
              (row.centreGestion.verificationAdministrative && !row.verificationAdministrative) ||
              (row.centreGestion.validationConvention && !row.validationConvention);
          }
        });
      }
      this.backConfig = undefined;
      this.onUpdated.emit(results);
    });
  }

  update(): void {
    this.filterChanged.next(this.filterValues);
  }

  changePaginator(event: PageEvent): void {
    if (this.pageSize == event.pageSize) {
      this.page = event.pageIndex + 1;
    } else {
      this.resetPage();
    }
    this.pageSize = event.pageSize;
    this.getPaginated();
  }

  sorting(event: Sort): void {
    this.sortColumn = event.active;
    this.sortOrder = event.direction;
    this.update();
  }

  initFilters(emptyValues: boolean): void {
    this.filters.forEach(filter => {
      if (filter && filter.id) {
        this.filterValues[filter.id] = {
          type: filter.type ?? 'text',
          value: (emptyValues && !filter.permanent) ? undefined : filter.value,
          specific: filter.specific
        };

        if (filter.type === 'autocomplete') {
          this.autocmpleteChanged[filter.id] = new Subject();
          if (!this.filterValues[filter.id].value) {
            this.filterValues[filter.id].value = [];
          }
        }
      }
    });
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
    if (!this.filterValues[id]) {
      console.warn(`Filter ${id} not initialized`);
      return;
    }
    if (this.filterValues[id].type === 'list') {
      if (value === null || value === undefined) {
        this.filterValues[id].value = [];
      } else if (Array.isArray(value)) {
        this.filterValues[id].value = value;
      } else {
        this.filterValues[id].value = [value];
      }
    } else {
      this.filterValues[id].value = value;
    }
    this.filterChanged.next(this.filterValues);
  }

  getFilterValues(): any {
    return {...this.filterValues};
  }

  setFilterOption(id: string, options: any[]): void {
    const filter = this.filters.find((f: any) => f.id === id);
    if (filter) {
      if (Array.isArray(options)) {
        filter.options = options;
        this.filterChanged.next(this.filterValues);
      } else {
        console.warn(`Les options fournies pour le filtre ${id} ne sont pas un tableau.`);
      }
    } else {
      console.warn(`Aucun filtre trouvé avec l'ID : ${id}`);
    }
  }

  searchAutocomplete(filter: any, value: string): void {
    this.autocmpleteChanged[filter.id].next({filter, value});
  }

  autocompleteSelected(filter: any, event: MatAutocompleteSelectedEvent): void {
    this.filterValues[filter.id].value.push({...event.option.value});
    this.filterValues[filter.id].autocomplete = null;
    this.filterChanged.next(this.filterValues);
  }

  removeAutocomplete(filter: any, value: any): void {
    const index = this.filterValues[filter.id].value.findIndex((v: any) => {
      return v[filter.keyId] === value[filter.keyId];
    });
    if (index > -1) {
      this.filterValues[filter.id].value.splice(index, 1);
      this.filterChanged.next(this.filterValues);
    }
  }

  getFilters(): any {
    const f: any = {};
    for (const key of Object.keys(this.filterValues)) {
      const filterValue = this.filterValues[key];
      if (
        filterValue?.value !== undefined &&
        filterValue.value !== '' &&
        filterValue.value !== null &&
        (!Array.isArray(filterValue.value) || filterValue.value.length > 0)
      ) {
        f[key] = {...filterValue};
        if (f[key].type === 'list') {
          if (key === 'annee' && Array.isArray(f[key].value)) {
            f[key].value = String(f[key].value[0]);
          }
        }
        if (['date', 'date-min', 'date-max'].includes(f[key].type)) {
          f[key].value = f[key].value instanceof Date ? f[key].value.getTime() : f[key].value;
        }
        if (f[key].type === 'autocomplete') {
          if (Array.isArray(f[key].value)) {
            f[key].value = f[key].value
              .filter((v: undefined) => v !== undefined)
              .map((item: { id: any }) => item.id || item);
          }
        }
        if (typeof f[key].value === 'string') {
          f[key].value = f[key].value.trim();
        }
        if (f[key].specific === undefined) {
          delete f[key].specific;
        }
      }
    }
    return f;
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  export(format: string): void {
    this.service.exportData(format, JSON.stringify(this.exportColumns), this.sortColumn, this.sortOrder, JSON.stringify(this.filterValuesToSend)).subscribe((response: any) => {
      const type = format === 'excel' ? 'application/vnd.ms-excel' : 'text/csv';
      let blob = new Blob([response as BlobPart], {type: type});
      let filename = 'export_' + (new Date()).getTime() + '.' + (format === 'excel' ? 'xls' : 'csv');
      FileSaver.saveAs(blob, filename);
    });
  }

  resetPage(): void {
    this.page = 1;
    this.paginatorBottom.pageIndex = this.page - 1;
    this.paginatorTop.pageIndex = this.page - 1;
  }

  setBackConfig(config: any): void {
    this.backConfig = config;
  }

  getMobileTitle(row: any): string {
    return this.service.getMobileTitle(row);
  }

  updateNumber() {
    this.filterValues.id.value = this.filterValues.id.value.replace(/\D/g, '');
  }

  private normalizeExportColumns(raw: any): { sheets: any[] } {
    if (raw?.multipleExcelSheets && Array.isArray(raw.multipleExcelSheets)) {
      return {
        sheets: raw.multipleExcelSheets.map((sheet: { title?: string; columns: Record<string, { title: string }> }) => ({
          title: sheet.title || 'Feuille',
          availableColumns: Object.entries(sheet.columns).map(([key, col]: [string, any]) => ({
            key,
            title: col.title
          }))
        }))
      };
    }

    if (typeof raw === 'object' && !Array.isArray(raw)) {
      return {
        sheets: [
          {
            title: 'Colonnes disponibles',
            availableColumns: Object.entries(raw).map(([key, col]: [string, any]) => ({
              key,
              title: col.title
            }))
          }
        ]
      };
    }

    return { sheets: [] };
  }

  openExportDialog() {
    const normalized = this.normalizeExportColumns(this.exportColumns);

    const dialogRef = this.dialog.open(ColumnSelectorComponent, {
      width: '900px',
      data: { sheets: normalized.sheets }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const exportData = result.map((sheet: any) => ({
          title: sheet.title,
          columns: sheet.selectedColumns.reduce((acc: any, col: any) => {
            acc[col.key] = { title: col.title };
            return acc;
          }, {})
        }));

        this.exportWithCustomColumns({ multipleExcelSheets: exportData });
      }
    });
  }

  exportWithCustomColumns(columnsConfig: { multipleExcelSheets: any[] }) {
    this.service.exportData(
      'excel',
      JSON.stringify(columnsConfig),
      this.sortColumn,
      this.sortOrder,
      JSON.stringify(this.filterValuesToSend)
    ).subscribe((response: any) => {
      const blob = new Blob([response], { type: 'application/vnd.ms-excel' });
      FileSaver.saveAs(blob, `export_${Date.now()}.xls`);
    });
  }

  hasExportableColumns(): boolean {
    if (!this.exportColumns) return false;

    // Cas avec plusieurs feuilles
    if ('multipleExcelSheets' in this.exportColumns && Array.isArray(this.exportColumns.multipleExcelSheets)) {
      return this.exportColumns.multipleExcelSheets.some((sheet: any) =>
        sheet.columns && Object.keys(sheet.columns).length > 0
      );
    }

    // Cas simple : un seul objet plat
    return typeof this.exportColumns === 'object' && Object.keys(this.exportColumns).length > 0;
  }
}
