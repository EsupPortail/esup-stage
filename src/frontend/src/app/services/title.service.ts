import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TitleService {

  private _title = '';
  private _titleTooltip = '';

  constructor() { }

  get title(): string {
    return this._title;
  }

  set title(value: string) {
    this._title = value;
  }

  get titleTooltip(): string {
    return this._titleTooltip;
  }

  set titleTooltip(value: string) {
    this._titleTooltip = value;
  }
}
