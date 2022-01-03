import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { TypeStructureService } from "../../../../services/type-structure.service";
import { TypeOffreService } from "../../../../services/type-offre.service";

@Component({
  selector: 'app-admin-nomenclatures-creation',
  templateUrl: './admin-nomenclatures-creation.component.html',
  styleUrls: ['./admin-nomenclatures-creation.component.scss']
})
export class AdminNomenclaturesCreationComponent implements OnInit {

  form: FormGroup;
  service: any;
  labelTable: string;
  typeStructures: any;
  typeOffres: any;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesCreationComponent>,
    private typeStructureService: TypeStructureService,
    private typeOffreService: TypeOffreService,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service;
    this.labelTable = data.labelTable;
    if (data.creationFormType == 1) {
      this.form = this.fb.group({
        libelle: [null, [this.emptyStringValidator, Validators.maxLength(150)]],
      });
    } else if (data.creationFormType == 2) {
      this.form = this.fb.group({
        libelle: [null, [this.emptyStringValidator, Validators.maxLength(150)]],
        codeCtrl: [null, [this.emptyStringValidator, Validators.maxLength(20)]],
      });
    } else {
      switch(this.labelTable) {
        case 'Langue Convention':
          this.setLangueConventionForm();
          break;
        case 'Type de structure':
          this.setTypeStructureForm();
          break;
        case 'Statut juridique':
          this.setStatutJuridiqueForm();
          break;
        case 'Contrat du stage':
          this.setContratOffreForm();
          break;
      }
    }
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(false);
  }

  setLangueConventionForm() {
    this.form = this.fb.group({
      code: [null, [this.emptyStringValidator, Validators.maxLength(2)]],
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(150)]],
    });
  }

  setTypeStructureForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(100)]],
      siretObligatoire: [false, [Validators.required]],
    });
  }

  setStatutJuridiqueForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(100)]],
      typeStructure: [null, [Validators.required]],
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', '').subscribe((response: any) => {
      this.typeStructures = response.data;
    });
  }

  setContratOffreForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(100)]],
      codeCtrl: [null, [this.emptyStringValidator, Validators.maxLength(20)]],
      typeOffre: [null, [Validators.required]],
    });
    this.typeOffreService.getPaginated(1, 0, 'libelle', 'asc', '').subscribe((response: any) => {
      this.typeOffres = response.data;
    });
  }

  save(): void {
    if (this.form.valid) {
      this.service.create(this.form.value).subscribe((response: any) => {
        this.dialogRef.close(true);
      });
    }
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

  emptyStringValidator(control: FormControl) {
    const isEmpty = (control.value || '').trim().length == 0;
    return isEmpty ? { 'empty': true } : null;
  }

}
