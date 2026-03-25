import {Component, EventEmitter, Input, Output} from '@angular/core';
import {FileElement} from "../../../../models/file-element.model";
import {FileExplorerService} from "../../../../services/file-explorer.service";
import {MatDialog} from "@angular/material/dialog";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatMenuTrigger} from "@angular/material/menu";

@Component({
  selector: 'app-logs-explorer',
  templateUrl: './logs-explorer.component.html',
  styleUrl: './logs-explorer.component.scss',
  standalone: false
})
export class LogsExplorerComponent {

  @Input() rootPath = '/logs';
  @Output() fileOpened = new EventEmitter<FileElement>();

  currentPath = '';
  breadcrumbs: { name: string; path: string }[] = [];
  fileElements: FileElement[] = [];
  selectedElements = new Set<string>();
  loading = false;
  viewMode: 'grid' | 'list' = 'grid';
  sortBy: 'name' | 'date' | 'size' = 'name';
  filterText = '';

  constructor(
    private fileService: FileExplorerService,
    private dialog: MatDialog,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.navigate(null);
  }

  navigate(path: any): void {
    this.loading = true;
    this.selectedElements.clear();
    this.fileService.listFolder(path).subscribe({
      next: (elements) => {
        this.currentPath = path;
        this.fileElements = elements;
        this.updateBreadcrumbs(path);
        this.loading = false;
      },
      error: () => {
        this.snack.open('Erreur lors du chargement du dossier', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  navigateUp(): void {
    const parent = this.currentPath.substring(0, this.currentPath.lastIndexOf('/')) || this.rootPath;
    this.navigate(parent);
  }

  onElementClick(element: FileElement): void {
    if (element.isFolder) {
      this.navigate(element.path);
    } else {
      this.fileOpened.emit(element);
    }
  }

  toggleSelect(element: FileElement, event: MouseEvent): void {
    event.stopPropagation();
    if (this.selectedElements.has(element.id)) {
      this.selectedElements.delete(element.id);
    } else {
      this.selectedElements.add(element.id);
    }
  }

  isSelected(element: FileElement): boolean {
    return this.selectedElements.has(element.id);
  }

  get filteredElements(): FileElement[] {
    let els = this.fileElements;
    if (this.filterText) {
      els = els.filter(e => e.name.toLowerCase().includes(this.filterText.toLowerCase()));
    }
    return [...els].sort((a, b) => {
      if (a.isFolder !== b.isFolder) return a.isFolder ? -1 : 1;
      if (this.sortBy === 'name') return a.name.localeCompare(b.name);
      if (this.sortBy === 'date') return new Date(b.lastModified!).getTime() - new Date(a.lastModified!).getTime();
      if (this.sortBy === 'size') return (b.size || 0) - (a.size || 0);
      return 0;
    });
  }

  openContextMenu(event: MouseEvent, trigger: MatMenuTrigger): void {
    event.preventDefault();
    trigger.openMenu();
  }

  // openNewFolderDialog(): void {
  //   const ref = this.dialog.open(NewFolderDialogComponent);
  //   ref.afterClosed().subscribe(name => {
  //     if (name) {
  //       this.fileService.createFolder(this.currentPath, name).subscribe({
  //         next: () => { this.navigate(this.currentPath); this.snack.open('Dossier créé', '', { duration: 2000 }); },
  //         error: () => this.snack.open('Erreur création dossier', 'Fermer', { duration: 3000 })
  //       });
  //     }
  //   });
  // }
  //
  // openRenameDialog(element: FileElement): void {
  //   const ref = this.dialog.open(RenameDialogComponent, { data: { currentName: element.name } });
  //   ref.afterClosed().subscribe(newName => {
  //     if (newName) {
  //       this.fileService.renameElement(element.path, newName).subscribe({
  //         next: () => { this.navigate(this.currentPath); this.snack.open('Renommé', '', { duration: 2000 }); },
  //         error: () => this.snack.open('Erreur renommage', 'Fermer', { duration: 3000 })
  //       });
  //     }
  //   });
  // }
  //
  // deleteElement(element: FileElement): void {
  //   if (!confirm(`Supprimer "${element.name}" ?`)) return;
  //   this.fileService.deleteElement(element.path).subscribe({
  //     next: () => { this.navigate(this.currentPath); this.snack.open('Supprimé', '', { duration: 2000 }); },
  //     error: () => this.snack.open('Erreur suppression', 'Fermer', { duration: 3000 })
  //   });
  // }
  //
  // openExportDialog(element?: FileElement): void {
  //   // Si appelé sans argument, exporte la sélection en cours
  //   const targets = element
  //     ? [element]
  //     : this.fileElements.filter(e => this.selectedElements.has(e.id));
  //
  //   if (targets.length === 0) {
  //     this.snack.open('Sélectionnez au moins un fichier', '', { duration: 2000 });
  //     return;
  //   }
  //
  //   const isSingleFile = targets.length === 1 && !targets[0].isFolder;
  //   const isLogFile = isSingleFile && /\.(log|txt|out|err)$/i.test(targets[0].name);
  //
  //   const ref = this.dialog.open(ExportDialogComponent, {
  //     data: {
  //       fileNames: targets.map(t => t.name),
  //       isSingleFile,
  //       isLogFile
  //     } as ExportDialogData,
  //     width: '420px'
  //   });
  //
  //   ref.afterClosed().subscribe((result: ExportDialogResult | undefined) => {
  //     if (!result) return;
  //
  //     const paths = targets.map(t => t.path);
  //     const firstName = targets[0].name;
  //
  //     if (result.format === 'original') {
  //       this.fileService.exportSingle(paths[0]).subscribe(blob =>
  //         this.triggerDownload(blob, firstName)
  //       );
  //     } else if (result.format === 'zip') {
  //       this.fileService.exportAsZip(paths, result.zipName).subscribe(blob =>
  //         this.triggerDownload(blob, (result.zipName || 'export') + '.zip')
  //       );
  //     } else if (result.format === 'csv') {
  //       this.fileService.exportAsCsv(paths[0]).subscribe(blob =>
  //         this.triggerDownload(blob, firstName.replace(/\.\w+$/, '') + '.csv')
  //       );
  //     }
  //   });
  // }
  //
  private triggerDownload(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }
  //
  // openZipDialog(): void {
  //   const selectedPaths = this.fileElements
  //     .filter(e => this.selectedElements.has(e.id))
  //     .map(e => e.path);
  //   if (selectedPaths.length === 0) {
  //     this.snack.open('Sélectionnez au moins un fichier', '', { duration: 2000 });
  //     return;
  //   }
  //   const ref = this.dialog.open(ZipDialogComponent, { data: { paths: selectedPaths, currentPath: this.currentPath } });
  //   ref.afterClosed().subscribe(zipName => {
  //     if (zipName) {
  //       this.fileService.zipElements(selectedPaths, `${this.currentPath}/${zipName}.zip`).subscribe({
  //         next: () => { this.navigate(this.currentPath); this.snack.open('Archive créée', '', { duration: 2000 }); },
  //         error: () => this.snack.open('Erreur création archive', 'Fermer', { duration: 3000 })
  //       });
  //     }
  //   });
  // }

  downloadElement(element: FileElement): void {
    this.fileService.downloadFile(element.path).subscribe(blob => {
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = element.name;
      a.click();
      URL.revokeObjectURL(url);
    });
  }

  formatSize(bytes?: number): string {
    if (!bytes) return '-';
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  private updateBreadcrumbs(path: string): void {
    const parts = path.replace(this.rootPath, '').split('/').filter(Boolean);
    this.breadcrumbs = [{ name: 'Logs', path: this.rootPath }];
    let acc = this.rootPath;
    parts.forEach(part => {
      acc += '/' + part;
      this.breadcrumbs.push({ name: part, path: acc });
    });
  }
}
