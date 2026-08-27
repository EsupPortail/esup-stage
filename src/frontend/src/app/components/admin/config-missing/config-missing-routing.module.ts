import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigMissingComponent } from "./config-missing.component";
import { ConfigMissingPageComponent } from "./pages/config-missing-page/config-missing-page.component";

const routes: Routes = [
  {
    path: '',
    component: ConfigMissingComponent,
    children: [
      {
        path: '',
        component: ConfigMissingPageComponent,
        data: {
          title: 'Configuration requise'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigMissingRoutingModule {}
