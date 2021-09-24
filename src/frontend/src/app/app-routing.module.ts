import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";
import { Role } from "./constants/role";
import { AdminUserComponent } from "./components/admin/admin-user/admin-user.component";
import { ConventionSearchComponent } from "./components/convention/convention-search/convention-search.component";
import { ConventionCreateComponent } from "./components/convention/convention-create/convention-create.component";
import { AdminNomenclaturesComponent } from "./components/admin/admin-nomenclatures/admin-nomenclatures.component";
import { AppFonction } from "./constants/app-fonction";
import { Droit } from "./constants/droit";
import { AdminRoleComponent } from "./components/admin/admin-role/admin-role.component";

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
    path: 'nomenclatures',
    component: AdminNomenclaturesComponent,
    canActivate: [AuthGuard],
    data: {role: {fonction: AppFonction.NOMENCLATURE, droits: [Droit.LECTURE]}, title: 'Gestion des tables des nomenclatures'}
  },
  {
    path: 'tableau-de-bord',
    component: ConventionSearchComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.LECTURE]},
      title: 'Tableau de bord'
    }
  },
  {
    path: 'conventions/create',
    component: ConventionCreateComponent,
    canActivate: [AuthGuard],
    data: {
      role: {fonction: AppFonction.CONVENTION, droits: [Droit.CREATION]},
      title: 'Création d\'une conventions'
    }
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
