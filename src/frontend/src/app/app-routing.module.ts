import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HelloWorldComponent } from "./components/hello-world/hello-world.component";
import { AuthGuard } from "./guard/auth.guard";
import { HomeComponent } from "./components/home/home.component";

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [AuthGuard], data: { roles: [], title: 'Accueil' }},
  { path: 'hello-world', component: HelloWorldComponent, canActivate: [AuthGuard], data: { roles: [], title: 'Hello world !' }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
