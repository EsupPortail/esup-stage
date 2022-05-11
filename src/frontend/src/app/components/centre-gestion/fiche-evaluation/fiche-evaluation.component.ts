import { Component, OnInit, Input } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { FicheEvaluationService } from "../../../services/fiche-evaluation.service";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';

@Component({
  selector: 'app-fiche-evaluation',
  templateUrl: './fiche-evaluation.component.html',
  styleUrls: ['./fiche-evaluation.component.scss']
})
export class FicheEvaluationComponent implements OnInit {

  ficheEvaluation: any;
  ficheEtudiantForm: FormGroup;
  ficheEnseignantForm: FormGroup;
  ficheEntrepriseForm: FormGroup;

  FicheEtudiantIQuestions: any = [
    {
      title: 'Avez-vous rencontré des difficultés pour trouver un stage ?',
      texte: ['Non, il est automatiquement proposé dans le cadre de la formation',
             'Non, je l’ai trouvé assez facilement par moi-même',
             'Oui j’ai eu des difficultés',
             ],
      controlName: 'questionEtuI1',
    },
  ];

  @Input() idCentreGestion: any;

  constructor(private fb: FormBuilder,
              private ficheEvaluationService: FicheEvaluationService,
              public matDialog: MatDialog,
  ) {
    this.ficheEtudiantForm = this.fb.group({
      questionEtuI1: [null],
    });
    this.ficheEnseignantForm = this.fb.group({
      questionEtuI1: [null],
    });
    this.ficheEntrepriseForm = this.fb.group({
      questionEtuI1: [null],
    });
  }

  ngOnInit(): void {

    this.ficheEvaluationService.getByCentreGestion(this.idCentreGestion).subscribe((response: any) => {
      this.ficheEvaluation = response;
      console.log('this.ficheEvaluation : ' + JSON.stringify(this.ficheEvaluation, null, 2));
      this.ficheEtudiantForm.setValue({
        questionEtuI1: this.ficheEvaluation.questionEtuI1,
      });
    });

  }

}
