import { Component } from '@angular/core';
import { MenuService } from "./services/menu.service";
import { AuthService } from "./services/auth.service";
import { Role } from "./constants/role";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  favicon: HTMLLinkElement | null = document.querySelector('#app-favicon');

  items: any[] = [
    {
      libelle: 'Tableau de bord',
      path: 'tableau-de-bord',
      icon: 'fa-columns',
      canView: () => {
        return this.authService.checkRights([Role.ADM.code, Role.RESP_GES.code, Role.GES.code, Role.ENS.code, Role.ETU.code, Role.OBS.code])
      }
    },
    {
      libelle: 'Utilisateurs',
      path: 'utilisateurs',
      icon: 'fa-users',
      canView: () => {
        return this.authService.checkRights([Role.ADM.code, Role.ADM_TECH.code])
      }
    },
  ]

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

  getAppVersion(): string {
    return this.authService.appVersion;
  }

}
