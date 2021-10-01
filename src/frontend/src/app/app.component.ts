import { Component } from '@angular/core';
import { MenuService } from "./services/menu.service";
import { AuthService } from "./services/auth.service";
import { Role } from "./constants/role";
import { AppFonction } from "./constants/app-fonction";
import { Droit } from "./constants/droit";
import { environment } from "../environments/environment";
import { ConfigService } from "./services/config.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  favicon: HTMLLinkElement | null = document.querySelector('#app-favicon');
  theme: HTMLLinkElement | null = document.querySelector('#theme');

  items: any[] = [
    {
      libelle: 'Tableau de bord',
      path: 'tableau-de-bord',
      icon: 'fa-columns',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.CONVENTION, droits: [Droit.LECTURE]})
      }
    },
    {
      libelle: 'Paramétrage de l\'application',
      path: 'param-global',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]})
      },
      children: [
        {
          libelle: 'Utilisateurs',
          path: 'param-global/utilisateurs',
          icon: 'fa-users',
        },
        {
          libelle: 'Rôles',
          path: 'param-global/roles',
          icon: 'fa-user-lock',
        },
        {
          libelle: 'Paramètres généraux',
          path: 'param-global/config-generale',
          icon: 'fa-cogs',
        },
      ]
    },
    {
      libelle: 'Tables des nomenclatures',
      path: 'nomenclatures',
      icon: 'fa-table',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]})
      }
    },
  ]

  constructor(private menuService: MenuService, private authService: AuthService, private configService: ConfigService) {
    // TODO get favicon from config
    if (this.favicon !== null) {
      this.favicon.href = 'favicon.ico';
    }
    this.configService.getConfigTheme();
    this.configService.themeModified.subscribe((date: any) => {
      if (this.theme !== null) {
        this.theme.href = environment.themeUrl + `?q=${(new Date(date)).getTime()}`;
      }
    });
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
