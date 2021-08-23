import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HelloWorldComponent } from "./components/hello-world/hello-world.component";

const routes: Routes = [
  { path: 'hello-world', component: HelloWorldComponent, data: { roles: [] }},
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
