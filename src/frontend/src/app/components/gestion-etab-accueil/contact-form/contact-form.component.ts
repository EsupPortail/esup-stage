import { Component, Inject, OnInit, OnDestroy } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { ContactService } from "../../../services/contact.service";
import { REGEX } from "../../../utils/regex.utils";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { BehaviorSubject, Subject, takeUntil } from 'rxjs';
import {debounceTime} from "rxjs/operators";
import { CentreGestionSearchModel } from 'src/app/models/centre-gestion-search.model';

@Component({
  selector: 'app-contact-form',
  templateUrl: './contact-form.component.html',
  styleUrls: ['./contact-form.component.scss']
})
export class ContactFormComponent implements OnInit, OnDestroy {

  contact: any;
  service: any;
  civilites: any[] = [];
  form: any;
  protected centreGestions: CentreGestionSearchModel[] = [];
  protected filteredCentreGestions = new BehaviorSubject<CentreGestionSearchModel[]>([]);
  protected centreGestionsFilterCtrl = new FormControl<string>('');
  _onDestroy = new Subject<void>();

  constructor(public contactService: ContactService,
              private dialogRef: MatDialogRef<ContactFormComponent>,
              private fb: FormBuilder,
              private centreGestionService: CentreGestionService,
              @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.contact = data.contact;
    this.service = data.service;
    this.civilites = data.civilites;
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.required, Validators.maxLength(100)]],
      tel: [null, [Validators.required, Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
      fax: [null, [Validators.maxLength(50)]],
      idCentreGestion: [null, [Validators.required]],
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
        idCentreGestion: this.contact.centreGestion.id ? this.contact.centreGestion.id : null,
      });
    }
  }

  ngOnInit(): void {
    this.centreGestionsFilterCtrl = new FormControl('');
    this.loadCentreGestions();
    this.centreGestionsFilterCtrl.valueChanges
      .pipe(debounceTime(300), takeUntil(this._onDestroy))
      .subscribe(() => this.filterCentreGestions());
  }

  loadCentreGestions(): void {
    this.centreGestionService.getAll().subscribe((response: any[]) => {
      this.centreGestions = response.sort((a, b) => (a.nomCentre || '').localeCompare(b.nomCentre || ''));
      this.filteredCentreGestions.next(this.centreGestions.slice());
    });
  }

  close(): void {
    this.dialogRef.close(null);
  }

  save(): void {
    if (this.form.valid) {
      const data = { ...this.form.value };
      console.log(data);
      if (this.contact) {
        this.contactService.update(this.contact.id, data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      } else {
        data.idService = this.service.id;
        this.contactService.create(data).subscribe((response: any) => {
          this.contact = response;
          this.dialogRef.close(this.contact);
        });
      }
    }
  }

  private filterCentreGestions(): void {
    if (!this.centreGestions) return;
    const search = (this.centreGestionsFilterCtrl.value || '').toLowerCase();
    this.filteredCentreGestions.next(
      !search
        ? this.centreGestions.slice()
        : this.centreGestions.filter(c =>
          (c.nomCentre || '').toLowerCase().includes(search)
        )
    );
  }

  ngOnDestroy() {
    this._onDestroy.next();
    this._onDestroy.complete();
  }
}
