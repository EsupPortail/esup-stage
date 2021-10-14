import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";

@Component({
  selector: 'app-admin-nomenclatures-creation',
  templateUrl: './admin-nomenclatures-creation.component.html',
  styleUrls: ['./admin-nomenclatures-creation.component.scss']
})
export class AdminNomenclaturesCreationComponent implements OnInit {

  form: FormGroup;
  service: any;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AdminNomenclaturesCreationComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.service = data.service;
    if (data.creationFormType == 1) {
      this.form = this.fb.group({
        libelle: [null, [Validators.required, Validators.maxLength(100)]],
      });
    }
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close(false);
  }

  save(): void {
    if (this.form.valid) {
      this.service.create(this.form.value).subscribe((response: any) => {
        this.dialogRef.close(true);
      });
    }
  }

}
