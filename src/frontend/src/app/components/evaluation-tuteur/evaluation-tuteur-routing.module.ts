import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {AccueilTuteurComponent} from "./pages/accueil-tuteur/accueil-tuteur.component";
import {QuestionnaireTuteurComponent} from "./pages/questionnaire-tuteur/questionnaire-tuteur.component";
import {QuestionnaireCompletTuteurComponent} from "./pages/questionnaire-complet-tuteur/questionnaire-complet-tuteur.component";
import {EvaluationTuteurComponent} from "./evaluation-tuteur.component";

const routes: Routes = [
  {
    path: '',
    component: EvaluationTuteurComponent,
    children: [
      {
        path: ':idConvention',
        component: AccueilTuteurComponent,
        data:{
          title: 'Évaluation du stage',
        }
      },
      {
        path: ':idConvention/questionnaire',
        component: QuestionnaireTuteurComponent,
        data:{
          title: 'Évaluation du stage'
        }
      },
      {
        path: ':idConvention/terminee',
        component: QuestionnaireCompletTuteurComponent,
        data:{
          title: 'Évaluation du stage'
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EvaluationTuteurRoutingModule { }
