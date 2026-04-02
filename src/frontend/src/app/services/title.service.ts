import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  private _title: string = '';

  constructor() { }

  get title(): string {
    return this._title;
  }

  set title(value: string) {
    this._title = value;
  }
}
