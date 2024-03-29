import {Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";

@Component({
  selector: 'app-interruptions-form',
  templateUrl: './interruptions-form.component.html',
  styleUrls: ['./interruptions-form.component.scss'],
})
export class InterruptionsFormComponent implements OnInit  {

  periodes: any[] = [];
  interruptionsStage: any[] = [];

  periodesForm!: FormGroup;

  convention: any;
  interruptionStage: any;
  initialPeriodesLength: number = 0;

  interruptionsDateFilter: any;

  constructor(private fb: FormBuilder,
              private dialogRef: MatDialogRef<InterruptionsFormComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.interruptionsStage = data.interruptionsStage.slice();
    this.interruptionStage = data.interruptionStage;

    if (data.periodes){
      this.periodes = data.periodes.slice();
      this.initialPeriodesLength = this.periodes.length;
    }

    const date = new Date();

    if (this.interruptionStage){
      this.removeItemOnce(this.interruptionsStage,this.interruptionStage);
    }
  }

  ngOnInit(): void {
    this.periodesForm = this.fb.group({
      dateDebutInterruption: [null, [Validators.required]],
      dateFinInterruption: [null, [Validators.required]],
    })

    this.interruptionsDateFilter = (d: Date | null): boolean => {
        const date = (d || new Date());

        let disable = false;

        //disable dates already chosen
        for (const periode of this.periodes) {
            if (date >= periode.dateDebutInterruption && date <= periode.dateFinInterruption){
              disable = true;
            }
        }
        for (const periode of this.interruptionsStage) {
            if (date >= this.dateFromBackend(periode.dateDebutInterruption) && date <= this.dateFromBackend(periode.dateFinInterruption)){
              disable = true;
            }
        }
        //disable dates not within stage period
        if (date < this.dateFromBackend(this.convention.dateDebutStage) || date > this.dateFromBackend(this.convention.dateFinStage)){
          disable = true;
        }
        return !disable;
    };

  }

  //fix new date('2021-02-02') != new date(2021,2,2)
  dateFromBackend(dateString: string):Date{
    let date = new Date(dateString)
    date = new Date(date.getFullYear(),date.getMonth(),date.getDate());
    return date;
  }

  addPeriode(): void {
    if (this.periodesForm.valid){
      const data = {...this.periodesForm.value};
      data.idConvention = this.convention.id;
      this.periodes.push(data);
      this.clearDatePicker();
    }
  }

  clearDatePicker(): void {
    this.periodesForm.reset();
  }

  removePeriode(periode: any): void {
    this.removeItemOnce(this.periodes,periode);
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
    if (this.interruptionStage){
      if (this.periodesForm.valid){
        const data = {...this.periodesForm.value};
        data.idConvention = this.convention.id;
        data.interruption = true;
        this.dialogRef.close(data);
      }
    }else{
      if (this.periodes.length > 0 || this.initialPeriodesLength > 0 ){
        this.dialogRef.close(this.periodes);
      }
    }
  }

}
