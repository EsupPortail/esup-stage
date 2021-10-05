import { APP_INITIALIZER, LOCALE_ID, NgModule } from '@angular/core';
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
import { NgProgressHttpModule } from "ngx-progressbar/http";
import { MessageComponent } from './components/message/message.component';
import { MatDialogModule } from "@angular/material/dialog";
import { MatTabsModule } from "@angular/material/tabs";
import {
  MAT_COLOR_FORMATS,
  NGX_MAT_COLOR_FORMATS,
  NgxMatColorPickerModule
} from "@angular-material-components/color-picker";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { AdminUserComponent } from './components/admin/admin-user/admin-user.component';
import { MatTableModule } from "@angular/material/table";
import { MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatPaginatorIntl, MatPaginatorModule } from "@angular/material/paginator";

import { registerLocaleData } from "@angular/common";
import localeFr from '@angular/common/locales/fr';
import { MAT_DATE_LOCALE } from "@angular/material/core";
import { PaginatorIntl } from "./paginator-intl";
import { TableComponent } from './components/table/table.component';
import { BooleanPipe } from './pipes/boolean.pipe';
import { MatCardModule } from "@angular/material/card";
import { RoleLibellePipe } from './pipes/role-libelle.pipe';
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { ConventionSearchComponent } from './components/convention/convention-search/convention-search.component';
import { ConventionCreateComponent } from './components/convention/convention-create/convention-create.component';
import { MatStepperModule } from "@angular/material/stepper";
import { AdminNomenclaturesComponent } from './components/admin/admin-nomenclatures/admin-nomenclatures.component';
import { AdminNomenclaturesEditionComponent } from './components/admin/admin-nomenclatures/admin-nomenclatures-edition/admin-nomenclatures-edition.component';
import { AdminRoleComponent } from './components/admin/admin-role/admin-role.component';
import { ConfirmComponent } from './components/confirm/confirm.component';
import { ConfigGeneraleComponent } from './components/admin/config-generale/config-generale.component';
import { MatRadioModule } from "@angular/material/radio";
import { ContenuComponent } from './components/admin/contenu/contenu.component';
import { ContenuPipe } from './pipes/contenu.pipe';
import { ContenuService } from "./services/contenu.service";
import { CKEditorModule } from "@ckeditor/ckeditor5-angular";
import { CentreGestionSearchComponent } from './components/centre-gestion/centre-gestion-search/centre-gestion-search.component';

registerLocaleData(localeFr, 'fr');

@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    MenuComponent,
    HomeComponent,
    TitleComponent,
    MessageComponent,
    AdminUserComponent,
    TableComponent,
    BooleanPipe,
    RoleLibellePipe,
    ConventionSearchComponent,
    ConventionCreateComponent,
    AdminNomenclaturesComponent,
    AdminNomenclaturesEditionComponent,
    AdminRoleComponent,
    ConfirmComponent,
    ConfigGeneraleComponent,
    ContenuComponent,
    ContenuPipe,
    CentreGestionSearchComponent
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
    NgProgressHttpModule,
    ReactiveFormsModule,
    FormsModule,
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
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatCardModule,
    MatSelectModule,
    MatCheckboxModule,
    MatStepperModule,
    MatRadioModule,
    CKEditorModule,
  ],
  providers: [
    CookieService,
    ContenuService,
    { provide: LOCALE_ID, useValue: 'fr' },
    { provide: HTTP_INTERCEPTORS, useClass: TechnicalInterceptor, multi: true },
    { provide: MAT_COLOR_FORMATS, useValue: NGX_MAT_COLOR_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'fr-FR' },
    { provide: MatPaginatorIntl, useClass: PaginatorIntl },
    { provide: APP_INITIALIZER, useFactory: (cs: ContenuService) => () => cs.getAllLibelle(), deps: [ContenuService], multi: true },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
