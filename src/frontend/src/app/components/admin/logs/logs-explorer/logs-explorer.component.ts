import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { FileElement } from '../../../../models/file-element.model';
import { FileExplorerService } from '../../../../services/file-explorer.service';

@Component({
  selector: 'app-logs-explorer',
  templateUrl: './logs-explorer.component.html',
  styleUrl: './logs-explorer.component.scss',
  standalone: false
})
export class LogsExplorerComponent implements OnInit {
  @Input() rootPath = '/logs';
  @Output() fileOpened = new EventEmitter<FileElement>();

  currentPath = '';
  breadcrumbs: { name: string; path: string }[] = [];
  fileElements: FileElement[] = [];
  searchIndex: FileElement[] = [];
  searchIndexPath = '';
  loading = false;
  viewMode: 'grid' | 'list' = 'list';
  sortBy: 'name' | 'date' | 'size' = 'date';
  filterText = '';

  constructor(
    private fileService: FileExplorerService,
    private snack: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.navigate(this.rootPath);
  }

  get isAtRoot(): boolean {
    return this.currentPath === this.rootPath;
  }

  get filteredElements(): FileElement[] {
    const filter = this.filterText.trim().toLowerCase();
    let elements = filter ? this.searchIndex : this.fileElements;

    if (filter) {
      elements = elements.filter(element =>
        [element.name, element.extension, element.path]
          .filter(value => value != null)
          .some(value => String(value).toLowerCase().includes(filter))
      );
    }

    return [...elements].sort((left, right) => {
      if (left.isFolder !== right.isFolder) {
        return left.isFolder ? -1 : 1;
      }
      if (this.sortBy === 'name') {
        return left.name.localeCompare(right.name, 'fr', { numeric: true });
      }
      if (this.sortBy === 'date') {
        return this.toTimestamp(right.lastModified) - this.toTimestamp(left.lastModified);
      }
      return (right.size || 0) - (left.size || 0);
    });
  }

  navigate(path: string | null): void {
    const targetPath = this.normalizePath(path);
    const requestPath = targetPath === this.rootPath ? null : targetPath;

    this.loading = true;

    this.fileService.listFolder(requestPath)
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.markForCheck();
      }))
      .subscribe({
        next: elements => {
          this.currentPath = targetPath;
          this.fileElements = (Array.isArray(elements) ? elements : []).map(element => ({
            ...element,
            isFolder: (element as { folder?: boolean }).folder ?? element.isFolder ?? false
          }));
          this.updateBreadcrumbs(targetPath);
          this.searchIndex = [];
          this.searchIndexPath = '';
          if (this.filterText.trim()) {
            void this.refreshSearchIndex(targetPath);
          }
        },
        error: () => {
          this.currentPath = targetPath;
          this.fileElements = [];
          this.searchIndex = [];
          this.searchIndexPath = '';
          this.updateBreadcrumbs(targetPath);
          this.snack.open('Erreur lors du chargement du dossier', 'Fermer', { duration: 3000 });
        }
      });
  }

  navigateUp(): void {
    if (this.isAtRoot) {
      return;
    }

    const parent = this.currentPath.substring(0, this.currentPath.lastIndexOf('/')) || this.rootPath;
    this.navigate(parent);
  }

  onElementClick(element: FileElement): void {
    if (element.isFolder) {
      this.navigate(element.path);
    }
  }

  openElement(element: FileElement): void {
    if (element.isFolder) {
      this.navigate(element.path);
      return;
    }

    this.fileOpened.emit(element);
  }

  clearFilter(event?: MouseEvent): void {
    event?.stopPropagation();
    this.filterText = '';
  }

  onFilterChange(value: string): void {
    this.filterText = value;
    if (!value.trim()) {
      return;
    }

    if (this.searchIndexPath === this.currentPath && this.searchIndex.length > 0) {
      return;
    }

    void this.refreshSearchIndex(this.currentPath);
  }

  openContextMenu(event: MouseEvent, trigger: MatMenuTrigger): void {
    event.preventDefault();
    event.stopPropagation();
    trigger.openMenu();
  }

  downloadElement(element: FileElement): void {
    this.fileService.downloadFile(element.path).subscribe(blob => this.triggerDownload(blob, element.name));
  }

  formatSize(bytes?: number): string {
    if (bytes == null) {
      return '-';
    }
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  getFileIcon(element: FileElement): string {
    const extension = element.extension?.toLowerCase();
    if (['log', 'out', 'err'].includes(extension || '')) {
      return 'article';
    }
    if (extension === 'txt') {
      return 'text_snippet';
    }
    if (extension === 'csv') {
      return 'table_chart';
    }
    return 'insert_drive_file';
  }

  trackByElement(_: number, element: FileElement): string {
    return element.id;
  }

  private normalizePath(path: string | null): string {
    if (!path || path === this.rootPath || path === '/') {
      return this.rootPath;
    }
    return path;
  }

  private toTimestamp(value?: Date): number {
    return value ? new Date(value).getTime() : 0;
  }

  private triggerDownload(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private updateBreadcrumbs(path: string): void {
    const safePath = this.normalizePath(path);
    const parts = safePath.replace(this.rootPath, '')
      .split('/')
      .map(part => part.trim())
      .filter(part => part && part !== '/');
    this.breadcrumbs = [{ name: 'Logs', path: this.rootPath }];

    let current = this.rootPath;
    parts.forEach(part => {
      current += '/' + part;
      this.breadcrumbs.push({ name: part, path: current });
    });
  }

  private async refreshSearchIndex(path: string): Promise<void> {
    this.loading = true;
    this.cdr.markForCheck();

    try {
      this.searchIndex = await this.collectDescendants(path);
      this.searchIndexPath = path;
    } catch {
      this.searchIndex = [];
      this.searchIndexPath = path;
      this.snack.open('Erreur lors de l indexation des fichiers', 'Fermer', { duration: 3000 });
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }

  private async collectDescendants(path: string): Promise<FileElement[]> {
    const requestPath = path === this.rootPath ? null : path;
    const elements = ((await firstValueFrom(this.fileService.listFolder(requestPath))) || []).map(element => ({
      ...element,
      isFolder: (element as { folder?: boolean }).folder ?? element.isFolder ?? false
    }));

    const descendants = await Promise.all(
      elements
        .filter(element => element.isFolder)
        .map(folder => this.collectDescendants(folder.path))
    );

    return [...elements, ...descendants.flat()];
  }
}
