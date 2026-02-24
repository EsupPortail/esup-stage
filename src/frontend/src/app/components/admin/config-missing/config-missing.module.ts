import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigMissingRoutingModule } from './config-missing-routing.module';
import { ConfigMissingComponent } from './config-missing.component';
import { ConfigMissingPageComponent } from './pages/config-missing-page/config-missing-page.component';
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

@NgModule({
  declarations: [
    ConfigMissingComponent,
    ConfigMissingPageComponent
  ],
  imports: [
    CommonModule,
    ConfigMissingRoutingModule,
    MatCardModule,
    FormsModule,
    ReactiveFormsModule,

  ]
})
export class ConfigMissingModule {}
