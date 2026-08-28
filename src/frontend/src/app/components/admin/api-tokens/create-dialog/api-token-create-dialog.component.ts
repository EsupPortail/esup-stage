import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ApiToken } from "../../../../services/api-token.service";

export interface ApiTokenFormResult {
  nom: string;
  nomApplication: string;
}

/**
 * Saisie du nom et de l'application, à la création d'un token comme à sa modification.
 * Renvoie les valeurs au composant appelant, qui se charge de l'appel serveur.
 */
@Component({
  selector: 'app-api-token-create-dialog',
  templateUrl: './api-token-create-dialog.component.html',
  styleUrls: ['./api-token-create-dialog.component.scss'],
  standalone: false
})
export class ApiTokenCreateDialogComponent {

  form: FormGroup;
  edition: boolean;

  constructor(
    private dialogRef: MatDialogRef<ApiTokenCreateDialogComponent, ApiTokenFormResult>,
    @Inject(MAT_DIALOG_DATA) public data: { apiToken?: ApiToken },
    private fb: FormBuilder,
  ) {
    this.edition = !!data?.apiToken;
    this.form = this.fb.group({
      nom: [data?.apiToken?.nom ?? null, Validators.required],
      nomApplication: [data?.apiToken?.nomApplication ?? null, Validators.required],
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.value as ApiTokenFormResult);
  }
}
