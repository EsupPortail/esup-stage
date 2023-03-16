import {Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";

@Component({
  selector: 'app-calendrier',
  templateUrl: './calendrier.component.html',
  styleUrls: ['./calendrier.component.scss'],
})
export class CalendrierComponent implements OnInit  {

  periodes: any[] = [];
  interruptionsStage: any[] = [];

  periodesForm: FormGroup;
  heuresJournalieresForm: FormGroup;

  convention: any;
  idPeriod : number = 0;

  calendarDateFilter: any;

  constructor(private fb: FormBuilder,
              private dialogRef: MatDialogRef<CalendrierComponent>,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.interruptionsStage = data.interruptionsStage;
  }

  ngOnInit(): void {
    this.periodesForm = this.fb.group({
      dateDebut: [null, [Validators.required]],
      dateFin: [null, [Validators.required]],
    })
    this.heuresJournalieresForm = this.fb.group({})

    this.calendarDateFilter = (d: Date | null): boolean => {
        const date = (d || new Date());

        let disable = false;
        //disable dates already chosen
        for (const periode of this.periodes) {
            if (date >= periode.dateDebut && date <= periode.dateFin){
              disable = true;
            }
        }
        //disable dates not within stage period
        if (date < this.dateFromBackend(this.convention.dateDebutStage) || date > this.dateFromBackend(this.convention.dateFinStage)){
          disable = true;
        }
        //disable dates within stage interruption period
        for (const periode of this.interruptionsStage) {
            if (date >= this.dateFromBackend(periode.dateDebutInterruption) && date <= this.dateFromBackend(periode.dateFinInterruption)){
              disable = true;
            }
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
      data.formControlName = 'heuresJournalieres' + this.idPeriod;
      this.idPeriod += 1;
      this.periodes.push(data);
      this.heuresJournalieresForm.addControl(data.formControlName, new FormControl(null, Validators.required));
      this.periodesForm.reset();
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
      for(let periode of this.periodes){
        periode.nbHeuresJournalieres = parseInt(this.heuresJournalieresForm.get(periode.formControlName)!.value);
      }
      this.dialogRef.close(this.periodes);
    }
  }

}
