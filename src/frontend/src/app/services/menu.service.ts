import { Injectable } from '@angular/core';
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  private _navbarOpened = true;
  public currentUrl = new BehaviorSubject<string>('');

  constructor() { }


  get navbarOpened(): boolean {
    return this._navbarOpened;
  }

  set navbarOpened(value: boolean) {
    this._navbarOpened = value;
  }
}
