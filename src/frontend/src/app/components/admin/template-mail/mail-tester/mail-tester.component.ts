import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { MessageService } from "../../../../services/message.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { TemplateMailService } from "../../../../services/template-mail.service";

@Component({
  selector: 'app-mail-tester',
  templateUrl: './mail-tester.component.html',
  styleUrls: ['./mail-tester.component.scss']
})
export class MailTesterComponent implements OnInit {

  form: FormGroup;

  constructor(
    private dialogRef: MatDialogRef<MailTesterComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: FormBuilder,
    private templateMailService: TemplateMailService,
    private messageService: MessageService,
  ) {
    this.form = this.fb.group({
      email: [null, [Validators.required, Validators.pattern('[^@ ]+@[^@. ]+\\.[^@ ]+')]],
    });
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close();
  }

  send(): void {
    if (this.form.valid) {
      const data = {
        templateMail: this.data.code,
        to: this.form.get('email')?.value,
      }
      this.templateMailService.sendMailTest(data).subscribe((response: any) => {
        this.dialogRef.close();
        this.messageService.setSuccess('Mail de test envoyé');
      });
    }
  }

}
