import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';
import { LogsService, LogStreamLine, LogStreamStatus } from '../../../services/logs.service';

interface DisplayedLogLine {
  id: number;
  level: string;
  text: string;
  searchable: string;
}

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss'],
  standalone: false
})
export class LogsComponent implements OnInit, OnDestroy {

  @ViewChild('logsViewport') logsViewport?: ElementRef<HTMLDivElement>;

  readonly maxLines = 2000;
  readonly levelOptions = [
    { key: 'DEBUG', label: 'DEBUG' },
    { key: 'INFO', label: 'INFO' },
    { key: 'WARN', label: 'WARN' },
    { key: 'ERROR', label: 'ERROR' },
    { key: 'UNKNOWN', label: 'AUTRE' },
  ];

  status: LogStreamStatus = 'disconnected';
  autoScroll = true;
  searchTerm = '';

  levelFilters: Record<string, boolean> = {
    DEBUG: true,
    INFO: true,
    WARN: true,
    ERROR: true,
    UNKNOWN: true,
  };

  private readonly genericLevelPattern = /\b(DEBUG|INFO|WARN|ERROR)\b/i;
  private lines: DisplayedLogLine[] = [];
  private seq = 0;
  private pendingScroll = false;
  private subscriptions: Subscription[] = [];

  constructor(private logsService: LogsService) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.logsService.status$.subscribe(status => {
        this.status = status;
      }),
      this.logsService.lines$.subscribe(line => {
        this.appendLine(line);
      })
    );

    this.logsService.connect();
  }

  ngOnDestroy(): void {
    for (const sub of this.subscriptions) {
      sub.unsubscribe();
    }
    this.logsService.disconnect();
  }

  get displayedLines(): DisplayedLogLine[] {
    const query = this.searchTerm.trim().toLowerCase();
    return this.lines.filter(line => {
      if (!this.levelFilters[line.level]) {
        return false;
      }
      if (!query) {
        return true;
      }
      return line.searchable.includes(query);
    });
  }

  get statusLabel(): string {
    switch (this.status) {
      case 'connected':
        return 'Connecté';
      case 'connecting':
        return 'Connexion en cours';
      case 'forbidden':
        return 'Acces refuse';
      case 'error':
        return 'Erreur de connexion (reconnexion automatique)';
      default:
        return 'Déconnecté';
    }
  }

  get statusClass(): string {
    switch (this.status) {
      case 'connected':
        return 'status-connected';
      case 'connecting':
        return 'status-connecting';
      case 'forbidden':
        return 'status-forbidden';
      case 'error':
        return 'status-error';
      default:
        return 'status-disconnected';
    }
  }

  toggleLevel(level: string, checked: boolean): void {
    this.levelFilters[level] = checked;
  }

  onLogScroll(): void {
    const viewport = this.logsViewport?.nativeElement;
    if (!viewport) {
      return;
    }

    const threshold = 30;
    const distanceToBottom = viewport.scrollHeight - viewport.scrollTop - viewport.clientHeight;
    this.autoScroll = distanceToBottom <= threshold;
  }

  reconnect(): void {
    this.logsService.disconnect();
    this.logsService.connect();
  }

  levelClass(level: string): string {
    return 'level-' + level.toLowerCase();
  }

  trackByLineId(_index: number, line: DisplayedLogLine): number {
    return line.id;
  }

  private appendLine(line: LogStreamLine): void {
    const level = this.extractLevel(line);
    const text = line.raw?.trim() ? line.raw : this.formatFromParts(line, level);

    this.lines.push({
      id: ++this.seq,
      level,
      text,
      searchable: text.toLowerCase(),
    });

    if (this.lines.length > this.maxLines) {
      this.lines.splice(0, this.lines.length - this.maxLines);
    }

    if (this.autoScroll) {
      this.scheduleScrollToBottom();
    }
  }

  private extractLevel(line: LogStreamLine): string {
    const payloadLevel = this.normalizeLevel(line.level);
    if (payloadLevel !== 'UNKNOWN') {
      return payloadLevel;
    }

    const source = `${line.raw ?? ''} ${line.message ?? ''}`;
    const match = this.genericLevelPattern.exec(source);
    return this.normalizeLevel(match?.[1]);
  }

  private normalizeLevel(level: string | null | undefined): string {
    const normalized = (level ?? 'UNKNOWN').toUpperCase().trim();
    if (normalized === 'DEBUG' || normalized === 'INFO' || normalized === 'WARN' || normalized === 'ERROR') {
      return normalized;
    }
    return 'UNKNOWN';
  }

  private formatFromParts(line: LogStreamLine, resolvedLevel: string): string {
    const parts: string[] = [];
    if (line.timestamp) {
      parts.push(line.timestamp);
    }
    parts.push(`[${resolvedLevel}]`);
    if (line.logger) {
      parts.push(line.logger);
    }
    if (line.message) {
      parts.push('-', line.message);
    }
    return parts.join(' ');
  }

  private scheduleScrollToBottom(): void {
    if (this.pendingScroll) {
      return;
    }
    this.pendingScroll = true;

    requestAnimationFrame(() => {
      this.pendingScroll = false;
      const viewport = this.logsViewport?.nativeElement;
      if (!viewport || !this.autoScroll) {
        return;
      }
      viewport.scrollTop = viewport.scrollHeight;
    });
  }
}
