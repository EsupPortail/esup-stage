import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from "@angular/material/legacy-dialog";
import { UntypedFormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../../services/message.service";
import { UserService } from "../../../../services/user.service";

@Component({
  selector: 'app-create-dialog',
  templateUrl: './create-dialog.component.html',
  styleUrls: ['./create-dialog.component.scss']
})
export class CreateDialogComponent implements OnInit {

  constructor(
    private dialogRef: MatDialogRef<CreateDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private fb: UntypedFormBuilder,
    private userService: UserService,
    private messageService: MessageService,
  ) {
    this.data.form.get('login')?.disable();
    this.data.form.get('nom')?.disable();
    this.data.form.get('prenom')?.disable();

    this.data.form.get('login')?.setValue(this.data.ldapUser.supannAliasLogin);
    this.data.form.get('nom')?.setValue(this.data.ldapUser.sn.join(' '));
    this.data.form.get('prenom')?.setValue(this.data.ldapUser.givenName.join(' '));
  }

  ngOnInit(): void {
  }

  close(): void {
    this.dialogRef.close();
  }

  save(): void {
    if (this.data.form.valid) {
      const data = {
        ...{
          login: this.data.ldapUser.supannAliasLogin,
          nom: this.data.ldapUser.sn.join(' '),
          prenom: this.data.ldapUser.givenName.join(' '),
        },
        ...this.data.form.value
      }
      this.userService.create(data).subscribe((response: any) => {
        this.dialogRef.close();
        this.messageService.setSuccess('Utilisateur créé');
      });
    }
  }

}
