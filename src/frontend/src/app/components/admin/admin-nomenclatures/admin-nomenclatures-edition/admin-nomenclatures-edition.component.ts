import { Component, OnInit, Inject } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { UntypedFormBuilder, UntypedFormGroup, UntypedFormControl, Validators } from "@angular/forms";
import { TypeStructureService } from "../../../../services/type-structure.service";
import { TypeOffreService } from "../../../../services/type-offre.service";

@Component({
  selector: 'app-admin-nomenclatures-edition',
  templateUrl: './admin-nomenclatures-edition.component.html',
  styleUrls: ['./admin-nomenclatures-edition.component.scss']
})
export class AdminNomenclaturesEditionComponent implements OnInit {

  form!: UntypedFormGroup;
  service: any;
  data: any;
  labelTable: string;
  typeStructures: any;
  typeOffres: any;

  constructor(
    private fb: UntypedFormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesEditionComponent>,
    private typeStructureService: TypeStructureService,
    private typeOffreService: TypeOffreService,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service;
    this.data = data.data;
    this.labelTable = data.labelTable;
    switch(this.labelTable) {
      case 'Type de structure':
        this.setTypeStructureForm();
        break;
      case 'Statut juridique':
        this.setStatutJuridiqueForm();
        break;
      case 'Contrat du stage':
        this.setContratOffreForm();
        break;
      default:
        this.setDefaultForm();
        break;
    }
    this.setFormData(this.labelTable);
  }

  ngOnInit(): void {
  }

  setFormData(labelTable: string): void {
    const data = {...this.data};
    delete data.id;
    delete data.code;
    delete data.codeCtrl;
    delete data.temEnServ;
    delete data.modifiable;

    if (labelTable !== 'Type de structure')
      delete data.siretObligatoire;
    if (labelTable !== 'Statut juridique')
      delete data.typeStructure;
    if (labelTable !== 'Contrat du stage')
      delete data.typeOffre;
    this.form.setValue(data);
  }

  close(): void {
    this.dialogRef.close(false);
  }

  setDefaultForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
    });
  }

  setTypeStructureForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      siretObligatoire: [false, [Validators.required]],
    });
  }

  setStatutJuridiqueForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      typeStructure: [null, [Validators.required]],
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', '').subscribe((response: any) => {
      this.typeStructures = response.data;
    });
  }

  setContratOffreForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      typeOffre: [null, [Validators.required]],
    });
    this.typeOffreService.getPaginated(1, 0, 'libelle', 'asc', '').subscribe((response: any) => {
      this.typeOffres = response.data;
    });
  }

  save(): void {
    if (this.form.valid) {
      let key = this.data.id ? this.data.id : this.data.code;
      this.service.update(key, this.form.value).subscribe((response: any) => {
        this.data = response;
        this.setFormData(this.labelTable);
        this.dialogRef.close(true);
      });
    }
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  emptyStringValidator(control: UntypedFormControl) {
    const isEmpty = (control.value || '').trim().length == 0;
    return isEmpty ? { 'empty': true } : null;
  }

}
