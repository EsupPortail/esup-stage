import { Component, OnInit, Input } from '@angular/core';
import { ServiceService } from "../../../services/service.service";
import { MessageService } from "../../../services/message.service";
import { ModeVersGratificationService } from "../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { DeviseService } from "../../../services/devise.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { AvenantService } from "../../../services/avenant.service";

@Component({
  selector: 'app-avenant',
  templateUrl: './avenant.component.html',
  styleUrls: ['./avenant.component.scss']
})
export class AvenantComponent implements OnInit {

  fieldValidators : any = {
      'dateRupture': [Validators.required],
  }

  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  devises: any[] = [];

  services: any[] = [];

  serviceTableColumns = ['nom', 'voie', 'codePostal','batimentResidence', 'commune'];

  @Input() convention: any;

  form: FormGroup;

  autreModifChecked: boolean = false;

  constructor(private avenantService: AvenantService,
              private serviceService: ServiceService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private deviseService: DeviseService,
              private fb: FormBuilder,
              private messageService: MessageService,
  ) {
    this.form = this.fb.group({
      titreAvenant: [null],
      rupture: [null],
      dateRupture: [null],
      commentaireRupture: [null],
      modificationPeriode: [null],
      dateDebutStage: [null],
      dateFinStage: [null],
      interruptionStage: [null],
      dateDebutInterruption: [null],
      dateFinInterruption: [null],
      modificationLieu: [null],
      modificationSujet: [null],
      sujetStage: [null],
      modificationTuteurPro: [null],
      modificationEnseignant: [null],
      modificationMontantGratification: [null],
      montantGratification: [null, [Validators.maxLength(7)]],
      idUniteGratification: [null],
      idUniteDuree: [null],
      idModeVersGratification: [null],
      idDevise: [null],
      validationAvenant: [null],
    });
  }

  ngOnInit(): void {
    this.modeVersGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.modeVersGratifications = response.data;
    });
    this.uniteDureeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteDurees = response.data;
    });
    this.uniteGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteGratifications = response.data;
    });
    this.deviseService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.devises = response.data;
    });
  }

  create(): void {
    if (this.customFormValidation()) {

      const data = {...this.form.value};

      data.idConvention = this.convention.id

      this.avenantService.create(data).subscribe((response: any) => {
        this.messageService.setSuccess('Avenant créé');
      });
    }
  }

  customFormValidation(): boolean {
    let valid = false;
    const checkboxFields = ['dateRupture', 'modificationPeriode', 'modificationLieu', 'modificationSujet', 'modificationTuteurPro',
       'modificationEnseignant', 'modificationMontantGratification'];

    checkboxFields.forEach((field: string) => {
      if (this.form.get(field)!.value){
        valid = true;
      }
    });


    return valid && this.form.valid
  }

  cancel(): void {
  }

  loadServices(): void{
    if (this.services.length == 0){
      this.serviceService.getByStructure(this.convention.structure.id).subscribe((response: any) => {
        this.services = response;
      });
    }
  }

}
