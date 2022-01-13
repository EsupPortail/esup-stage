import { Component, EventEmitter, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CiviliteService } from "../../../services/civilite.service";
import { ContactService } from "../../../services/contact.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { ConfigService } from "../../../services/config.service";

@Component({
  selector: 'app-tuteur-pro',
  templateUrl: './tuteur-pro.component.html',
  styleUrls: ['./tuteur-pro.component.scss']
})
export class TuteurProComponent implements OnInit, OnChanges {

  civilites: any[] = [];

  data: any;

  @Input() service: any;
  @Input() etab: any;

  @Input() contact: any;
  contacts:any[] = [];

  modif: boolean = false;
  form: FormGroup;

  autorisationModification = false;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  @Input() modifiable: boolean;

  constructor(public contactService: ContactService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private civiliteService: CiviliteService,
              private configService: ConfigService,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.maxLength(100)]],
      tel: [null, [Validators.required, Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.pattern('[^@ ]+@[^@. ]+\\.[^@. ]+'), Validators.maxLength(50)]],
      fax: [null, [Validators.maxLength(50)]],
    });
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.autorisationModification = response.autoriserEtudiantAModifierEntreprise;
    });
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
    return this.modifiable && this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    let hasRight = this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.MODIFICATION]});
    if (this.authService.isEtudiant() && !this.autorisationModification) {
      hasRight = false;
    }
    return this.modifiable && hasRight;
  }

  choose(row: any): void {
    this.modif = false;
    if (this.firstPanel) {
      this.firstPanel.expanded = false;
    }
    this.validated.emit(this.contact);
  }

  initCreate(): void {
    this.contact = {};
    this.form.reset();
    this.modif = true;
  }

  edit(): void {
    this.form.setValue({
      nom: this.contact.nom,
      prenom: this.contact.prenom,
      idCivilite: this.contact.civilite ? this.contact.civilite.id : null,
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

      if (this.contact.id) {
        this.contactService.update(this.contact.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Contact modifié');
          this.contact = response;
          this.refreshContacts();
          this.modif = false;
        });
      } else {

        //ajoute idService à l'objet contact
        data.idService = this.service.id;

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

