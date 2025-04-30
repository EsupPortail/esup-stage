import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';

@Component({
  selector: 'app-column-selector',
  templateUrl: './column-selector.component.html',
  styleUrls: ['./column-selector.component.scss']
})
export class ColumnSelectorComponent {

  sheets: any[] = [];
  selectedSheetIndex = 0;
  selectedAvailableColumn: any = null;
  selectedChosenColumn: any = null;

  constructor(
    public dialogRef: MatDialogRef<ColumnSelectorComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.sheets = data.sheets.map((sheet: any) => ({
      title: sheet.title,
      originalOrder: [...sheet.availableColumns],
      availableColumns: [...sheet.availableColumns],
      selectedColumns: []
    }));
  }

  get hasMultipleSheets(): boolean {
    return this.sheets.length > 1;
  }

  get currentSheet() {
    return this.sheets[this.selectedSheetIndex];
  }

  drop(event: CdkDragDrop<any[]>, sheet: any, isSelectedList: boolean) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex
      );

      sheet.availableColumns.sort((a: any, b: any) => {
        return sheet.originalOrder.findIndex((col: any) => col.key === a.key) -
          sheet.originalOrder.findIndex((col: any) => col.key === b.key);
      });
    }
    this.selectedAvailableColumn = null;
    this.selectedChosenColumn = null;
  }

  selectAvailable(col: any) {
    this.selectedAvailableColumn = col;
  }

  selectSelected(col: any) {
    this.selectedChosenColumn = col;
  }

  addColumn(sheet: any) {
    if (!this.selectedAvailableColumn) return;
    sheet.availableColumns = sheet.availableColumns.filter((c: any) => c.key !== this.selectedAvailableColumn.key);
    sheet.selectedColumns.push(this.selectedAvailableColumn);
    this.selectedAvailableColumn = null;
  }

  removeColumn(sheet: any) {
    if (!this.selectedChosenColumn) return;
    sheet.selectedColumns = sheet.selectedColumns.filter((c: any) => c.key !== this.selectedChosenColumn.key);
    sheet.availableColumns.push(this.selectedChosenColumn);

    sheet.availableColumns.sort((a: any, b: any) => {
      return sheet.originalOrder.findIndex((col: any) => col.key === a.key) -
        sheet.originalOrder.findIndex((col: any) => col.key === b.key);
    });

    this.selectedChosenColumn = null;
  }

  reset(sheet: any) {
    sheet.availableColumns.push(...sheet.selectedColumns);
    sheet.selectedColumns = [];
    this.selectedAvailableColumn = null;
    this.selectedChosenColumn = null;

    sheet.availableColumns.sort((a: any, b: any) => {
      return sheet.originalOrder.findIndex((col: any) => col.key === a.key) -
        sheet.originalOrder.findIndex((col: any) => col.key === b.key);
    });
  }

  cancel() {
    this.dialogRef.close();
  }

  confirm() {
    this.dialogRef.close(this.sheets);
  }
}


