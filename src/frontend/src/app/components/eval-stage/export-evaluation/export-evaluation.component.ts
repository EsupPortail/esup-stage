import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {EvaluationService} from "../../../services/evaluation.service";
import * as FileSaver from "file-saver";
import {CdkDragDrop, moveItemInArray} from "@angular/cdk/drag-drop";

type TypeFiche = 0 | 1 | 2 | 3; // 0 = étudiant, 1 = enseignant référent, 2 = tuteur pro, 3 = tous

@Component({
    selector: 'app-export-evaluation',
    templateUrl: './export-evaluation.component.html',
    styleUrl: './export-evaluation.component.scss',
    standalone: false
})
export class ExportEvaluationComponent implements OnInit{

  form!: FormGroup;

  sheets: any[] = [];
  selectedSheetIndex = 0;
  trackByKey = (_: number, item: any) => item?.key;

  selectedAvailableKeys = new Set<string>();
  selectedChosenKeys = new Set<string>();

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: {
      sheets: any[];
      rows: any[] },
    public dialogRef: MatDialogRef<ExportEvaluationComponent>,
    private fb: FormBuilder,
    private evaluationService : EvaluationService,
  ) {
    this.form = this.fb.group({
      typeFiche: [null as TypeFiche | null, Validators.required],
    });
    this.sheets = data.sheets.map((sheet: any) => ({
      title: sheet.title,
      availableColumns: [...sheet.availableColumns],
      selectedColumns: []
    }));
    this.form.get('typeFiche')!.valueChanges.subscribe(() => {
      this.selectedSheetIndex = 0;
      this.selectedAvailableKeys.clear();
      this.selectedChosenKeys.clear();
    });
  }

  ngOnInit() {
    console.log(this.data.rows);
  }

// composant
  confirm() {
    console.log("sheets : ",this.sheets);
    let SelectedColumns: string[] =  this.getSelectedColumnKeys(this.form.value.typeFiche);
    console.log(SelectedColumns);
    console.log("conventions : ",this.data.rows);
    this.data.rows.map(r => console.log(r.id));
    console.log(this.form.value.typeFiche);

    this.evaluationService
      .getExportExcel(this.data.rows.map(r => r.id), this.form.value.typeFiche, SelectedColumns)
      .subscribe((res) => {
        const blob = res.body!;
        FileSaver.saveAs(blob,'export_' + Date.now() + '.xlsx');
        this.dialogRef.close();
      });
  }


  cancel(){
    this.dialogRef.close();
  }

  reset(sheet: any) {
    if (this.isLocked) return;
    sheet = sheet ?? this.currentSheet;
    sheet.availableColumns = [...sheet.availableColumns, ...sheet.selectedColumns];
    sheet.selectedColumns = [];
  }

  private sortByTitle = (a: any, b: any) =>
    (a?.title || '').localeCompare(b?.title || '', 'fr', { sensitivity: 'base' });

  get hasMultipleSheets(): boolean {
    return this.sheets.length > 1;
  }

  get currentSheet() {
    return this.displayedSheets[this.selectedSheetIndex];
  }

  // --- Drag & drop
  drop(event: CdkDragDrop<any[]>, sheet: any) {
    if (this.isLocked || !sheet) return;
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    const fromSelectedList = (event.previousContainer.data === sheet.selectedColumns);
    const toSelectedList   = (event.container.data === sheet.selectedColumns);

    const draggedCol = event.item.data as any;

    // bloc multi-sélection ou élément unique
    let keysToMove = new Set<string>();
    if (fromSelectedList) {
      keysToMove = (this.selectedChosenKeys.size && this.selectedChosenKeys.has(draggedCol.key))
        ? new Set(this.selectedChosenKeys)
        : new Set([draggedCol.key]);
    } else {
      keysToMove = (this.selectedAvailableKeys.size && this.selectedAvailableKeys.has(draggedCol.key))
        ? new Set(this.selectedAvailableKeys)
        : new Set([draggedCol.key]);
    }

    const source = fromSelectedList ? sheet.selectedColumns : sheet.availableColumns;
    const target = toSelectedList   ? sheet.selectedColumns : sheet.availableColumns;

    const toMove = source.filter((c: any) => keysToMove.has(c.key));
    const remain = source.filter((c: any) => !keysToMove.has(c.key));

    if (fromSelectedList) sheet.selectedColumns = remain; else sheet.availableColumns = remain;

    const insertAt = event.currentIndex;
    const newTarget = [...target.slice(0, insertAt), ...toMove, ...target.slice(insertAt)];

    if (toSelectedList) {
      sheet.selectedColumns = newTarget;
    } else {
      sheet.availableColumns = newTarget
    }

    if (fromSelectedList) this.selectedChosenKeys.clear(); else this.selectedAvailableKeys.clear();
  }


  // --- Sélection (toggle)
  toggleAvailable(col: any) {
    this.selectedAvailableKeys.has(col.key)
      ? this.selectedAvailableKeys.delete(col.key)
      : this.selectedAvailableKeys.add(col.key);
  }

  toggleChosen(col: any) {
    this.selectedChosenKeys.has(col.key)
      ? this.selectedChosenKeys.delete(col.key)
      : this.selectedChosenKeys.add(col.key);
  }

  // --- Sélectionner tout / désélectionner
  isAllAvailableSelected(sheet: any): boolean {
    return sheet.availableColumns.length > 0 &&
      sheet.availableColumns.every((c: any) => this.selectedAvailableKeys.has(c.key));
  }

  toggleAllAvailable(checked: boolean, sheet: any) {
    if (checked) {
      this.selectedAvailableKeys = new Set(sheet.availableColumns.map((c: any) => c.key));
    } else {
      this.selectedAvailableKeys.clear();
    }
  }

  isAllChosenSelected(sheet: any): boolean {
    return sheet.selectedColumns.length > 0 &&
      sheet.selectedColumns.every((c: any) => this.selectedChosenKeys.has(c.key));
  }

  toggleAllChosen(checked: boolean, sheet: any) {
    if (checked) {
      this.selectedChosenKeys = new Set(sheet.selectedColumns.map((c: any) => c.key));
    } else {
      this.selectedChosenKeys.clear();
    }
  }

  addSelected(sheet: any) {
    if (this.isLocked || !sheet || !this.selectedAvailableKeys.size) return;
    const toMove = sheet.availableColumns.filter((c: any) => this.selectedAvailableKeys.has(c.key));
    const remain = sheet.availableColumns.filter((c: any) => !this.selectedAvailableKeys.has(c.key));
    sheet.availableColumns = remain;
    sheet.selectedColumns = [...sheet.selectedColumns, ...toMove];
  }

  removeSelected(sheet: any) {
    if (this.isLocked || !sheet || !this.selectedChosenKeys.size) return;
    const toMove = sheet.selectedColumns.filter((c: any) => this.selectedChosenKeys.has(c.key));
    const remain = sheet.selectedColumns.filter((c: any) => !this.selectedChosenKeys.has(c.key));
    sheet.selectedColumns = remain;
    sheet.availableColumns = [...sheet.availableColumns, ...toMove]
  }

  get displayedSheets(): any[] {
    const t = this.form?.value?.typeFiche as TypeFiche | null;
    if (t === null || t === undefined) return []; // rien si pas de choix

    switch (t) {
      case 0: return this.sheets.filter(s => s.title?.toLowerCase().includes('étudiant'));
      case 1: return this.sheets.filter(s => s.title?.toLowerCase().includes('enseignant'));
      case 2: return this.sheets.filter(s => s.title?.toLowerCase().includes('entreprise'));
      case 3: return this.sheets; // tout
      default: return [];
    }
  }

  get isLocked(): boolean {
    return !(this.form?.valid);
  }

  getFilteredColumns(t: TypeFiche): {
    title: string;
    availableColumns: { key: string; title: string }[];
    selectedColumns: { key: string; title: string }[];
  }[] {
    if (t === null || t === undefined) return [];

    const keepByType = (title: string) => {
      const tLower = (title || '').toLowerCase();
      switch (t) {
        case 0: return tLower.includes('étudiant');
        case 1: return tLower.includes('enseignant');
        case 2: return tLower.includes('entreprise'); // adapte si libellé différent
        case 3: return true;
        default: return false;
      }
    };

    return this.sheets
      .filter(s => keepByType(s.title))
      .map(s => ({
        title: s.title,
        availableColumns: s.availableColumns.map((c: any) => ({ key: c.key, title: c.title })),
        selectedColumns: s.selectedColumns.map((c: any) => ({ key: c.key, title: c.title })),
      }));
  }

// Optionnel : juste la liste des keys sélectionnés (plat, dans l’ordre)
  getSelectedColumnKeys(t: TypeFiche): string[] {
    return this.getFilteredColumns(t)
      .flatMap(s => s.selectedColumns)
      .map(c => c.key);
  }
}
