import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';

export interface MaintenanceState {
  active: boolean;
  upcoming: boolean;
  alertActive: boolean;
  startDate: string | null;
  endDate: string | null;
  alertDate: string | null;
  message: string | null;
}

const INACTIVE_STATE: MaintenanceState = {
  active: false,
  upcoming: false,
  alertActive: false,
  startDate: null,
  endDate: null,
  alertDate: null,
  message: null,
};

@Injectable({
  providedIn: 'root'
})
export class MaintenanceStateService implements OnDestroy {
  private readonly streamUrl = `${environment.apiUrl}/maintenance/stream`;

  private readonly stateSubject = new BehaviorSubject<MaintenanceState>(INACTIVE_STATE);
  private eventSource?: EventSource;

  state$ = this.stateSubject.asObservable();

  constructor() {}

  start(): void {
    this.connectStream();
  }

  stop(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = undefined;
    }
  }

  ngOnDestroy(): void {
    this.stop();
    this.stateSubject.complete();
  }

  private connectStream(): void {
    if (this.eventSource && this.eventSource.readyState !== EventSource.CLOSED) {
      return;
    }

    this.eventSource = new EventSource(this.streamUrl, { withCredentials: true });

    this.eventSource.addEventListener('maintenance', (event: Event) => {
      const messageEvent = event as MessageEvent<string>;
      this.pushStateFromRawData(messageEvent.data);
    });

    this.eventSource.onmessage = (event: MessageEvent<string>) => {
      this.pushStateFromRawData(event.data);
    };

    this.eventSource.onerror = () => {
      // Browser-native SSE auto-reconnect handles transient failures.
    };
  }

  private pushStateFromRawData(rawData: string): void {
    try {
      const parsed = JSON.parse(rawData) as MaintenanceState;
      this.pushState(parsed);
    } catch {
      // Ignore malformed payload and keep the current state.
    }
  }

  private pushState(state: Partial<MaintenanceState> | null | undefined): void {
    this.stateSubject.next({
      active: !!state?.active,
      upcoming: !!state?.upcoming,
      alertActive: !!state?.alertActive,
      startDate: state?.startDate ?? null,
      endDate: state?.endDate ?? null,
      alertDate: state?.alertDate ?? null,
      message: state?.message ?? null,
    });
  }
}
