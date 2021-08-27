import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class PersonnalisationService {

  // TODO récupération de la config côté db
  config: any = {};

  constructor() { }

  // TODO ne pas écraser les valeurs par défauts si non existante
  updateConfig(config: any): void {
    this.config = config;
    for (const style of Object.keys(this.config)) {
      if (this.config[style]) {
        document.documentElement.style.setProperty(`--${style}`, this.config[style]);
      }
    }
  }

}
