import { Component, OnInit, Input } from '@angular/core';
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AvenantService } from "../../../services/avenant.service";

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  uniteGratifications: any[] = [];
  uniteDurees: any[] = [];

  @Input() convention: any;

  form: FormGroup;

  autreModifChecked: boolean = false;

  constructor(private avenantService: AvenantService,
              private uniteGratificationService: UniteGratificationService,
              private uniteDureeService: UniteDureeService,
              private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      titreAvenant: [null, [Validators.required,]],
      motifAvenant: [null, [Validators.required,]],
      rupture: [null, []],
      dateRupture: [null, [Validators.required,]],
      modificationPeriode: [null, []],
      dateDebutStage: [null, [Validators.required,]],
      dateFinStage: [null, [Validators.required,]],
      interruptionStage: [null, []],
      dateDebutInterruption: [null, [Validators.required,]],
      dateFinInterruption: [null, [Validators.required,]],
      modificationLieu: [null, [Validators.required,]],
      modificationSujet: [null, []],
      sujetStage: [null, [Validators.required,]],
      modificationTuteurPro: [null, []],
      modificationEnseignant: [null, []],
      modificationMontantGratification: [null, []],
      montantGratification: [null, [Validators.required,Validators.maxLength(7),]],
      idUniteGratification: [null, [Validators.required,]],
      idUniteDuree: [null, [Validators.required,]],
      commentaireRupture: [null, [Validators.required,]],
      monnaieGratification: [null, [Validators.required,Validators.maxLength(50),]],
      validationAvenant: [null, []],
    });
  }

  ngOnInit(): void {
    this.uniteGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteGratifications = response.data;
    });
    this.uniteDureeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteDurees = response.data;
    });
  }

  save(): void {
  }
  cancel(): void {
  }
}
