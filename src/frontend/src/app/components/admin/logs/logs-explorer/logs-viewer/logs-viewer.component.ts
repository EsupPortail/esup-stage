import { Component, Inject, Input, OnChanges, OnInit, Optional, SimpleChanges } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { FileElement } from '../../../../../models/file-element.model';
import { FileExplorerService } from '../../../../../services/file-explorer.service';

interface LogLine {
  lineNumber: number;
  raw: string;
  level: 'ERROR' | 'WARN' | 'INFO' | 'DEBUG' | 'UNKNOWN';
  highlighted: SafeHtml;
}

interface LogsViewerDialogData {
  file: FileElement;
}

@Component({
  selector: 'app-logs-viewer',
  templateUrl: './logs-viewer.component.html',
  styleUrl: './logs-viewer.component.scss',
  standalone: false
})
export class LogsViewerComponent implements OnInit, OnChanges {
  @Input() file?: FileElement;

  readonly pageSize = 1000;

  loading = false;
  lines: LogLine[] = [];
  filteredLines: LogLine[] = [];
  totalLines = 0;
  page = 0;
  currentPageSize = this.pageSize;

  searchText = '';
  levelFilter: string[] = [];
  wrapLines = false;
  showLineNumbers = false;

  readonly levels = ['ERROR', 'WARN', 'INFO', 'DEBUG'];

  constructor(
    private fileService: FileExplorerService,
    private sanitizer: DomSanitizer,
    @Optional() @Inject(MAT_DIALOG_DATA) private dialogData?: LogsViewerDialogData,
    @Optional() private dialogRef?: MatDialogRef<LogsViewerComponent>
  ) {}

  get currentFile(): FileElement | undefined {
    return this.file ?? this.dialogData?.file;
  }

  get isDialogMode(): boolean {
    return !!this.dialogRef;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.totalLines / this.currentPageSize));
  }

  get pageStartLine(): number {
    if (this.totalLines === 0) {
      return 0;
    }
    return this.page * this.currentPageSize + 1;
  }

  get pageEndLine(): number {
    if (this.totalLines === 0) {
      return 0;
    }
    return Math.min(this.pageStartLine + this.lines.length - 1, this.totalLines);
  }

  ngOnInit(): void {
    if (this.currentFile) {
      this.resetViewer();
      this.loadPage();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['file'] && this.currentFile) {
      this.resetViewer();
      this.loadPage();
    }
  }

  closeDialog(): void {
    this.dialogRef?.close();
  }

  loadPage(): void {
    const file = this.currentFile;
    if (!file) {
      return;
    }

    this.loading = true;
    this.fileService.getFileContent(file.path, this.page, this.pageSize).subscribe({
      next: (content) => {
        this.totalLines = content.totalLines;
        this.page = content.page ?? this.page;
        this.currentPageSize = content.pageSize || this.pageSize;
        this.lines = this.parseLines(content.content);
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.lines = [];
        this.filteredLines = [];
        this.totalLines = 0;
        this.loading = false;
      }
    });
  }

  prevPage(): void {
    if (this.page === 0 || this.loading) {
      return;
    }
    this.page--;
    this.loadPage();
  }

  nextPage(): void {
    if (this.loading || this.page + 1 >= this.totalPages) {
      return;
    }
    this.page++;
    this.loadPage();
  }

  clearSearch(): void {
    this.searchText = '';
    this.applyFilters();
  }

  applyFilters(): void {
    let result = this.lines;

    if (this.levelFilter.length > 0) {
      result = result.filter(line => this.levelFilter.includes(line.level));
    }

    if (this.searchText) {
      const search = this.searchText.toLowerCase();
      result = result.filter(line => line.raw.toLowerCase().includes(search));
    }

    this.filteredLines = result.map(line => ({
      ...line,
      highlighted: this.highlight(line.raw)
    }));
  }

  levelClass(level: string): string {
    return level.toLowerCase();
  }

  export(format: 'original' | 'csv'): void {
    const file = this.currentFile;
    if (!file) {
      return;
    }

    const name = file.name;
    if (format === 'original') {
      this.fileService.exportSingle(file.path).subscribe(blob => this.triggerDownload(blob, name));
    } else {
      this.fileService.exportAsCsv(file.path).subscribe(
        blob => this.triggerDownload(blob, name.replace(/\.\w+$/, '') + '.csv')
      );
    }
  }

  toggleLevelFilter(level: string): void {
    const index = this.levelFilter.indexOf(level);
    if (index >= 0) {
      this.levelFilter.splice(index, 1);
    } else {
      this.levelFilter.push(level);
    }
    this.applyFilters();
  }

  trackLine(index: number, line: LogLine): number {
    return line.lineNumber || index;
  }

  private resetViewer(): void {
    this.searchText = '';
    this.levelFilter = [];
    this.lines = [];
    this.filteredLines = [];
    this.totalLines = 0;
    this.page = 0;
    this.currentPageSize = this.pageSize;
  }

  private parseLines(raw: string): LogLine[] {
    const firstLineNumber = this.page * this.currentPageSize + 1;
    return raw.split('\n').map((line, index) => ({
      lineNumber: firstLineNumber + index,
      raw: line,
      level: this.detectLevel(line),
      highlighted: this.highlight(line)
    }));
  }

  private detectLevel(line: string): LogLine['level'] {
    if (/\bERROR\b|\bFATAL\b/i.test(line)) return 'ERROR';
    if (/\bWARN\b|\bWARNING\b/i.test(line)) return 'WARN';
    if (/\bINFO\b/i.test(line)) return 'INFO';
    if (/\bDEBUG\b/i.test(line)) return 'DEBUG';
    return 'UNKNOWN';
  }

  private highlight(line: string): SafeHtml {
    let html = this.escapeHtml(line);

    html = html.replace(
      /(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?)/g,
      '<span class="log-date">$1</span>'
    );
    html = html.replace(/\b(ERROR|FATAL)\b/g, '<span class="log-level error">$1</span>');
    html = html.replace(/\b(WARN|WARNING)\b/g, '<span class="log-level warn">$1</span>');
    html = html.replace(/\b(INFO)\b/g, '<span class="log-level info">$1</span>');
    html = html.replace(/\b(DEBUG)\b/g, '<span class="log-level debug">$1</span>');
    html = html.replace(/(\[[\w\-./: ]+\])/g, '<span class="log-thread">$1</span>');
    html = html.replace(/([\w.]+Exception[\w.]*)/g, '<span class="log-exception">$1</span>');
    html = html.replace(/(\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b)/g, '<span class="log-ip">$1</span>');

    if (this.searchText) {
      const regex = new RegExp(`(${this.escapeRegex(this.searchText)})`, 'gi');
      html = html.replace(regex, '<mark>$1</mark>');
    }

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private triggerDownload(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = fileName;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  private escapeHtml(text: string): string {
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  private escapeRegex(text: string): string {
    return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}
