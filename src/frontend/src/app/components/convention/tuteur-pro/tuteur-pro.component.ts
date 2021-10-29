import { Component, EventEmitter, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CiviliteService } from "../../../services/civilite.service";
import { ContactService } from "../../../services/contact.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";

@Component({
  selector: 'app-tuteur-pro',
  templateUrl: './tuteur-pro.component.html',
  styleUrls: ['./tuteur-pro.component.scss']
})
export class TuteurProComponent implements OnInit, OnChanges {

  civilites: any[] = [];

  data: any;

  @Input() service = {id:null!};
  contacts:any[] = [];

  contact: any;
  modif: boolean = false;
  form: FormGroup;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  constructor(public contactService: ContactService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private civiliteService: CiviliteService,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(150)]],
      prenom: [null, [Validators.required, Validators.maxLength(200)]],
      civilite: [null, []],
      fonction: [null, [Validators.required, Validators.maxLength(200)]],
      tel: [null, [Validators.maxLength(200)]],
      fax: [null, [Validators.maxLength(200)]],
      mail: [null, [Validators.required, Validators.email, Validators.maxLength(200)]],
    });
  }

  ngOnInit(): void {
    this.civiliteService.getPaginated(1, 0, 'libelle', 'asc','').subscribe((response: any) => {
      this.civilites = response.data;
    });
  }

  ngOnChanges(): void{
    this.refreshContacts();
  }

  refreshContacts(): void{
    if (this.service){
      this.contactService.getByService(this.service.id).subscribe((response: any) => {
        this.contacts = response;
      });
    }
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.MODIFICATION]});
  }

  choose(row: any): void {
    this.modif = false;
    if (this.firstPanel) {
      this.firstPanel.expanded = false;
    }
    this.validated.emit(2);
  }

  initCreate(): void {
    this.contact = {};
    this.form.reset();
    this.modif = true;
  }

  edit(): void {
    console.log('data : ' + JSON.stringify(this.contact, null, 2));
    this.form.setValue({
      nom: this.contact.nom,
      prenom: this.contact.prenom,
      civilite: this.contact.civilite,
      fonction: this.contact.fonction,
      tel: this.contact.tel,
      fax: this.contact.fax,
      mail: this.contact.mail,
    });
    this.modif = true;
  }

  cancelEdit(): void {
    this.modif = false;
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  save(): void {
    if (this.form.valid) {

      const data = {...this.form.value};

      //ajoute idService à l'objet contact
      data.service = {'id':this.service.id};

      if (this.contact.id) {
        this.contactService.update(this.contact.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Contact modifié');
          this.contact = response;
          this.refreshContacts();
          this.modif = false;
        });
      } else {
        this.contactService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Contact créé');
          this.contact = response;
          this.refreshContacts();
          this.choose(this.contact);
        });
      }
    }
  }

}

