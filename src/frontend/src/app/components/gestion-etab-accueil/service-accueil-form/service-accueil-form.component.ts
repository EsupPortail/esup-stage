import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, Validators } from "@angular/forms";
import { ServiceService } from "../../../services/service.service";
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

  form: any;

  constructor(public serviceService: ServiceService,
              private dialogRef: MatDialogRef<ServiceAccueilFormComponent>,
              private fb: FormBuilder,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service
    this.etab = data.etab
    this.countries = data.countries
    if (this.service) {
      this.form = this.fb.group({
        nom: [this.service.nom, [Validators.required, Validators.maxLength(70)]],
        voie: [this.service.voie, [Validators.required, Validators.maxLength(200)]],
        codePostal: [this.service.codePostal, [Validators.required, Validators.maxLength(10), Validators.pattern('[0-9]+')]],
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
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {

      const data = {...this.form.value};

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


}
