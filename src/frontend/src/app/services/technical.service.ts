import { Injectable } from '@angular/core';
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class TechnicalService {

  isMobile = new BehaviorSubject<boolean>(false);
  public static MAX_WIDTH = 768;

  constructor() { }

}
