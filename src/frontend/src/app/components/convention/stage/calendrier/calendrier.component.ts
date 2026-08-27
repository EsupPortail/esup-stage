import { Component, Inject, OnInit, Output, EventEmitter } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { PeriodeStageService } from "../../../../services/periode-stage.service"

@Component({
    selector: 'app-calendrier',
    templateUrl: './calendrier.component.html',
    styleUrls: ['./calendrier.component.scss'],
    standalone: false
})
export class CalendrierComponent implements OnInit  {

  periodes: any[] = [];
  interruptionsStage: any[] = [];

  periodesForm!: FormGroup;
  heuresJournalieresForm!: FormGroup;

  convention: any;
  idPeriod : number = 0;

  calendarDateFilter: any;

  @Output() periodesChange = new EventEmitter<any[]>();

  constructor(private fb: FormBuilder,
              private dialogRef: MatDialogRef<CalendrierComponent>,
              private periodeStageService: PeriodeStageService,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.interruptionsStage = data.interruptionsStage;
    if (data.periodes) {
      this.periodes = data.periodes;
      this.idPeriod = this.periodes.length;
    }
  }

  ngOnInit(): void {
    this.periodesForm = this.fb.group({
      dateDebut: [null, [Validators.required]],
      dateFin: [null, [Validators.required]],
    });
    this.heuresJournalieresForm = this.fb.group({});

    // Initialize form controls for existing periodes
    this.periodes.forEach((periode, index) => {
      const controlName = 'heuresJournalieres' + index;
      periode.formControlName = controlName;
      this.heuresJournalieresForm.addControl(
        controlName,
        new FormControl(periode.nbHeuresJournalieres, Validators.required)
      );
    });

    this.calendarDateFilter = (d: Date | null): boolean => {
      const date = (d || new Date());

      let disable = false;
      // Disable dates already chosen
      for (const periode of this.periodes) {
        if (date >= periode.dateDebut && date <= periode.dateFin) {
          disable = true;
        }
      }
      // Disable dates not within stage period
      if (date < this.dateFromBackend(this.convention.dateDebutStage) || date > this.dateFromBackend(this.convention.dateFinStage)) {
        disable = true;
      }
      // Disable dates within stage interruption period
      for (const periode of this.interruptionsStage) {
        if (date >= this.dateFromBackend(periode.dateDebutInterruption) && date <= this.dateFromBackend(periode.dateFinInterruption)) {
          disable = true;
        }
      }
      return !disable;
    };
  }

  dateFromBackend(dateString: string): Date {
    let date = new Date(dateString);
    date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    return date;
  }

  addPeriode(): void {
    if (this.periodesForm.valid) {
      const data = { ...this.periodesForm.value };
      data.formControlName = 'heuresJournalieres' + this.idPeriod;
      this.idPeriod += 1;
      this.periodes.push(data);
      this.heuresJournalieresForm.addControl(data.formControlName, new FormControl(null, Validators.required));
      this.periodesForm.reset();
    }
  }

  removePeriode(periode: any): void {
    this.removeItemOnce(this.periodes, periode);
    if(periode.id){
      this.periodeStageService.delete(periode.id).subscribe();
    }
    this.heuresJournalieresForm.removeControl(periode.formControlName);
  }

  removeItemOnce(arr: any[], value: any): any[] {
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
      for (let periode of this.periodes) {
        periode.nbHeuresJournalieres = parseFloat(this.heuresJournalieresForm.get(periode.formControlName)!.value);
      }
      this.periodesChange.emit(this.periodes);
      this.dialogRef.close(this.periodes);
    }
  }
}
