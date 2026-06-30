import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  private _title: string = '';
  private _subtitle?: string;

  constructor() { }

  get title(): string {
    return this._title;
  }

  set title(value: string) {
    this._title = value;
    this._subtitle = undefined;
  }

  get subtitle(): string|undefined {
    return this._subtitle;
  }

  set subtitle(value: string|undefined) {
    this._subtitle = value;
  }
}
