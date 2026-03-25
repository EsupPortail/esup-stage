import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {LogsService, LogStreamLine, LogStreamStatus} from "../../../../services/logs.service";
import {MatTableDataSource} from "@angular/material/table";
import {SelectionModel} from "@angular/cdk/collections";
import {LoggerLevel} from "../../../../models/logger-level.model";
import {MatTreeFlatDataSource, MatTreeFlattener} from "@angular/material/tree";
import {FlatLoggerNode} from "../../../../models/flat-logger-node.model";
import {LoggerNode} from "../../../../models/logger-node.model";
import {FlatTreeControl} from "@angular/cdk/tree";
import {DisplayedLogLine} from "../../../../models/displayed-log-line.model";
import {Subscription} from "rxjs";
import {AuthService} from "../../../../services/auth.service";


@Component({
  selector: 'app-logs-live',
  templateUrl: './logs-live.component.html',
  styleUrl: './logs-live.component.scss',
  standalone: false
})
export class LogsLiveComponent implements OnInit, AfterViewInit, OnDestroy{
  @ViewChild('logsViewport') logsViewport?: ElementRef<HTMLDivElement>;
  @ViewChild('sortLoggers') sortLoggers?: MatSort;
  @ViewChild('paginatorLoggers') paginatorLoggers?: MatPaginator;

  readonly maxLines = 2000;
  readonly levelOptions = [
    { key: 'DEBUG',   label: 'DEBUG' },
    { key: 'INFO',    label: 'INFO'  },
    { key: 'WARN',    label: 'WARN'  },
    { key: 'ERROR',   label: 'ERROR' },
    { key: 'UNKNOWN', label: 'AUTRE' },
  ];
  readonly displayedColumnsLoggers: string[] = ['select', 'name', 'configuredLevel', 'effectiveLevel'];
  readonly logLevels: string[] = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'];

  status: LogStreamStatus = 'disconnected';
  autoScroll = true;
  searchTerm = '';
  selectedLogLevel: string | null = null;
  loggerFilterText = '';

  dataSourceLoggers = new MatTableDataSource<LoggerLevel>([]);
  selectionLoggers  = new SelectionModel<FlatLoggerNode>(true, []);

  levelFilters: Record<string, boolean> = {
    DEBUG: true, INFO: true, WARN: true, ERROR: true, UNKNOWN: true,
  };

  // ─── Tree ──────────────────────────────────────────────────────────────────

  private _treeFlattener = new MatTreeFlattener<LoggerNode, FlatLoggerNode>(
    (node, lvl) => ({
      label:      node.label,
      fullName:   node.fullName,
      logger:     node.logger,
      level:      lvl,
      expandable: node.children.length > 0,
    }),
    n => n.level,
    n => n.expandable,
    n => n.children
  );

  treeControl = new FlatTreeControl<FlatLoggerNode>(n => n.level, n => n.expandable);
  dataSource  = new MatTreeFlatDataSource(this.treeControl, this._treeFlattener);

  hasChild = (_: number, n: FlatLoggerNode) => n.expandable;

  // ──────────────────────────────────────────────────────────────────────────

  private readonly genericLevelPattern = /\b(DEBUG|INFO|WARN|ERROR)\b/i;
  private lines: DisplayedLogLine[] = [];
  private allLoggers: LoggerLevel[] = [];
  private seq = 0;
  private pendingScroll = false;
  private subscriptions: Subscription[] = [];

  constructor(
    private logsService: LogsService,
    private authService: AuthService,
  ) {
    this.dataSourceLoggers.filterPredicate = (data: LoggerLevel, filter: string): boolean => {
      const f = filter.trim().toLowerCase();
      if (!f) return true;
      return [data.name, data.configuredLevel, data.effectiveLevel]
        .filter(v => v != null)
        .some(v => String(v).toLowerCase().includes(f));
    };
  }

  // ─── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.subscriptions.push(
      this.logsService.status$.subscribe(s => { this.status = s; }),
      this.logsService.lines$.subscribe(l => { this.appendLine(l); })
    );
    this.logsService.connect();
    if (this.isAdminTech()) this.refreshLoggers();
  }

  ngAfterViewInit(): void {
    this.bindLoggersTableControls();
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
    this.logsService.disconnect();
  }

  // ─── Getters ───────────────────────────────────────────────────────────────

  get displayedLines(): DisplayedLogLine[] {
    const query = this.searchTerm.trim().toLowerCase();
    return this.lines.filter(line => {
      if (!this.levelFilters[line.level]) return false;
      return !query || line.searchable.includes(query);
    });
  }

  get statusLabel(): string {
    const labels: Record<string, string> = {
      connected:    'Connecté',
      connecting:   'Connexion en cours',
      forbidden:    'Accès refusé',
      error:        'Erreur de connexion (reconnexion automatique)',
      disconnected: 'Déconnecté',
    };
    return labels[this.status] ?? 'Déconnecté';
  }

  get statusClass(): string {
    const classes: Record<string, string> = {
      connected:    'status-connected',
      connecting:   'status-connecting',
      forbidden:    'status-forbidden',
      error:        'status-error',
      disconnected: 'status-disconnected',
    };
    return classes[this.status] ?? 'status-disconnected';
  }

  // ─── Auth ──────────────────────────────────────────────────────────────────

  isAdminTech(): boolean {
    return this.authService.isAdminTech();
  }

  // ─── Loggers CRUD ──────────────────────────────────────────────────────────

  refreshLoggers(): void {
    this.logsService.getLoggersInfo().subscribe({
      next: (loggers: LoggerLevel[]) => {
        this.allLoggers = Array.isArray(loggers) ? loggers : [];
        this.dataSourceLoggers.data = this.allLoggers;
        this.selectionLoggers.clear();
        this.selectedLogLevel = null;
        this.bindLoggersTableControls();
        setTimeout(() => this.buildTree(), 0);
      },
      error: err => console.error('gestion erreur refresh loggers', err),
    });
  }

  updateSelectedLoggers(): void {
    if (!this.selectedLogLevel || this.selectionLoggers.isEmpty()) return;
    const names = this.selectionLoggers.selected
      .filter(n => n.logger?.name)
      .map(n => n.logger!.name as string);
    if (!names.length) return;
    this.logsService.updateLoggers(names, this.selectedLogLevel).subscribe({
      next: () => this.refreshLoggers(),
      error: err => console.error('gestion erreur update loggers', err),
    });
  }

  // ─── Filter ────────────────────────────────────────────────────────────────

  applyFilter(event: Event): void {
    const value = ((event.target as HTMLInputElement)?.value ?? '').trim();
    this.loggerFilterText = value;
    this.dataSourceLoggers.filter = value.toLowerCase();
    this.selectionLoggers.clear();
    if (this.dataSourceLoggers.paginator) this.dataSourceLoggers.paginator.firstPage();
    this.buildTree();
  }

  // ─── Tree build ────────────────────────────────────────────────────────────

  buildTree(): void {
    const filter = this.loggerFilterText.toLowerCase();

    // Mémorise les branches ouvertes avant reconstruction
    const expandedFullNames = new Set<string>(
      (this.treeControl.dataNodes ?? [])
        .filter(n => n.expandable && this.treeControl.isExpanded(n))
        .map(n => n.fullName)
    );

    const filtered = filter
      ? this.allLoggers.filter(l => l.name?.toLowerCase().includes(filter))
      : this.allLoggers;

    this.dataSource.data = this.buildNodes(filtered);

    if (filter) {
      // Filtre actif → tout déplier pour voir les résultats
      this.treeControl.expandAll();
    } else {
      // Restaure exactement les branches qui étaient ouvertes
      this.treeControl.dataNodes
        .filter(n => n.expandable && expandedFullNames.has(n.fullName))
        .forEach(n => this.treeControl.expand(n));
    }
  }

  private buildNodes(loggers: LoggerLevel[]): LoggerNode[] {
    const root: LoggerNode = { label: '__root__', fullName: '', children: [] };
    for (const logger of loggers) {
      if (!logger.name) continue;
      const parts = logger.name.split('.');
      let current = root;
      for (let i = 0; i < parts.length; i++) {
        const segment  = parts[i];
        const fullName = parts.slice(0, i + 1).join('.');
        let child = current.children.find(c => c.label === segment);
        if (!child) {
          child = { label: segment, fullName, children: [] };
          current.children.push(child);
        }
        if (fullName === logger.name) child.logger = logger;
        current = child;
      }
    }
    return root.children;
  }

  // ─── Tree selection ────────────────────────────────────────────────────────

  private getDescendantLeaves(node: FlatLoggerNode): FlatLoggerNode[] {
    const idx = this.treeControl.dataNodes.indexOf(node);
    const results: FlatLoggerNode[] = [];
    for (let i = idx + 1; i < this.treeControl.dataNodes.length; i++) {
      const n = this.treeControl.dataNodes[i];
      if (n.level <= node.level) break;
      if (n.logger) results.push(n);
    }
    return results;
  }

  private get selectableNodes(): FlatLoggerNode[] {
    return this.treeControl.dataNodes.filter(n => !!n.logger);
  }

  toggleNode(node: FlatLoggerNode): void {
    if (node.expandable) {
      const leaves = this.getDescendantLeaves(node);
      const allSel = leaves.every(n => this.selectionLoggers.isSelected(n));
      allSel ? this.selectionLoggers.deselect(...leaves) : this.selectionLoggers.select(...leaves);
    } else if (node.logger) {
      this.selectionLoggers.toggle(node);
    }
  }

  isNodeSelected(node: FlatLoggerNode): boolean {
    if (node.logger) return this.selectionLoggers.isSelected(node);
    const leaves = this.getDescendantLeaves(node);
    return leaves.length > 0 && leaves.every(n => this.selectionLoggers.isSelected(n));
  }

  isNodeIndeterminate(node: FlatLoggerNode): boolean {
    if (!node.expandable) return false;
    const leaves = this.getDescendantLeaves(node);
    const some = leaves.some(n => this.selectionLoggers.isSelected(n));
    const all  = leaves.every(n => this.selectionLoggers.isSelected(n));
    return some && !all;
  }

  toggleAll(): void {
    const all = this.selectableNodes;
    const allSel = all.every(n => this.selectionLoggers.isSelected(n));
    allSel ? this.selectionLoggers.deselect(...all) : this.selectionLoggers.select(...all);
  }

  isAllSelected(): boolean {
    const all = this.selectableNodes;
    return all.length > 0 && all.every(n => this.selectionLoggers.isSelected(n));
  }

  isSomeSelected(): boolean {
    return this.selectionLoggers.hasValue() && !this.isAllSelected();
  }

  // ─── Level helpers ─────────────────────────────────────────────────────────

  levelColor(level: string | null | undefined): string {
    const colors: Record<string, string> = {
      TRACE: '#9c27b0', DEBUG: '#2196f3', INFO:  '#4caf50',
      WARN:  '#ff9800', ERROR: '#f44336', OFF:   '#9e9e9e',
    };
    return colors[level ?? ''] ?? '#bdbdbd';
  }

  levelClass(level: string): string {
    return 'level-' + level.toLowerCase();
  }

  // ─── Log stream ────────────────────────────────────────────────────────────

  toggleLevel(level: string, checked: boolean): void {
    this.levelFilters[level] = checked;
  }

  onLogScroll(): void {
    const vp = this.logsViewport?.nativeElement;
    if (!vp) return;
    this.autoScroll = (vp.scrollHeight - vp.scrollTop - vp.clientHeight) <= 30;
  }

  reconnect(): void {
    this.logsService.disconnect();
    this.logsService.connect();
  }

  trackByLineId(_: number, line: DisplayedLogLine): number {
    return line.id;
  }

  // ─── Private ───────────────────────────────────────────────────────────────

  private appendLine(line: LogStreamLine): void {
    const level = this.extractLevel(line);
    const text  = line.raw?.trim() ? line.raw : this.formatFromParts(line, level);
    this.lines.push({ id: ++this.seq, level, text, searchable: text.toLowerCase() });
    if (this.lines.length > this.maxLines) this.lines.splice(0, this.lines.length - this.maxLines);
    if (this.autoScroll) this.scheduleScrollToBottom();
  }

  private bindLoggersTableControls(): void {
    if (this.sortLoggers)      this.dataSourceLoggers.sort      = this.sortLoggers;
    if (this.paginatorLoggers) this.dataSourceLoggers.paginator = this.paginatorLoggers;
  }

  private extractLevel(line: LogStreamLine): string {
    const p = this.normalizeLevel(line.level);
    if (p !== 'UNKNOWN') return p;
    const match = this.genericLevelPattern.exec(`${line.raw ?? ''} ${line.message ?? ''}`);
    return this.normalizeLevel(match?.[1]);
  }

  private normalizeLevel(level: string | null | undefined): string {
    const n = (level ?? '').toUpperCase().trim();
    return ['DEBUG', 'INFO', 'WARN', 'ERROR'].includes(n) ? n : 'UNKNOWN';
  }

  private formatFromParts(line: LogStreamLine, resolvedLevel: string): string {
    const parts: string[] = [];
    if (line.timestamp) parts.push(line.timestamp);
    parts.push(`[${resolvedLevel}]`);
    if (line.logger)  parts.push(line.logger);
    if (line.message) parts.push('-', line.message);
    return parts.join(' ');
  }

  private scheduleScrollToBottom(): void {
    if (this.pendingScroll) return;
    this.pendingScroll = true;
    requestAnimationFrame(() => {
      this.pendingScroll = false;
      const vp = this.logsViewport?.nativeElement;
      if (vp && this.autoScroll) vp.scrollTop = vp.scrollHeight;
    });
  }
}
