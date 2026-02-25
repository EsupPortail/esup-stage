import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigMissingRoutingModule } from './config-missing-routing.module';
import { ConfigMissingComponent } from './config-missing.component';
import { ConfigMissingPageComponent } from './pages/config-missing-page/config-missing-page.component';
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatIcon} from "@angular/material/icon";
import {MatProgressSpinner} from "@angular/material/progress-spinner";

@NgModule({
  declarations: [
    ConfigMissingComponent,
    ConfigMissingPageComponent
  ],
  exports: [
    ConfigMissingPageComponent
  ],
  imports: [
    CommonModule,
    ConfigMissingRoutingModule,
    MatCardModule,
    FormsModule,
    ReactiveFormsModule,
    MatIcon,
    MatProgressSpinner,

  ]
})
export class ConfigMissingModule {}
