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

const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [AuthGuard], data: {role: {}, title: 'Accueil'}},
  {
    path: 'param-global/utilisateurs',
    component: AdminUserComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Gestion des utilisateurs'}
  },
  {
    path: 'param-global/roles',
    component: AdminRoleComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Gestion des rôles'}
  },
  {
    path: 'param-global/config-generale',
    component: ConfigGeneraleComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Paramètres généraux'}
  },
  {
    path: 'param-global/contenu',
    component: ContenuComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Contenu'}
  },
  {
    path: 'param-global/mails',
    component: TemplateMailComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.LECTURE]}, title: 'Template de mails'}
  },
  {
    path: 'param-global/conventions',
    component: TemplateConventionComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.PARAM_GLOBAL}, droits: [Droit.LECTURE]}
  },
  {
    path: 'etab-accueils',
    component: GestionEtabAccueilComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]}, title: 'Gestion des établissements d\'accueil'}
  },
  {
    path: 'nomenclatures',
    component: AdminNomenclaturesComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]}, title: 'Gestion des tables des nomenclatures'}
  },
  {
    path: 'tableau-de-bord',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.LECTURE]},
      title: 'Tableau de bord'
    }
  },
  {
    path: 'conventions/:id',
    component: ConventionComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION]},
      title: 'Création d\'une convention'
    }
  },
  {
    path: 'centre-gestion/search',
    component: CentreGestionSearchComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.PARAM_CENTRE, droits: [Droit.LECTURE]},
      title: 'Liste des centres de gestion'
    }
  },
  {
    path: 'centre-gestion/:id',
    component: CentreGestionComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.PARAM_CENTRE, droits: [Droit.CREATION, Droit.VALIDATION, Droit.LECTURE, Droit.MODIFICATION]},
      title: 'Modification d\'un centre de gestion'
    }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
