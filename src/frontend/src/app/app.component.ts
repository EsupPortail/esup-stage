import { Component } from '@angular/core';
import { MenuService } from "./services/menu.service";
import { AuthService } from "./services/auth.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  favicon: HTMLLinkElement|null = document.querySelector('#app-favicon');

  constructor(private menuService: MenuService, private authService: AuthService) {
    // TODO get favicon from config
    if (this.favicon !== null) {
      this.favicon.href = 'favicon.ico';
    }
  }

  isOpened(): boolean {
    return this.menuService.navbarOpened;
  }

  isConnected() {
    return this.authService.userConnected;
  }

}
