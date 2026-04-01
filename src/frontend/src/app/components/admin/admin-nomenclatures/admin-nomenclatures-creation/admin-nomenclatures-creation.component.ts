import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { TypeStructureService } from "../../../../services/type-structure.service";
import { TypeOffreService } from "../../../../services/type-offre.service";
import { TemplateConventionService } from "../../../../services/template-convention.service";

@Component({
    selector: 'app-admin-nomenclatures-creation',
    templateUrl: './admin-nomenclatures-creation.component.html',
    styleUrls: ['./admin-nomenclatures-creation.component.scss'],
    standalone: false
})
export class AdminNomenclaturesCreationComponent implements OnInit {

  form!: FormGroup;
  service: any;
  labelTable: string;
  typeStructures: any;
  typeOffres: any;
  templateConventions: any[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesCreationComponent>,
    private typeStructureService: TypeStructureService,
    private typeOffreService: TypeOffreService,
    private templateConventionService: TemplateConventionService,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service;
    this.labelTable = data.labelTable;

    if (this.labelTable === 'Type Convention') {
      this.setTypeConventionForm();
      this.loadTemplateConventions();
      return;
    }

    if (data.creationFormType == 1) {
      this.form = this.fb.group({
        libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      });
    } else if (data.creationFormType == 2) {
      this.form = this.fb.group({
        libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
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
      codeCtrl: [null, [this.emptyStringValidator, Validators.maxLength(20)]],
      typeOffre: [null, [Validators.required]],
    });
    this.typeOffreService.getPaginated(1, 0, 'libelle', 'asc', '').subscribe((response: any) => {
      this.typeOffres = response.data;
    });
  }

  setTypeConventionForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      codeCtrl: [null, [this.emptyStringValidator, Validators.maxLength(20)]],
      templateConventions: [[], []],
    });
  }

  private loadTemplateConventions(): void {
    this.templateConventionService.getAll().subscribe((response: any) => {
      this.templateConventions = (response || []).slice().sort((a: any, b: any) => this.getTemplateLabel(a).localeCompare(this.getTemplateLabel(b)));
    });
  }

  save(): void {
    if (!this.form.valid) {
      return;
    }

    const payload = this.buildPayload();
    this.service.create(payload).subscribe((response: any) => {
      this.dialogRef.close(true);
    });
  }

  private buildPayload(): any {
    if (this.labelTable !== 'Type Convention') {
      return this.form.value;
    }

    return {
      libelle: this.form.value.libelle,
      codeCtrl: this.form.value.codeCtrl,
      templateConventions: (this.form.value.templateConventions || []).map((t: any) => ({id: t.id}))
    };
  }

  getTemplateLabel(template: any): string {
    if (!template) {
      return '';
    }

    const typeLabel = Array.isArray(template.typeConventions) && template.typeConventions.length > 0
      ? template.typeConventions
        .map((typeConvention: any) => typeConvention?.libelle)
        .filter((libelle: string | undefined) => !!libelle)
        .join(', ')
      : template.typeConvention?.libelle || 'Sans type';

    const langCode = template.langueConvention?.code || '?';
    return `${typeLabel} - ${langCode}`;
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      if (option.id != null && value.id != null) {
        return option.id === value.id;
      }
      if (option.code != null && value.code != null) {
        return option.code === value.code;
      }
    }
    return false;
  }

  emptyStringValidator(control: FormControl) {
    const isEmpty = (control.value || '').trim().length == 0;
    return isEmpty ? { 'empty': true } : null;
  }

}
