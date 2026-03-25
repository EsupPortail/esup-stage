import {Component, Input, SimpleChanges} from '@angular/core';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";
import {FileElement} from "../../../../../models/file-element.model";
import {FileExplorerService} from "../../../../../services/file-explorer.service";

interface LogLine {
  lineNumber: number;
  raw: string;
  level: 'ERROR' | 'WARN' | 'INFO' | 'DEBUG' | 'TRACE' | 'UNKNOWN';
  highlighted: SafeHtml;
}

@Component({
  selector: 'app-logs-viewer',
  templateUrl: './logs-viewer.component.html',
  styleUrl: './logs-viewer.component.scss',
  standalone: false,
})

export class LogsViewerComponent {
  @Input() file?: FileElement;

  loading = false;
  lines: LogLine[] = [];
  filteredLines: LogLine[] = [];
  totalLines = 0;
  page = 0;
  pageSize = 500;

  searchText = '';
  levelFilter: string[] = [];
  wrapLines = false;
  showLineNumbers = true;

  readonly levels = ['ERROR', 'WARN', 'INFO', 'DEBUG', 'TRACE'];

  constructor(
    private fileService: FileExplorerService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['file'] && this.file) {
      this.page = 0;
      this.loadPage();
    }
  }

  loadPage(): void {
    if (!this.file) return;
    this.loading = true;
    this.fileService.getFileContent(this.file.path, this.page, this.pageSize).subscribe({
      next: (content) => {
        this.totalLines = content.totalLines;
        this.lines = this.parseLines(content.content);
        this.applyFilters();
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }

  private parseLines(raw: string): LogLine[] {
    return raw.split('\n').map((line, index) => {
      const lineNum = this.page * this.pageSize + index + 1;
      const level = this.detectLevel(line);
      return {
        lineNumber: lineNum,
        raw: line,
        level,
        highlighted: this.highlight(line)
      };
    });
  }

  private detectLevel(line: string): LogLine['level'] {
    if (/\bERROR\b|\bFATAL\b/i.test(line)) return 'ERROR';
    if (/\bWARN\b|\bWARNING\b/i.test(line)) return 'WARN';
    if (/\bINFO\b/i.test(line)) return 'INFO';
    if (/\bDEBUG\b/i.test(line)) return 'DEBUG';
    if (/\bTRACE\b/i.test(line)) return 'TRACE';
    return 'UNKNOWN';
  }

  private highlight(line: string): SafeHtml {
    let html = this.escapeHtml(line);

    // Dates/timestamps
    html = html.replace(
      /(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?)/g,
      '<span class="log-date">$1</span>'
    );
    // Log levels
    html = html.replace(/\b(ERROR|FATAL)\b/g, '<span class="log-level error">$1</span>');
    html = html.replace(/\b(WARN|WARNING)\b/g, '<span class="log-level warn">$1</span>');
    html = html.replace(/\b(INFO)\b/g, '<span class="log-level info">$1</span>');
    html = html.replace(/\b(DEBUG)\b/g, '<span class="log-level debug">$1</span>');
    html = html.replace(/\b(TRACE)\b/g, '<span class="log-level trace">$1</span>');
    // Thread/class names in brackets
    html = html.replace(/(\[[\w\-./: ]+\])/g, '<span class="log-thread">$1</span>');
    // Exceptions
    html = html.replace(/([\w.]+Exception[\w.]*)/g, '<span class="log-exception">$1</span>');
    // IP addresses
    html = html.replace(/(\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b)/g, '<span class="log-ip">$1</span>');
    // Search highlight
    if (this.searchText) {
      const regex = new RegExp(`(${this.escapeRegex(this.searchText)})`, 'gi');
      html = html.replace(regex, '<mark>$1</mark>');
    }

    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  applyFilters(): void {
    let result = this.lines;
    if (this.levelFilter.length > 0) {
      result = result.filter(l => this.levelFilter.includes(l.level));
    }
    if (this.searchText) {
      result = result.filter(l => l.raw.toLowerCase().includes(this.searchText.toLowerCase()));
    }
    this.filteredLines = result;
    // Re-highlight with search
    if (this.searchText) {
      this.filteredLines = result.map(l => ({
        ...l,
        highlighted: this.highlight(l.raw)
      }));
    }
  }

  prevPage(): void {
    if (this.page > 0) { this.page--; this.loadPage(); }
  }

  nextPage(): void {
    if ((this.page + 1) * this.pageSize < this.totalLines) { this.page++; this.loadPage(); }
  }

  get totalPages(): number {
    return Math.ceil(this.totalLines / this.pageSize);
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

  levelClass(level: string): string {
    return level.toLowerCase();
  }

  export(format: 'original' | 'zip' | 'csv'): void {
    if (!this.file) return;
    const name = this.file.name;
    if (format === 'original') {
      this.fileService.exportSingle(this.file.path).subscribe(blob => this.triggerDownload(blob, name));
    } else if (format === 'zip') {
      this.fileService.exportAsZip([this.file.path], name.replace(/\.\w+$/, '')).subscribe(
        blob => this.triggerDownload(blob, name.replace(/\.\w+$/, '') + '.zip')
      );
    } else {
      this.fileService.exportAsCsv(this.file.path).subscribe(
        blob => this.triggerDownload(blob, name.replace(/\.\w+$/, '') + '.csv')
      );
    }
  }

  private triggerDownload(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url; a.download = fileName; a.click();
    URL.revokeObjectURL(url);
  }

  trackLine(index: number, line: { lineNumber: number }): number {
    return line.lineNumber;
  }

  toggleLevelFilter(level: string): void {
    const idx = this.levelFilter.indexOf(level);
    if (idx >= 0) {
      this.levelFilter.splice(idx, 1);
    } else {
      this.levelFilter.push(level);
    }
    this.applyFilters();
  }

  isLevelActive(level: string): boolean {
    return this.levelFilter.length === 0 || this.levelFilter.includes(level);
  }
}
