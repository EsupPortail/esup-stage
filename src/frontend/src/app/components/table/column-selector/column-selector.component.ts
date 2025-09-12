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

  selectedAvailableKeys = new Set<string>();
  selectedChosenKeys = new Set<string>();

  constructor(
    public dialogRef: MatDialogRef<ColumnSelectorComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.sheets = data.sheets.map((sheet: any) => ({
      title: sheet.title,
      availableColumns: [...sheet.availableColumns].sort(this.sortByTitle),
      selectedColumns: []
    }));
  }

  private sortByTitle = (a: any, b: any) =>
    (a?.title || '').localeCompare(b?.title || '', 'fr', { sensitivity: 'base' });

  get hasMultipleSheets(): boolean {
    return this.sheets.length > 1;
  }

  get currentSheet() {
    return this.sheets[this.selectedSheetIndex];
  }

  // --- Drag & drop
  drop(event: CdkDragDrop<any[]>, sheet: any, isSelectedList: boolean) {
    const fromSelectedList = event.previousContainer.id === 'selectedList';
    const toSelectedList = event.container.id === 'selectedList';

    // Si on reste dans la même liste, on laisse le comportement standard (réordonnancement simple).
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    // On récupère la colonne effectivement dragguée
    const draggedCol = event.item.data as any;

    // Détermine l’ensemble de colonnes à déplacer :
    // - si la colonne dragguée fait partie de la sélection de sa liste, on déplace TOUTE la sélection
    // - sinon, on déplace juste la colonne dragguée
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

    // Références source/target selon le sens
    const source = fromSelectedList ? sheet.selectedColumns : sheet.availableColumns;
    const target = toSelectedList   ? sheet.selectedColumns : sheet.availableColumns;

    // Conserver l’ordre d’apparition des éléments à déplacer
    const toMove = source.filter((c: any) => keysToMove.has(c.key));
    const remain = source.filter((c: any) => !keysToMove.has(c.key));

    // Met à jour la source
    if (fromSelectedList) {
      sheet.selectedColumns = remain;
    } else {
      sheet.availableColumns = remain;
    }

    // Insère le bloc à l’index de drop dans la cible
    const insertAt = event.currentIndex;
    const newTarget = [
      ...target.slice(0, insertAt),
      ...toMove,
      ...target.slice(insertAt)
    ];

    if (toSelectedList) {
      sheet.selectedColumns = newTarget;
    } else {
      // Si on remet dans "disponibles", on retrie A→Z comme avant
      sheet.availableColumns = newTarget.sort(this.sortByTitle);
    }

    // Nettoie / met à jour la sélection
    if (fromSelectedList) {
      this.selectedChosenKeys.clear();
     } else {
      this.selectedAvailableKeys.clear();
     }
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

  // --- Déplacements groupés via flèches
  addSelected(sheet: any) {
    if (!this.selectedAvailableKeys.size) return;
    const toMove = sheet.availableColumns.filter((c: any) => this.selectedAvailableKeys.has(c.key));
    const remain = sheet.availableColumns.filter((c: any) => !this.selectedAvailableKeys.has(c.key));
    sheet.availableColumns = remain;                    // retire de disponibles
    sheet.selectedColumns = [...sheet.selectedColumns, ...toMove]; // ajoute à sélectionnées
  }

  removeSelected(sheet: any) {
    if (!this.selectedChosenKeys.size) return;
    const toMove = sheet.selectedColumns.filter((c: any) => this.selectedChosenKeys.has(c.key));
    const remain = sheet.selectedColumns.filter((c: any) => !this.selectedChosenKeys.has(c.key));
    sheet.selectedColumns = remain;                                          // retire de sélectionnées
    sheet.availableColumns = [...sheet.availableColumns, ...toMove].sort(this.sortByTitle); // remet et trie
  }

  reset(sheet: any) {
    const all = [...sheet.availableColumns, ...sheet.selectedColumns];
    sheet.availableColumns = all.sort(this.sortByTitle);
    sheet.selectedColumns = [];
  }

  cancel() {
    this.dialogRef.close();
  }

  confirm() {
    this.dialogRef.close(this.sheets);
  }
}
