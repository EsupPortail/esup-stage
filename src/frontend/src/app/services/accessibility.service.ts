import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

interface AccessibilityPreferences {
  disableAutoSearch: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AccessibilityService {
  private disableAutoSearchSubject = new BehaviorSubject<boolean>(false);
  readonly disableAutoSearch$ = this.disableAutoSearchSubject.asObservable();

  constructor() {
    this.loadFromStorage();
  }

  setDisableAutoSearch(value: boolean): void {
    this.disableAutoSearchSubject.next(!!value);
  }

  private loadFromStorage(): void {
    const saved = localStorage.getItem('accessibilityPreferences');
    if (!saved) return;

    try {
      const preferences = JSON.parse(saved) as AccessibilityPreferences;
      if (preferences.disableAutoSearch !== undefined) {
        this.disableAutoSearchSubject.next(!!preferences.disableAutoSearch);
      }
    } catch {}
  }
}
