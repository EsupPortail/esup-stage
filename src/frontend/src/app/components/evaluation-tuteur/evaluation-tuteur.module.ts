import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { EvaluationTuteurRoutingModule } from './evaluation-tuteur-routing.module';
import { EvaluationTuteurComponent } from './evaluation-tuteur.component';
import { AccueilTuteurComponent } from './pages/accueil-tuteur/accueil-tuteur.component';
import { QuestionnaireTuteurComponent } from './pages/questionnaire-tuteur/questionnaire-tuteur.component';
import { QuestionnaireCompletTuteurComponent } from './pages/questionnaire-complet-tuteur/questionnaire-complet-tuteur.component';
import { ConfirmEvalTuteurComponent } from './shared/confirm-eval-tuteur/confirm-eval-tuteur.component';

import { ContenuService } from "../../services/contenu.service";
import {MatButton} from "@angular/material/button";
import {MatTab, MatTabBody, MatTabGroup, MatTabHeader} from "@angular/material/tabs";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import { MatRadioModule} from "@angular/material/radio";
import { MatDivider} from "@angular/material/divider";
import {MatFormField} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatDialogActions, MatDialogContent, MatDialogTitle} from "@angular/material/dialog";


@NgModule({
  declarations: [
    EvaluationTuteurComponent,
    AccueilTuteurComponent,
    QuestionnaireTuteurComponent,
    QuestionnaireCompletTuteurComponent,
    ConfirmEvalTuteurComponent
  ],
  imports: [
    CommonModule,
    EvaluationTuteurRoutingModule,
    MatButton,
    MatTabGroup,
    MatTab,
    MatTabHeader,
    MatTabBody,
    FormsModule,
    MatRadioModule,
    MatDivider,
    MatFormField,
    ReactiveFormsModule,
    MatInput,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
  ],
  providers: [ContenuService]
})
export class EvaluationTuteurModule { }
