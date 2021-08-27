import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './app.component';
import { HelloWorldComponent } from './components/hello-world/hello-world.component';
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

@NgModule({
  declarations: [
    AppComponent,
    HelloWorldComponent,
    HeaderComponent,
    MenuComponent,
    HomeComponent,
    TitleComponent,
    MessageComponent
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
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatDialogModule,
  ],
  providers: [
    CookieService,
    {provide: HTTP_INTERCEPTORS, useClass: TechnicalInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
