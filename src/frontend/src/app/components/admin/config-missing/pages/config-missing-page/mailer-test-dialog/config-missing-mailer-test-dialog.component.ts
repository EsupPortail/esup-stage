import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { REGEX } from '../../../../../../utils/regex.utils';

@Component({
  selector: 'app-config-missing-mailer-test-dialog',
  templateUrl: './config-missing-mailer-test-dialog.component.html',
  styleUrls: ['./config-missing-mailer-test-dialog.component.scss'],
  standalone: false
})
export class ConfigMissingMailerTestDialogComponent {
  form: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<ConfigMissingMailerTestDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      mailto: [data?.mailto ?? null, [Validators.required, Validators.pattern(REGEX.EMAIL)]],
      subject: [data?.subject ?? null, [Validators.required]],
      content: [data?.content ?? null, [Validators.required]],
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
