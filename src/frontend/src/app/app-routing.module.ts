import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";
import { PersonnalisationComponent } from "./components/personnalisation/personnalisation.component";
import { Role } from "./constants/role";
import { AdminUserComponent } from "./components/admin/admin-user/admin-user.component";

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthGuard], data: { roles: [], title: 'Accueil' }},
  { path: 'utilisateurs', component: AdminUserComponent, canActivate: [AuthGuard], data: { roles: [Role.ADM.code, Role.ADM_TECH.code], title: 'Gestion des utilisateurs' }},
  { path: 'personnalisation', component: PersonnalisationComponent, canActivate: [AuthGuard], data: { roles: [Role.ADM.code, Role.ADM_TECH.code], title: 'Personnalisation de l\'application' }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
