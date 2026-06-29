import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-centre-proprietaire-dialog',
  templateUrl: './centre-proprietaire-dialog.component.html',
  styleUrls: ['./centre-proprietaire-dialog.component.scss'],
  standalone: false
})
export class CentreProprietaireDialogComponent {

  form: FormGroup;

  constructor(
    private readonly dialogRef: MatDialogRef<CentreProprietaireDialogComponent>,
    private readonly fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { centres: any[] }
  ) {
    this.form = this.fb.group({
      idCentreGestionProprietaire: [null, Validators.required]
    });
  }

  cancel(): void {
    this.dialogRef.close(null);
  }

  valid(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.value.idCentreGestionProprietaire);
  }
}
