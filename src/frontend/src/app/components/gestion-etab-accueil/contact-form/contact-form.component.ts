import { Component, EventEmitter, Input, Output, SimpleChanges, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { ContactService } from "../../../services/contact.service";
import { MessageService } from "../../../services/message.service";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-contact-form',
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.scss']
})
export class ContactFormComponent implements OnInit {

  contact: any;
  service: any;
  civilites: any[] = [];
  idCentreGestion: number|null;

  form: any;
  centreGestions: any[] = [];

  constructor(public contactService: ContactService,
              private dialogRef: MatDialogRef<ContactFormComponent>,
              private fb: FormBuilder,
              private authService: AuthService,
              private centreGestionService: CentreGestionService,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.contact = data.contact
    this.service = data.service
    this.civilites = data.civilites
    this.idCentreGestion = data.idCentreGestion;
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.required, Validators.maxLength(100)]],
      tel: [null, [Validators.required, Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.email, Validators.maxLength(50)]],
      fax: [null, [Validators.maxLength(50)]],
      idCentreGestion: [this.idCentreGestion, []],
    });

    // Dans le cas où on n'a pas de centre de gestion, la sélection est obligatoire
    if (!this.idCentreGestion) {
      this.form.get('idCentreGestion')?.setValidators([Validators.required]);
    }

    if (this.contact) {
      this.form.setValue({
        nom: this.contact.nom,
        prenom: this.contact.prenom,
        idCivilite: this.contact.civilite ? this.contact.civilite.id : null,
        fonction: this.contact.fonction,
        tel: this.contact.tel,
        fax: this.contact.fax,
        mail: this.contact.mail,
      });
    }
  }

  ngOnInit(): void {
    const filters = {
      personnel: { type: 'text', value: this.authService.userConnected.login, specific: true }
    };
    this.centreGestionService.getPaginated(1, 0, 'nomCentre', 'asc', JSON.stringify(filters)).subscribe((response: any) => {
      this.centreGestions = response.data;
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {

      const data = {...this.form.value};
      if (this.idCentreGestion) {
        data.idCentreGestion = this.idCentreGestion; // ajout de l'id cetnre de gestion passé en paramètre (correspondant à la convention)
      }

      if (this.contact) {
        this.contactService.update(this.contact.id, data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      } else {
        //ajoute idService à l'objet contact
        data.idService = this.service.id;
        this.contactService.create(data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      }
    }
  }
}
