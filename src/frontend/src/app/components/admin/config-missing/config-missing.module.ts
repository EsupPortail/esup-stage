import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConfigMissingRoutingModule } from './config-missing-routing.module';
import { ConfigMissingComponent } from './config-missing.component';
import { ConfigMissingPageComponent } from './pages/config-missing-page/config-missing-page.component';
import { ConfigMissingMailerTestDialogComponent } from './pages/config-missing-page/mailer-test-dialog/config-missing-mailer-test-dialog.component';
import {MatCardModule} from "@angular/material/card";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatIcon} from "@angular/material/icon";
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import {AppModule} from "../../../app.module";

@NgModule({
  declarations: [
    ConfigMissingComponent,
    ConfigMissingPageComponent,
    ConfigMissingMailerTestDialogComponent
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
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ]
})
export class ConfigMissingModule {}
