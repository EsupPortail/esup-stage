import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";
import { Role } from "./constants/role";
import { AdminUserComponent } from "./components/admin/admin-user/admin-user.component";
import { DashboardComponent } from "./components/dashboard/dashboard.component";
import { ConventionComponent } from "./components/convention/convention.component";
import { AdminNomenclaturesComponent } from "./components/admin/admin-nomenclatures/admin-nomenclatures.component";
import { AppFonction } from "./constants/app-fonction";
import { Droit } from "./constants/droit";
import { AdminRoleComponent } from "./components/admin/admin-role/admin-role.component";
import { ConfigGeneraleComponent } from "./components/admin/config-generale/config-generale.component";
import { ContenuComponent } from "./components/admin/contenu/contenu.component";
import { CentreGestionSearchComponent } from "./components/centre-gestion-search/centre-gestion-search.component";
import { CentreGestionComponent } from "./components/centre-gestion/centre-gestion.component";
import { TemplateMailComponent } from "./components/admin/template-mail/template-mail.component";
import { GestionEtabAccueilComponent } from "./components/gestion-etab-accueil/gestion-etab-accueil.component";
import { TemplateConventionComponent } from "./components/admin/template-convention/template-convention.component";
import { EvalStageComponent } from './components/eval-stage/eval-stage.component';
import { ConventionCreateEnMasseComponent } from './components/convention-create-en-masse/convention-create-en-masse.component';
import { GestionGroupeComponent } from './components/convention-create-en-masse/gestion-groupe/gestion-groupe.component';
import { TemplateMailGroupeComponent } from './components/convention-create-en-masse/template-mail-groupe/template-mail-groupe.component';
import {TachePlanifieComponent} from "./components/admin/taches-planifiees/tache-planifie.component";
import {SitemapComponent} from "./components/sitemap/sitemap.component";
import {AccessibilityComponent} from "./components/accessibility/accessibility.component";
import {LegalNoticeComponent} from "./components/legal-notice/legal-notice.component";
import {ConfigAppComponent} from "./components/admin/config-app/config-app.component";
import { ConfigMissingGuard } from "./guard/config-missing.guard";

const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [AuthGuard, ConfigMissingGuard], data: {role: {}, title: 'Accueil', sitemap: {label: 'Accueil', group: 'Général', order: 1}}},
  {
    path: 'param-global/utilisateurs',
    component: AdminUserComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Gestion des utilisateurs', sitemap: {label: 'Gestion des utilisateurs', group: 'Paramétrage global', order: 1}}
  },
  {
    path: 'param-global/roles',
    component: AdminRoleComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Gestion des rôles', sitemap: {label: 'Gestion des rôles', group: 'Paramétrage global', order: 2}}
  },
  {
    path: 'param-global/config-generale',
    component: ConfigGeneraleComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Paramètres généraux', sitemap: {label: 'Paramètres généraux', group: 'Paramétrage global', order: 3}}
  },
  {
    path: 'param-global/contenu',
    component: ContenuComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Contenu', sitemap: {label: 'Contenu', group: 'Paramétrage global', order: 4}}
  },
  {
    path: 'param-global/mails',
    component: TemplateMailComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Template de mails', sitemap: {label: 'Templates de mails', group: 'Paramétrage global', order: 5}}
  },
  {
    path: 'param-global/conventions',
    component: TemplateConventionComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL}, droits: [Droit.LECTURE], sitemap: {label: 'Templates de conventions', group: 'Paramétrage global', order: 6}}
  },
  {
    path: 'eval-stages',
    component: EvalStageComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.CONVENTION, droits: [Droit.LECTURE]}, sitemap: {label: 'Évaluation des stages', group: 'Conventions de stages', order: 3}}
  },
  {
    path: 'etab-accueils',
    component: GestionEtabAccueilComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]}, title: 'Gestion des établissements d\'accueil', sitemap: {label: 'Établissements d\'accueil', group:"Établissements d\'accueil" , order: 2}}
  },
  {
    path: 'nomenclatures',
    component: AdminNomenclaturesComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {role: {fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]}, title: 'Gestion des tables des nomenclatures', sitemap: {label: 'Tables des nomenclatures', group: 'Nomenclatures', order: 1}}
  },
  {
    path: 'tableau-de-bord',
    component: DashboardComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.LECTURE]},
      title: 'Tableau de bord',
      sitemap: {label: 'Tableau de bord', group: 'Conventions de stages', order: 1}
    }
  },
  {
    path: 'conventions/:id',
    component: ConventionComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION]},
      title: 'Création d\'une convention',
      sitemap: {label: 'Créer une convention', group: 'Conventions de stages', order: 2}
    }
  },
  {
    path: 'convention-create-en-masse/groupes',
    component: GestionGroupeComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.CREATION, Droit.LECTURE]},
      title: 'Gestion des groupes',
      sitemap: {label: 'Gestion des groupes', group: 'Conventions en masse', order: 2}
    }
  },
  {
    path: 'convention-create-en-masse/mails',
    component: TemplateMailGroupeComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.CREATION, Droit.LECTURE]},
      title: 'Template de mails',
      sitemap: {label: 'Templates de mails (en masse)', group: 'Conventions en masse', order: 3}
    }
  },
  {
    path: 'convention-create-en-masse/:id',
    component: ConventionCreateEnMasseComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.CREATION, Droit.LECTURE]},
      title: 'Création des conventions en masse',
      sitemap: {label: 'Créer des conventions en masse', group: 'Conventions en masse', order: 1}
    }
  },
  {
    path: 'centre-gestion/search',
    component: CentreGestionSearchComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.PARAM_CENTRE, droits: [Droit.LECTURE]},
      title: 'Liste des centres de gestion',
      sitemap: {label: 'Liste des centres de gestion', group: 'Centres de gestion', order: 1}
    }
  },
  {
    path: 'centre-gestion/:id',
    component: CentreGestionComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {fonction: AppFonction.PARAM_CENTRE, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION]},
      title: 'Modification d\'un centre de gestion',
      sitemap: {label: 'Ajouter un centre de gestion', group: 'Centres de gestion', order: 2}
    }
  },
  {
    path: 'param-global/taches-planifiees',
    component: TachePlanifieComponent,
    canActivate:[AuthGuard, ConfigMissingGuard],
    data:{
      role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION,Droit.SUPPRESSION],},
      title: 'Tâches planifiées',
      sitemap: {label: 'Tâches planifiées', group: 'Paramétrage global', order: 7}
    }
  },
  {
    path: 'evaluation-tuteur',
    loadChildren: () => import('./components/evaluation-tuteur/evaluation-tuteur.module')
      .then(m => m.EvaluationTuteurModule),
    data: {
      title: 'Évaluation du stage',
      layout: 'public',
    }
  },
  {
    path: 'admin/config-missing',
    loadChildren: () => import('./components/admin/config-missing/config-missing.module')
      .then(m => m.ConfigMissingModule),
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      title: 'Configuration requise',
      layout: 'public',
    }
  },
  {
    path: 'plan-du-site',
    component: SitemapComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {},
      title: 'Plan du site',
      sitemap: {exclude: true}
    }
  },
  {
    path: 'accessibilite',
    component: AccessibilityComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {},
      title: 'Accessibilité',
      sitemap: {label: 'Accessibilité', group: 'Général', order: 2}
    }
  },
  {
    path: 'mentions-legales',
    component: LegalNoticeComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data: {
      role: {},
      title: 'Mentions légales',
      sitemap: {label: 'Mentions légales', group: 'Général', order: 3}
    }
  },
  {
    path:'param-global/config-app',
    component: ConfigAppComponent,
    canActivate: [AuthGuard, ConfigMissingGuard],
    data:{
      role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION, Droit.SUPPRESSION]},
      title: 'Configuration de l\'application',
      sitemap: {label: 'Configuration de l\'application', group: 'Paramétrage global', order: 8}
    }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

