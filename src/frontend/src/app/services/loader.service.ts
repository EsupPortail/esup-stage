import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoaderService {

  private loadingSubject: BehaviorSubject<boolean>;
  public isLoading: Observable<boolean>;

  constructor() {
    this.loadingSubject = new BehaviorSubject<boolean>(false);
    this.isLoading = this.loadingSubject.asObservable();
  }

  show() {
    this.loadingSubject.next(true);
  }

  hide() {
    this.loadingSubject.next(false);
  }
}
