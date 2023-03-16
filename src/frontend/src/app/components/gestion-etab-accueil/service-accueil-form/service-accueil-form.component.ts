import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { UntypedFormBuilder, Validators } from "@angular/forms";
import { ServiceService } from "../../../services/service.service";
import { CommuneService } from "../../../services/commune.service";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-service-accueil-form',
  templateUrl: './service-accueil-form.component.html',
  styleUrls: ['./service-accueil-form.component.scss']
})
export class ServiceAccueilFormComponent {

  service: any;
  etab: any;

  countries: any[] = [];
  communes: any[] = [];

  form: any;

  constructor(public serviceService: ServiceService,
              public communeService: CommuneService,
              private messageService: MessageService,
              private dialogRef: MatDialogRef<ServiceAccueilFormComponent>,
              private fb: UntypedFormBuilder,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service
    this.etab = data.etab
    this.countries = data.countries
    if (this.service) {
      this.form = this.fb.group({
        nom: [this.service.nom, [Validators.required, Validators.maxLength(70)]],
        voie: [this.service.voie, [Validators.required, Validators.maxLength(200)]],
        codePostal: [this.service.codePostal, [Validators.required, Validators.maxLength(10)]],
        batimentResidence: [this.service.batimentResidence, [Validators.maxLength(200)]],
        commune: [this.service.commune, [Validators.required, Validators.maxLength(200)]],
        idPays: [this.service.pays ? this.service.pays.id : null, [Validators.required]],
        telephone: [this.service.telephone, [Validators.maxLength(20)]],
      });
    }else{
      this.form = this.fb.group({
        nom: [null, [Validators.required, Validators.maxLength(70)]],
        voie: [this.etab.voie, [Validators.required, Validators.maxLength(200)]],
        codePostal: [this.etab.codePostal, [Validators.required, Validators.maxLength(10), Validators.pattern('[0-9]+')]],
        batimentResidence: [this.etab.batimentResidence, [Validators.maxLength(200)]],
        commune: [this.etab.commune, [Validators.required, Validators.maxLength(200)]],
        idPays: [this.etab.pays ? this.etab.pays.id : null, [Validators.required]],
        telephone: [this.etab.telephone, [Validators.maxLength(20)]],
      });
    }
    this.communeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.communes = response;
      this.initForm();
    });
  }

  initForm(): void {
    this.toggleCommune();
    this.form.get('idPays')?.valueChanges.subscribe((idPays: any) => {
      this.toggleCommune();
      this.clearCommune();
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {

      // Contrôle code postal commune
      if (this.isFr() && !this.isCodePostalValid()) {
        this.messageService.setError('Code postal inconnu');
        return;
      }

      const data = {...this.form.getRawValue()};

      if (this.service) {
        this.serviceService.update(this.service.id, data).subscribe((response: any) => {
          this.service = response;
          this.dialogRef.close(this.service);
        });
      } else {
        //ajoute idStructure à l'objet service
        data.idStructure = this.etab.id;
        this.serviceService.create(data).subscribe((response: any) => {
          this.service = response;
          this.dialogRef.close(this.service);
        });
      }
    }
  }

  clearCommune(): void {
      this.form.get('commune')?.setValue('');
      this.form.get('codePostal')?.setValue('');
      this.form.get('commune')?.markAsPristine();
      this.form.get('codePostal')?.markAsPristine();
  }
  toggleCommune(): void {
      if (!this.isPaysSet()) {
        this.form.get('commune')?.disable();
        this.form.get('codePostal')?.disable();
      }else{
        if (this.isFr()) {
          this.form.get('commune')?.disable();
          this.form.get('codePostal')?.enable();
        }else{
          this.form.get('commune')?.enable();
          this.form.get('codePostal')?.enable();
        }
      }
  }

  updateCommune(commune : any): void {
    this.form.get('commune')?.setValue(commune.split(' - ')[0]);
    this.form.get('codePostal')?.setValue(commune.split(' - ')[1]);
  }

  isPaysSet() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays)
      return true;
    return false;
  }

  isFr() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays) {
      let pays = this.countries.find(c => c.id === idPays);
      if (pays)
        return pays.libelle === 'FRANCE';
    }
    return true;
  }

  isCodePostalValid() {
    let codePostal = this.form.get('codePostal')?.value;
    if (codePostal) {
      let commune = this.communes.find(c => c.codePostal === codePostal);
      if (commune)
        return true;
    }
    return false;
  }
}
