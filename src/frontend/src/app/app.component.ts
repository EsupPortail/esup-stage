import { Component, ElementRef, HostListener } from '@angular/core';
import { MenuService } from "./services/menu.service";
import { AuthService } from "./services/auth.service";
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
      filterKey: 'dashboard',
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
          icon: 'fa-list',
          filterKey: 'centregestion',
        },
        {
          libelle: 'Ajouter un centre de gestion',
          path: 'centre-gestion/create',
          icon: 'fa-plus',
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
      alerte: false,
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.CONVENTION, droits: [Droit.CREATION]})
      }
    },
    {
      libelle: 'Création des conventions en masse',
      path: 'convention-create-en-masse',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.LECTURE]})
      },
      children: [
        {
          libelle: 'Créer des conventions en masse',
          path: 'convention-create-en-masse/create',
          icon: 'fa-file-contract',
          canView: () => {
            return this.authService.checkRights({fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.LECTURE]})
          },
        },
        {
          libelle: 'Gestion des groupes',
          path: 'convention-create-en-masse/groupes',
          icon: 'fa-file-contract',
          canView: () => {
            return this.authService.checkRights({fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.LECTURE]})
          },
        }
      ]
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
        {
          libelle: 'Template de mails',
          path: 'param-global/mails',
          icon: 'fa-envelope',
        },
        {
          libelle: 'Templates de convention',
          path: 'param-global/conventions',
          icon: 'fa-file',
        },
      ]
    },
    {
      libelle: 'Établissements d\'accueil',
      path: 'etab-accueils',
      icon: 'fa-building',
      canView: () => {
        return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]})
      }
    },
    {
      libelle: 'Évaluation des stages',
      path: 'eval-stages',
      children: [
        {
          libelle: 'Rechercher une évaluation',
          path: 'eval-stages/gestionnaires',
          icon: 'fa-columns',
          canView: () => {
            return !this.authService.isEtudiant() && !this.authService.isEnseignant();
          },
        },
        {
          libelle: 'Visualiser mes évaluations',
          path: 'eval-stages/etudiants',
          icon: 'fa-columns',
          canView: () => {
            return this.authService.isEtudiant()
          },
        },
        {
          libelle: 'Visualiser mes évaluations',
          path: 'eval-stages/enseignants',
          icon: 'fa-columns',
          canView: () => {
            return this.authService.isEnseignant()
          },
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

  constructor(
    public menuService: MenuService,
    private authService: AuthService,
    private configService: ConfigService,
    private el: ElementRef,
  ) {
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

  slideNavbar(): void {
    let opened = this.menuService.navbarOpened;
    this.menuService.navbarOpened = !opened;
  }

  @HostListener('submit') onClick(): void {
    const firstInvalidControl: HTMLElement = this.el.nativeElement.querySelector("form .ng-invalid");

    if (firstInvalidControl) {
      firstInvalidControl.scrollIntoView({
        behavior: 'smooth',
      });
    }
  }

}
