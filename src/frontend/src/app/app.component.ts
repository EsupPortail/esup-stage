import { Component } from '@angular/core';
import { MenuService } from "./services/menu.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  favicon: HTMLLinkElement|null = document.querySelector('#app-favicon');

  constructor(private menuService: MenuService) {
    // TODO get favicon from db
    if (this.favicon !== null) {
      this.favicon.href = 'favicon.ico';
    }
  }

  isOpened(): boolean {
    return this.menuService.navbarOpened;
  }

}
