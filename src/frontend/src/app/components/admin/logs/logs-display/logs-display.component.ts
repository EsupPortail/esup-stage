import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
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
  standalone: false
})
export class LogsDisplayComponent implements OnChanges {
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
  readonly levels = ['ERROR', 'WARN', 'INFO', 'DEBUG'];
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
    // Applique le highlight de recherche si la ligne n'en a pas déjà un
    this.filteredLines = result.map(l => ({
      ...l,
      highlighted: l.highlighted ?? this.highlightSearch(l.text)
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

  private highlightSearch(text: string): SafeHtml {
    let html = this.escapeHtml(text);
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
