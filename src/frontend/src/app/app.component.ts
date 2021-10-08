import { Component } from '@angular/core';
import { MenuService } from "./services/menu.service";
import { AuthService } from "./services/auth.service";
import { Role } from "./constants/role";
import { AppFonction } from "./constants/app-fonction";
import { Droit } from "./constants/droit";
import { environment } from "../environments/environment";
import { ConfigService } from "./services/config.service";
import { ContenuService } from "./services/contenu.service";

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
      libelle: 'Centre de Gestion',
      path: 'centre-gestion',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.PARAM_CENTRE, droits: [Droit.LECTURE]})
      },
      children: [
        {
          libelle: 'Liste des centres de gestion',
          path: 'centre-gestion/search',
          icon: 'fa-cogs',
        },
        {
          libelle: 'Ajouter un centre de gestion',
          path: 'centre-gestion/create',
          icon: 'fa-file-contract',
          canView: () => {
            return this.authService.checkRights({fonction: AppFonction.PARAM_CENTRE, droits: [Droit.CREATION]})
          }
        },
      ]
    },
    {
      libelle: 'Créer une convention',
      path: 'conventions/create',
      icon: 'fa-file-contract',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.CONVENTION, droits: [Droit.CREATION]})
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
        {
          libelle: 'Contenu',
          path: 'param-global/contenu',
          icon: 'fa-font',
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
    this.configService.getConfigTheme();
    this.configService.themeModified.subscribe((config: any) => {
      if (this.favicon !== null && config.favicon) {
        this.favicon.href = `data:${config.favicon.contentType};base64,${config.favicon.base64}`;
      }
      if (this.theme !== null) {
        this.theme.href = environment.themeUrl + `?q=${(new Date(config.dateModification)).getTime()}`;
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
