import { Component, EventEmitter, OnInit , Input, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { AuthService } from "../../../services/auth.service";
import { ConventionService } from "../../../services/convention.service";
import { debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-stage',
  templateUrl: './stage.component.html',
  styleUrls: ['./stage.component.scss']
})
export class StageComponent implements OnInit {

  countries: any[] = [];

  //TODO codeLangueConvention
  langues: string[] = ['Anglais','Français'];

  //TODO idTheme
  thematiques: string[] = ['TODO','TODO'];
  //TODO idTempsTravail
  tempsTravail: string[] = ['TODO','TODO'];
  //TODO idUniteGratification
  unitesGratification: string[] = ['Brut','Net'];
  //TODO idUniteDureeGratification
  unitesDureeGratification: string[] = ['Horaire','Mensuel'];
  //TODO idMonnaieGratification
  monnaiesGratification: string[] = ['Euros','Dollars'];
  //TODO idModeVersGratification
  modesVersGratification: string[] = ['Chèque','Virement'];

  @Input() convention: any;

  form: FormGroup;

  @Output() validated = new EventEmitter<number>();

  constructor(public conventionService: ConventionService,
              private fb: FormBuilder,
              private authService: AuthService,
              private paysService: PaysService,
  ) {
    this.form = this.fb.group({
      // - Modèle de la convention
      codeLangueConvention: [null, [Validators.required]],
      idPays: [null, [Validators.required]],
      // - Description du stage
      idTheme: [null, [Validators.required]],
      //TODO case à cocher pour rendre les champs confidentiels
      sujetStage: [null, [Validators.required]],
      competences: [null, [Validators.required]],
      fonctionsEtTaches: [null, [Validators.required]],
      details: [null, [Validators.required]],
      // - Partie Dates / horaires
      //TODO contrôle de la cohérence des dates saisies
      dateDebutStage: [null, [Validators.required]],
      dateFinStage: [null, [Validators.required]],
      interruptionStage: [false, [Validators.required]],
      dateDebutInterruption: [null],
      dateFinInterruption: [null],
      idTempsTravail: [null, [Validators.required]],
      commentaireDureeTravail: [null],
      // - Partie Gratification
      //TODO champ de saisie du type de gratification (gratification à l’heure, gratification lissée).
      gratificationStage: [false, [Validators.required]],
      montantGratification: [null, [Validators.required]],
      idUniteGratification: ['Brut', [Validators.required]],
      idUniteDureeGratification: ['Mensuel', [Validators.required]],
      idMonnaieGratification: [null, [Validators.required]],
      idModeVersGratification: [null, [Validators.required]],
      //TODO un bandeau doit permettre de mettre un message à l’attention de l’étudiant
      // - Partie Divers

    });
    console.log('convention : ' + JSON.stringify(this.convention, null, 2))
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      console.log('val : ' + JSON.stringify(val, null, 2))
      this.updateBrouillon();
    });

  }

  ngOnInit(): void {
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
  }

  updateBrouillon(): void {
  //TODO
  }

}
