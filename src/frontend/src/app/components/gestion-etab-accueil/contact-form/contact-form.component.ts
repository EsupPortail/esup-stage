import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, Validators } from "@angular/forms";
import { ContactService } from "../../../services/contact.service";
import { AuthService } from "../../../services/auth.service";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import {REGEX} from "../../../utils/regex.utils";

@Component({
    selector: 'app-contact-form',
    templateUrl: './contact-form.component.html',
    styleUrls: ['./contact-form.component.scss'],
    standalone: false
})
export class ContactFormComponent implements OnInit {

  contact: any;
  service: any;
  civilites: any[] = [];
  gestionnaireCentres: any[] = [];

  form: any;
  idCentreGestion: number | null = null;

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
    this.idCentreGestion = data.idCentreGestion ?? null
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.required, Validators.maxLength(100)]],
      tel: [null, [Validators.required, Validators.pattern(REGEX.PHONE), Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
      fax: [null, [Validators.maxLength(50)]],
      idCentreGestion: [this.idCentreGestion, []],
    });

    if (this.contact) {
      this.form.setValue({
        nom: this.contact.nom,
        prenom: this.contact.prenom,
        idCivilite: this.contact.civilite ? this.contact.civilite.id : null,
        fonction: this.contact.fonction,
        tel: this.contact.tel,
        fax: this.contact.fax,
        mail: this.contact.mail,
        idCentreGestion: this.contact.idCentreGestion ?? this.contact.centreGestionnaire?.id ?? null,
      });
    }
  }

  ngOnInit(): void {
    this.loadGestionnaireCentres();
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {

      const data = {...this.form.value};

      if (this.contact) {
        this.contactService.update(this.contact.id, data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      } else {
        data.idService = this.service.id;
        const idCentreGestion = this.form.get('idCentreGestion')?.value ?? this.idCentreGestion;
        if (idCentreGestion) {
          data.idCentreGestion = idCentreGestion;
        }
        this.contactService.create(data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      }
    }
  }

  private loadGestionnaireCentres(): void {
    if (!this.authService.isGestionnaire() || this.contact) {
      return;
    }
    const uid = this.authService.userConnected?.uid ?? this.authService.userConnected?.login;
    if (!uid) {
      return;
    }
    const filters = JSON.stringify({
      personnel: { value: uid, specific: true }
    });
    this.centreGestionService.getPaginated(1, 0, 'nomCentre', 'asc', filters).subscribe((response: any) => {
      this.gestionnaireCentres = response?.data ?? [];
      this.applyCentreGestionSelection();
    });
  }

  private applyCentreGestionSelection(): void {
    const control = this.form.get('idCentreGestion');
    if (!control) {
      return;
    }
    if (this.gestionnaireCentres.length === 1) {
      control.setValue(this.gestionnaireCentres[0].id);
      control.clearValidators();
    } else if (this.gestionnaireCentres.length > 1) {
      const selectedCentre = this.gestionnaireCentres.find(centre => centre.id === this.idCentreGestion);
      control.setValue(selectedCentre ? selectedCentre.id : null);
      control.setValidators([Validators.required]);
    } else {
      control.clearValidators();
    }
    control.updateValueAndValidity();
  }
}