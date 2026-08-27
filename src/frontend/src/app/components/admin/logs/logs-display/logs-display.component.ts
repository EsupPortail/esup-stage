import { Component, ElementRef, Input, Output, EventEmitter, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

export interface CoreLogLine {
  id: number;
  lineNumber?: number;
  level: string;
  text: string;
  highlighted?: SafeHtml;
}

@Component({
  selector: 'app-logs-display',
  templateUrl: './logs-display.component.html',
  styleUrl: './logs-display.component.scss',
  standalone: false
})
export class LogsDisplayComponent implements OnChanges {
  @ViewChild('logContent') private logContent?: ElementRef<HTMLDivElement>;

  // ── Données ──────────────────────────────────────────────────────────────
  @Input() lines: CoreLogLine[] = [];
  @Input() loading = false;
  @Input() showLineNumbers = false;
  @Input() wrapLines = false;

  // ── Pagination (optionnelle – live n'en a pas) ─────────────────────────
  @Input() page: number | null = null;
  @Input() totalPages: number | null = null;
  @Input() totalLines: number | null = null;
  @Input() pageSize: number | null = null;
  @Output() pageChange = new EventEmitter<'prev' | 'next'>();

  // ── Filtres ──────────────────────────────────────────────────────────────
  readonly levels = ['ERROR', 'WARN', 'INFO', 'DEBUG', 'UNKNOWN'];
  levelFilter: string[] = [];
  searchText = '';

  @Output() filterChange = new EventEmitter<{ levels: string[]; search: string }>();

  // ── Scroll (pour le auto-scroll live) ───────────────────────────────────
  @Output() scrolled = new EventEmitter<void>();

  // ── Lignes filtrées localement ───────────────────────────────────────────
  filteredLines: CoreLogLine[] = [];

  constructor(private readonly sanitizer: DomSanitizer) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['lines']) {
      this.applyFilters();
    }
  }

  toggleLevelFilter(level: string): void {
    const idx = this.levelFilter.indexOf(level);
    idx >= 0 ? this.levelFilter.splice(idx, 1) : this.levelFilter.push(level);
    this.applyFilters();
    this.filterChange.emit({ levels: this.levelFilter, search: this.searchText });
  }

  clearAllLevels(): void {
    this.levelFilter = [];
    this.applyFilters();
    this.filterChange.emit({ levels: [], search: this.searchText });
  }

  onSearchChange(): void {
    this.applyFilters();
    this.filterChange.emit({ levels: this.levelFilter, search: this.searchText });
  }

  clearSearch(): void {
    this.searchText = '';
    this.onSearchChange();
  }

  applyFilters(): void {
    let result = this.lines;
    if (this.levelFilter.length > 0) {
      result = result.filter(l => this.levelFilter.includes(l.level.toUpperCase()));
    }
    if (this.searchText) {
      const q = this.searchText.toLowerCase();
      result = result.filter(l => l.text.toLowerCase().includes(q));
    }
    this.filteredLines = result.map(l => ({
      ...l,
      highlighted: this.highlightLine(l.text)
    }));
  }

  levelClass(level: string): string {
    return 'log-line--' + level.toLowerCase();
  }

  trackLine(index: number, line: CoreLogLine): number {
    return line.id ?? index;
  }

  hasPagination(): boolean {
    return this.page !== null && this.totalPages !== null;
  }

  isNearBottom(threshold = 30): boolean {
    const element = this.logContent?.nativeElement;
    if (!element) {
      return true;
    }
    return (element.scrollHeight - element.scrollTop - element.clientHeight) <= threshold;
  }

  scrollToBottom(): void {
    const element = this.logContent?.nativeElement;
    if (element) {
      element.scrollTop = element.scrollHeight;
    }
  }

  private highlightLine(text: string): SafeHtml {
    let html = this.escapeHtml(text);

    html = html.replace(
      /(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:\d{2})?)/g,
      '<span class="log-date">$1</span>'
    );
    html = html.replace(/\b(ERROR|FATAL)\b/g, '<span class="log-level error">$1</span>');
    html = html.replace(/\b(WARN|WARNING)\b/g, '<span class="log-level warn">$1</span>');
    html = html.replace(/\b(INFO)\b/g, '<span class="log-level info">$1</span>');
    html = html.replace(/\b(DEBUG)\b/g, '<span class="log-level debug">$1</span>');
    html = html.replace(/\b(TRACE)\b/g, '<span class="log-level trace">$1</span>');
    html = html.replace(/(\[[\w\-./: ]+\])/g, '<span class="log-thread">$1</span>');
    html = html.replace(/([\w.]+Exception[\w.]*)/g, '<span class="log-exception">$1</span>');
    html = html.replace(/(\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b)/g, '<span class="log-ip">$1</span>');

    if (this.searchText) {
      const regex = new RegExp(`(${this.escapeRegex(this.searchText)})`, 'gi');
      html = html.replace(regex, '<mark>$1</mark>');
    }
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  private escapeHtml(text: string): string {
    return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  private escapeRegex(text: string): string {
    return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  }
}
