import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { TypeStructureService } from "../../../../services/type-structure.service";
import { TypeOffreService } from "../../../../services/type-offre.service";
import { TemplateConventionService } from "../../../../services/template-convention.service";

@Component({
    selector: 'app-admin-nomenclatures-edition',
    templateUrl: './admin-nomenclatures-edition.component.html',
    styleUrls: ['./admin-nomenclatures-edition.component.scss'],
    standalone: false
})
export class AdminNomenclaturesEditionComponent implements OnInit {

  form!: FormGroup;
  service: any;
  data: any;
  labelTable: string;
  typeStructures: any;
  typeOffres: any;
  templateConventions: any[] = [];

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesEditionComponent>,
    private typeStructureService: TypeStructureService,
    private typeOffreService: TypeOffreService,
    private templateConventionService: TemplateConventionService,
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
      case 'Type Convention':
        this.setTypeConventionForm();
        this.loadTemplateConventions();
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
    if (labelTable === 'Type Convention') {
      this.form.patchValue({
        libelle: this.data.libelle,
      });
      this.patchSelectedTemplates();
      return;
    }

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

  setTypeConventionForm() {
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(255)]],
      templateConventions: [[], []],
    });
  }

  private loadTemplateConventions(): void {
    this.templateConventionService.getAll().subscribe((response: any) => {
      this.templateConventions = (response || []).slice().sort((a: any, b: any) => this.getTemplateLabel(a).localeCompare(this.getTemplateLabel(b)));
      this.patchSelectedTemplates();
    });
  }

  private patchSelectedTemplates(): void {
    if (this.labelTable !== 'Type Convention' || !this.form || !this.form.get('templateConventions')) {
      return;
    }
    if (!this.templateConventions || this.templateConventions.length === 0) {
      return;
    }

    const currentTypeId = this.data?.id;
    if (!currentTypeId) {
      return;
    }

    const selected = this.templateConventions.filter((template: any) => {
      const linkedManyToMany = Array.isArray(template.typeConventions)
        && template.typeConventions.some((tc: any) => tc && tc.id === currentTypeId);
      const linkedLegacy = template.typeConvention && template.typeConvention.id === currentTypeId;
      return linkedManyToMany || linkedLegacy;
    });

    this.form.patchValue({templateConventions: selected}, {emitEvent: false});
  }

  save(): void {
    if (!this.form.valid) {
      return;
    }

    let key = this.data.id ? this.data.id : this.data.code;
    const payload = this.buildPayload();
    this.service.update(key, payload).subscribe((response: any) => {
      this.data = response;
      this.setFormData(this.labelTable);
      this.dialogRef.close(true);
    });
  }

  private buildPayload(): any {
    if (this.labelTable !== 'Type Convention') {
      return this.form.value;
    }

    return {
      libelle: this.form.value.libelle,
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
