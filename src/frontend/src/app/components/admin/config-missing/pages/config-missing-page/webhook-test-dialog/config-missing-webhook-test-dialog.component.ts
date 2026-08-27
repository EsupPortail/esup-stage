import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-config-missing-webhook-test-dialog',
  templateUrl: './config-missing-webhook-test-dialog.component.html',
  styleUrls: ['./config-missing-webhook-test-dialog.component.scss'],
  standalone: false
})
export class ConfigMissingWebhookTestDialogComponent {
  form: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<ConfigMissingWebhookTestDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      suffix: [data?.suffix ?? null, [Validators.required]],
    });
  }

  close(): void {
    this.dialogRef.close();
  }

  confirm(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.dialogRef.close(this.form.value);
  }
}
