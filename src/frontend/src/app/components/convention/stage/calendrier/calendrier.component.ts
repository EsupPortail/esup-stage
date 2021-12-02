import {Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";

@Component({
  selector: 'app-calendrier',
  templateUrl: './calendrier.component.html',
  styleUrls: ['./calendrier.component.scss'],
})
export class CalendrierComponent implements OnInit  {

  periodes: any[] = [];

  stageForm: FormGroup;
  periodesForm: FormGroup;
  heuresJournalieresForm: FormGroup;

  idPeriod : number = 0;

  constructor(private fb: FormBuilder,
              private dialogRef: MatDialogRef<CalendrierComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.stageForm = data.form
  }

  ngOnInit(): void {
    this.periodesForm = this.fb.group({
      calendrierStartDate: [null, [Validators.required]],
      calendrierEndDate: [null, [Validators.required]],
    })
    this.heuresJournalieresForm = this.fb.group({})
  }

  addPeriode(): void {
    if (this.periodesForm.valid){
      const data = {...this.periodesForm.value};
      data.formControlName = 'heuresJournalieres' + this.idPeriod;
      this.idPeriod += 1;
      this.periodes.push(data);
      this.heuresJournalieresForm.addControl(data.formControlName, new FormControl(null, Validators.required));
    }

  }

  removePeriode(periode: any): void {
    this.removeItemOnce(this.periodes,periode);
    this.heuresJournalieresForm.removeControl(periode.formControlName);
  }

  removeItemOnce(arr : any[], value : any) : any[] {
    const index = arr.indexOf(value);
    if (index > -1) {
      arr.splice(index, 1);
    }
    return arr;
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.heuresJournalieresForm.valid) {
      console.log('valid : ' + JSON.stringify(this.periodes, null, 2))
    }
  }

}
