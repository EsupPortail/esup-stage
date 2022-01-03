import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";

@Component({
  selector: 'app-admin-nomenclatures-edition',
  templateUrl: './admin-nomenclatures-edition.component.html',
  styleUrls: ['./admin-nomenclatures-edition.component.scss']
})
export class AdminNomenclaturesEditionComponent implements OnInit {

  form: FormGroup;
  service: any;
  data: any;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesEditionComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service;
    this.data = data.data;
    this.form = this.fb.group({
      libelle: [null, [this.emptyStringValidator, Validators.maxLength(100)]],
    });
    this.setFormData();
  }

  ngOnInit(): void {
  }

  setFormData(): void {
    const data = {...this.data};
    delete data.id;
    delete data.code;
    delete data.codeCtrl;
    delete data.temEnServ;
    delete data.modifiable;
    delete data.siretObligatoire;
    this.form.setValue(data);
  }

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.form.valid) {
      let key = this.data.id ? this.data.id : this.data.code;
      this.service.update(key, this.form.value).subscribe((response: any) => {
        this.data = response;
        this.setFormData();
        this.dialogRef.close(true);
      });
    }
  }

  emptyStringValidator(control: FormControl) {
    const isEmpty = (control.value || '').trim().length == 0;
    return isEmpty ? { 'empty': true } : null;
  }

}
