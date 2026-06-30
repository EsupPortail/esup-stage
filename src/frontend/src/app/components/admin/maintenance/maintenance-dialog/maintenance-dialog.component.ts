import { Component, Inject } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

function datFinAfterDebut(control: AbstractControl): ValidationErrors | null {
  const parent = control.parent;
  if (!parent) return null;

  const datDeb: Date | null = parent.get('datDebMaint')?.value;
  const heureDeb: string | null = parent.get('heureDebMaint')?.value;
  const datFin: Date | null = control.value;
  const heureFin: string | null = parent.get('heureFinMaint')?.value;

  if (!datDeb || !datFin) return null;

  const debut = mergeDateAndTime(datDeb, heureDeb);
  const fin = mergeDateAndTime(datFin, heureFin);

  if (fin <= debut) {
    return { datFinBeforeDebut: true };
  }
  return null;
}

function mergeDateAndTime(date: Date, time: string | null): Date {
  const result = new Date(date);
  if (time) {
    const [hours, minutes] = time.split(':').map(Number);
    result.setHours(hours, minutes, 0, 0);
  } else {
    result.setHours(0, 0, 0, 0);
  }
  return result;
}

function toLocalDateTimeString(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}

@Component({
  selector: 'app-maintenance-dialog',
  templateUrl: './maintenance-dialog.component.html',
  styleUrls: ['./maintenance-dialog.component.scss'],
  standalone: false
})
export class MaintenanceDialogComponent {

  form: FormGroup;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<MaintenanceDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {
    this.form = this.fb.group({
      datDebMaint: [null, [Validators.required]],
      heureDebMaint: ['00:00'],
      datFinMaint: [null, [datFinAfterDebut]],
      heureFinMaint: ['00:00'],
      datAlertMaint: [null],
      heureAlertMaint: ['00:00'],
      message: [null, [Validators.maxLength(500)]],
    });

    // Revalider datFinMaint quand la date ou l'heure de début change
    this.form.get('datDebMaint')?.valueChanges.subscribe(() => {
      this.form.get('datFinMaint')?.updateValueAndValidity();
    });
    this.form.get('heureDebMaint')?.valueChanges.subscribe(() => {
      this.form.get('datFinMaint')?.updateValueAndValidity();
    });
    this.form.get('heureFinMaint')?.valueChanges.subscribe(() => {
      this.form.get('datFinMaint')?.updateValueAndValidity();
    });

    if (this.data?.maintenance) {
      this.isEditMode = true;
      this.patchForm(this.data.maintenance);
    }
  }

  close(): void {
    this.dialogRef.close();
  }

  save(): void {
    if (!this.form.valid) {
      this.form.markAllAsTouched();
      return;
    }

    const v = this.form.value;
    const datDebMaint = v.datDebMaint ? mergeDateAndTime(v.datDebMaint, v.heureDebMaint) : null;
    const datFinMaint = v.datFinMaint ? mergeDateAndTime(v.datFinMaint, v.heureFinMaint) : null;
    const datAlertMaint = v.datAlertMaint ? mergeDateAndTime(v.datAlertMaint, v.heureAlertMaint) : null;

    const data = {
      datDebMaint: datDebMaint ? toLocalDateTimeString(datDebMaint) : null,
      datFinMaint: datFinMaint ? toLocalDateTimeString(datFinMaint) : null,
      datAlertMaint: datAlertMaint ? toLocalDateTimeString(datAlertMaint) : null,
      message: v.message?.trim() || null,
    };

    this.dialogRef.close(data);
  }

  private patchForm(maintenance: any): void {
    const datDebMaint = this.toDate(maintenance?.datDebMaint);
    const datFinMaint = this.toDate(maintenance?.datFinMaint);
    const datAlertMaint = this.toDate(maintenance?.datAlertMaint);

    this.form.patchValue({
      datDebMaint,
      heureDebMaint: this.toTimeString(datDebMaint),
      datFinMaint,
      heureFinMaint: this.toTimeString(datFinMaint),
      datAlertMaint,
      heureAlertMaint: this.toTimeString(datAlertMaint),
      message: maintenance?.message ?? null,
    });
  }

  private toTimeString(date: Date | null): string {
    if (!date) {
      return '00:00';
    }
    const h = String(date.getHours()).padStart(2, '0');
    const m = String(date.getMinutes()).padStart(2, '0');
    return `${h}:${m}`;
  }

  private toDate(value: any): Date | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? null : value;
    }

    if (Array.isArray(value)) {
      return this.buildDateFromParts(value);
    }

    if (typeof value === 'string') {
      const raw = value.trim();
      if (!raw) {
        return null;
      }

      const commaSeparated = raw.replace(/^\[|\]$/g, '');
      if (/^\d{4},\d{1,2},\d{1,2}(,\d{1,2}){0,4}$/.test(commaSeparated)) {
        const parts = commaSeparated.split(',').map((part) => Number(part.trim()));
        return this.buildDateFromParts(parts);
      }

      const normalized = raw.includes('T') ? raw : raw.replace(' ', 'T');
      const date = new Date(normalized);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    if (typeof value === 'number') {
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    return null;
  }

  private buildDateFromParts(parts: number[]): Date | null {
    const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = parts;
    if (!year || !month || !day) {
      return null;
    }

    const milliseconds = Math.floor((Number(nano) || 0) / 1_000_000);
    const date = new Date(
      Number(year),
      Number(month) - 1,
      Number(day),
      Number(hour) || 0,
      Number(minute) || 0,
      Number(second) || 0,
      milliseconds
    );
    return Number.isNaN(date.getTime()) ? null : date;
  }
}
