import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class MenuService {

  private _navbarOpened = true;

  constructor() { }


  get navbarOpened(): boolean {
    return this._navbarOpened;
  }

  set navbarOpened(value: boolean) {
    this._navbarOpened = value;
  }
}
