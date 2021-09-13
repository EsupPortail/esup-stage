import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";
import { Role } from "./constants/role";
import { AdminUserComponent } from "./components/admin/admin-user/admin-user.component";
import { ConventionSearchComponent } from "./components/convention/convention-search/convention-search.component";
import { ConventionCreateComponent } from "./components/convention/convention-create/convention-create.component";

const routes: Routes = [
  {path: '', component: HomeComponent, canActivate: [AuthGuard], data: {roles: [], title: 'Accueil'}},
  {
    path: 'utilisateurs',
    component: AdminUserComponent,
    canActivate: [AuthGuard],
    data: {roles: [Role.ADM.code, Role.ADM_TECH.code], title: 'Gestion des utilisateurs'}
  },
  {
    path: 'conventions',
    component: ConventionSearchComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [Role.ADM.code, Role.RESP_GES.code, Role.GES.code, Role.ENS.code, Role.ETU.code, Role.OBS.code],
      title: 'Recherche de conventions'
    }
  },
  {
    path: 'conventions/create',
    component: ConventionCreateComponent,
    canActivate: [AuthGuard],
    data: {
      roles: [Role.ADM.code, Role.RESP_GES.code, Role.GES.code, Role.ENS.code, Role.ETU.code, Role.OBS.code],
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
