import { Injectable, OnDestroy } from '@angular/core';
import {BehaviorSubject, Observable, pipe, Subject} from 'rxjs';
import { environment } from '../../environments/environment';
import {HttpClient} from "@angular/common/http";
import {LoggerLevel} from "../models/LoggerLevel.model";
import {catchError} from "rxjs/operators";

export type LogStreamStatus = 'disconnected' | 'connecting' | 'connected' | 'error' | 'forbidden';

export interface LogStreamLine {
  timestamp: string | null;
  thread: string | null;
  level: string;
  logger: string | null;
  message: string;
  raw: string;
  historical: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class LogsService implements OnDestroy {

  private readonly streamUrl = `${environment.apiUrl}/admin/logs/stream`;
  private readonly adminProbeUrl = `${environment.apiUrl}/admin/config/missing`;
  private readonly logsConfigUrl = `${environment.apiUrl}/admin/logs`;
  private readonly rawLogPattern = /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([^\]]+)]\s+([A-Za-z]+)\s+([^\s]+)\s+-\s+(.*)$/;
  private readonly recordLevelPattern = /\blevel=([A-Z]+)\b/;
  private readonly recordHistoricalPattern = /\bhistorical=(true|false)\b/;
  private readonly genericLevelPattern = /\b(DEBUG|INFO|WARN|ERROR)\b/i;

  private readonly linesSubject = new Subject<LogStreamLine>();
  private readonly statusSubject = new BehaviorSubject<LogStreamStatus>('disconnected');

  private eventSource?: EventSource;
  private manualClose = false;
  private probingForbidden = false;

  lines$: Observable<LogStreamLine> = this.linesSubject.asObservable();
  status$: Observable<LogStreamStatus> = this.statusSubject.asObservable();

  constructor(private http: HttpClient) {}

  connect(): void {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      return;
    }

    this.manualClose = false;
    this.statusSubject.next('connecting');

    this.eventSource = new EventSource(this.streamUrl, { withCredentials: true });

    this.eventSource.onopen = () => {
      this.statusSubject.next('connected');
    };

    this.eventSource.onmessage = (event: MessageEvent<string>) => {
      const line = this.parseLine(event.data);
      this.linesSubject.next(line);
    };

    this.eventSource.onerror = () => {
      if (this.manualClose) {
        return;
      }

      if (this.statusSubject.getValue() !== 'forbidden') {
        this.statusSubject.next('error');
      }
      this.probeForbiddenStatus();
    };
  }

  disconnect(): void {
    this.closeStream('disconnected');
  }

  ngOnDestroy(): void {
    this.closeStream('disconnected');
    this.linesSubject.complete();
    this.statusSubject.complete();
  }

  private closeStream(status: LogStreamStatus): void {
    this.manualClose = true;

    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }

    this.statusSubject.next(status);
  }

  private async probeForbiddenStatus(): Promise<void> {
    if (this.probingForbidden) {
      return;
    }

    this.probingForbidden = true;
    try {
      const response = await fetch(this.adminProbeUrl, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Accept': 'application/json'
        }
      });

      if (response.status === 401 || response.status === 403) {
        this.closeStream('forbidden');
      }
    } catch {
      // Keep browser-native SSE auto-reconnect behaviour on network errors.
    } finally {
      this.probingForbidden = false;
    }
  }

  private parseLine(rawData: string): LogStreamLine {
    try {
      const payload = JSON.parse(rawData) as Partial<LogStreamLine>;
      const raw = typeof payload.raw === 'string' ? payload.raw : rawData;
      return {
        timestamp: payload.timestamp ?? null,
        thread: payload.thread ?? null,
        level: this.normalizeLevel(payload.level),
        logger: payload.logger ?? null,
        message: payload.message ?? raw,
        raw,
        historical: payload.historical ?? false,
      };
    } catch {
      return this.parseFallback(rawData);
    }
  }

  private parseFallback(rawData: string): LogStreamLine {
    const text = (rawData || '').trim();

    const rawLogMatch = this.rawLogPattern.exec(text);
    if (rawLogMatch) {
      return {
        timestamp: rawLogMatch[1],
        thread: rawLogMatch[2],
        level: this.normalizeLevel(rawLogMatch[3]),
        logger: rawLogMatch[4],
        message: rawLogMatch[5],
        raw: text,
        historical: false,
      };
    }

    const recordLevelMatch = this.recordLevelPattern.exec(text);
    const genericLevelMatch = this.genericLevelPattern.exec(text);
    const historicalMatch = this.recordHistoricalPattern.exec(text);

    return {
      timestamp: null,
      thread: null,
      level: this.normalizeLevel(recordLevelMatch?.[1] ?? genericLevelMatch?.[1]),
      logger: null,
      message: text,
      raw: text,
      historical: historicalMatch?.[1] === 'true',
    };
  }

  private normalizeLevel(level: string | null | undefined): string {
    const normalized = (level ?? 'UNKNOWN').toUpperCase().trim();
    if (normalized === 'DEBUG' || normalized === 'INFO' || normalized === 'WARN' || normalized === 'ERROR') {
      return normalized;
    }
    return 'UNKNOWN';
  }

  getLoggersInfo(): Observable<LoggerLevel[]> {
    return this.http.get<LoggerLevel[]>(this.logsConfigUrl);
  }

  updateLoggers(packageNames: string[], level: string): Observable<void> {
    const body = { packageNames, level };
    return this.http.post<void>(this.logsConfigUrl, body)
  }

}
