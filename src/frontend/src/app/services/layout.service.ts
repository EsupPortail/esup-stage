import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  private publicLayoutOverrideSubject = new BehaviorSubject<boolean | null>(null);
  publicLayoutOverride$ = this.publicLayoutOverrideSubject.asObservable();

  setPublicLayoutOverride(value: boolean | null): void {
    this.publicLayoutOverrideSubject.next(value);
  }
}
