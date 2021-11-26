import { Component, Output, EventEmitter, OnInit, Input } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ServiceService } from "../../../../services/service.service";
import { MessageService } from "../../../../services/message.service";
import { ModeVersGratificationService } from "../../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../../services/unite-gratification.service";
import { DeviseService } from "../../../../services/devise.service";
import { AvenantService } from "../../../../services/avenant.service";

@Component({
  selector: 'app-avenant-form',
  templateUrl: './avenant-form.component.html',
  styleUrls: ['./avenant-form.component.scss']
})
export class AvenantFormComponent implements OnInit {

  fieldValidators : any = {
      'dateRupture': [Validators.required],
  }

  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  devises: any[] = [];

  services: any[] = [];

  serviceTableColumns = ['nom', 'voie', 'codePostal','batimentResidence', 'commune'];

  @Input() avenant: any;
  @Input() convention: any;

  form: FormGroup;

  autreModifChecked: boolean = false;

  @Output() validated = new EventEmitter<any>();

  constructor(private avenantService: AvenantService,
              private serviceService: ServiceService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private deviseService: DeviseService,
              private fb: FormBuilder,
              private messageService: MessageService,
  ) {
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

    if (this.avenant.id){
      this.form = this.fb.group({
        titreAvenant: [this.avenant.titreAvenant],
        rupture: [this.avenant.rupture],
        dateRupture: [this.avenant.dateRupture],
        commentaireRupture: [this.avenant.commentaireRupture],
        modificationPeriode: [this.avenant.modificationPeriode],
        dateDebutStage: [this.avenant.dateDebutStage],
        dateFinStage: [this.avenant.dateFinStage],
        interruptionStage: [this.avenant.interruptionStage],
        dateDebutInterruption: [this.avenant.dateDebutInterruption],
        dateFinInterruption: [this.avenant.dateFinInterruption],
        modificationLieu: [this.avenant.modificationLieu],
        modificationSujet: [this.avenant.modificationSujet],
        sujetStage: [this.avenant.sujetStage],
        modificationTuteurPro: [this.avenant.modificationTuteurPro],
        modificationEnseignant: [this.avenant.modificationEnseignant],
        modificationMontantGratification: [this.avenant.modificationMontantGratification],
        montantGratification: [this.avenant.montantGratification],
        idUniteGratification: [this.avenant.UniteGratification?this.avenant.UniteGratification.id:null],
        idUniteDuree: [this.avenant.uniteDuree?this.avenant.uniteDuree.id:null],
        idModeVersGratification: [this.avenant.modeVersGratification?this.avenant.modeVersGratification.id:null],
        idDevise: [this.avenant.devise?this.avenant.devise.id:null],
        validationAvenant: [this.avenant.validationAvenant],
        modificationAutre: [this.avenant.motifAvenant?true:false],
        motifAvenant: [this.avenant.motifAvenant],
      });
    }else{
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
        modificationTuteurPro: [false],
        modificationEnseignant: [null],
        modificationMontantGratification: [null],
        montantGratification: [null, [Validators.maxLength(7)]],
        idUniteGratification: [null],
        idUniteDuree: [null],
        idModeVersGratification: [null],
        idDevise: [null],
        validationAvenant: [null],
        modificationAutre: [null],
        motifAvenant: [null],
      });
    }

  }

  createOrEdit(): void {
    if (this.customFormValidation()) {

      const data = {...this.form.value};

      data.idConvention = this.convention.id

      if (this.avenant.id){
        this.avenantService.update(this.avenant.id,data).subscribe((response: any) => {
          this.avenant = response;
          this.messageService.setSuccess('Avenant modifié avec succès');
          this.validated.emit();
        });
      }else{
        this.avenantService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Avenant créé avec succès');
          this.validated.emit();
        });
      }
    }
  }

  customFormValidation(): boolean {
    let valid = false;
    const checkboxFields = ['dateRupture', 'modificationPeriode', 'modificationLieu', 'modificationSujet', 'modificationTuteurPro',
       'modificationEnseignant', 'modificationMontantGratification', 'modificationAutre'];

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
