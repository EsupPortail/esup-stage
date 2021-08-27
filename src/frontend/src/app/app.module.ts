import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { AppRoutingModule } from "./app-routing.module";
import { HeaderComponent } from './components/header/header.component';
import { MatSidenavModule } from "@angular/material/sidenav";
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MenuComponent } from './components/menu/menu.component';
import { MatListModule } from "@angular/material/list";
import { HTTP_INTERCEPTORS, HttpClientModule } from "@angular/common/http";
import { CookieService } from "ngx-cookie-service";
import { TechnicalInterceptor } from "./interceptors/technical.interceptor";
import { HomeComponent } from './components/home/home.component';
import { TitleComponent } from './components/title/title.component';
import { NgProgressModule } from "ngx-progressbar";
import { NgProgressRouterModule } from "ngx-progressbar/router";
import { NgProgressHttpModule } from "ngx-progressbar/http";
import { MessageComponent } from './components/message/message.component';
import { MatDialogModule } from "@angular/material/dialog";
import { PersonnalisationComponent } from './components/personnalisation/personnalisation.component';
import { MatTabsModule } from "@angular/material/tabs";
import {
  MAT_COLOR_FORMATS,
  NGX_MAT_COLOR_FORMATS,
  NgxMatColorPickerModule
} from "@angular-material-components/color-picker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    MenuComponent,
    HomeComponent,
    TitleComponent,
    MessageComponent,
    PersonnalisationComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    NgProgressModule.withConfig({
      thick: true,
      spinner: false,
      color: "#6075e1"
    }),
    NgProgressRouterModule,
    NgProgressHttpModule,
    ReactiveFormsModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatDialogModule,
    MatTabsModule,
    MatFormFieldModule,
    MatInputModule,
    NgxMatColorPickerModule,
  ],
  providers: [
    CookieService,
    { provide: HTTP_INTERCEPTORS, useClass: TechnicalInterceptor, multi: true },
    { provide: MAT_COLOR_FORMATS, useValue: NGX_MAT_COLOR_FORMATS },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
