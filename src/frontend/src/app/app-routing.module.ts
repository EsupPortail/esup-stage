import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";
import { PersonnalisationComponent } from "./components/personnalisation/personnalisation.component";

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthGuard], data: { roles: [], title: 'Accueil' }},
  { path: 'personnalisation', component: PersonnalisationComponent, canActivate: [AuthGuard], data: { roles: [], title: 'Personnalisation de l\'application' }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
