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
import { debounceTime } from "rxjs/operators";
import { LdapService } from "../../../services/ldap.service";
import {REGEX} from "../../../utils/regex.utils";

@Component({
  selector: 'app-tuteur-pro',
  templateUrl: './tuteur-pro.component.html',
  styleUrls: ['./tuteur-pro.component.scss']
})
export class TuteurProComponent implements OnInit, OnChanges {

  civilites: any[] = [];
  columns = ['nomprenom', 'mail', 'departement', 'action'];

  data: any;

  @Input() service: any;
  @Input() etab: any;
  @Input() centreGestion: any;
  @Input() contact: any;
  @Input() modifiable!: boolean;
  @Input() enMasse!: boolean;
  @Output() validated = new EventEmitter<number>();

  contacts:any[] = [];
  staffs: any[] = [];
  staff: any;

  modif: boolean = false;
  form: FormGroup;
  searchForm: FormGroup;

  autorisationModification = false;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  constructor(public contactService: ContactService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private ldapService: LdapService,
              private civiliteService: CiviliteService,
              private configService: ConfigService,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.maxLength(100)]],
<<<<<<< HEAD
      tel: [null, [Validators.required, Validators.pattern(/^(?:(?:\+|00)\d{1,4}[-.\s]?|0)\d{1,4}([-.\s]?\d{1,4})*$/), Validators.maxLength(50)]],
=======
      tel: [null, [Validators.required, Validators.maxLength(50)]],
>>>>>>> feature/dockerisation
      mail: [null, [Validators.required, Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
      fax: [null, [Validators.maxLength(50)]],
    });

    this.searchForm = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.autorisationModification = response.autoriserEtudiantAModifierEntreprise;
    });
    this.civiliteService.getPaginated(1, 0, 'libelle', 'asc','').subscribe((response: any) => {
      this.civilites = response.data;
    });
    this.searchForm.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      this.search();
    });
  }

  ngOnChanges(): void{
    this.refreshContacts();
  }

  refreshContacts(): void{
    if (this.service){
      this.contactService.getByService(this.service.id, this.centreGestion.id).subscribe((response: any) => {
        this.contacts = response;
      });
    }
  }

  canCreate(): boolean {
    let hasRight = this.modifiable && this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
    if (this.authService.isEtudiant() && !this.autorisationModification) {
      hasRight = false;
    }
    return this.modifiable && hasRight;
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

  chooseStaff(row: any): void {
    this.contact = {};
    let civilite = 'M.' ? this.civilites.find(c => c.libelle === 'Mr') : this.civilites.find(c => c.libelle === 'Mme')
    this.form.setValue({
      nom: row.sn.join(' '),
      prenom: row.givenName.join(' '),
      idCivilite: civilite ? civilite.id : null,
      fonction: row.eduPersonPrimaryAffiliation,
      tel: row.telephoneNumber,
      fax: '',
      mail: row.mail,
    });
    this.modif = true;
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

        //ajout idCentreGestion à l'objet contact
        data.idCentreGestion = this.centreGestion.id;
        this.contactService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Contact créé');
          this.contact = response;
          this.refreshContacts();
          this.choose(this.contact);
        });
      }
    }
  }

  search(): void {
    if (!this.searchForm.get('nom')?.value && !this.searchForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.staff = undefined;
    this.ldapService.searchUsersByName(this.searchForm.get('nom')?.value, this.searchForm.get('prenom')?.value).subscribe((response: any) => {
      this.staffs = response;
      if (this.staffs.length === 1) {
        this.chooseStaff(this.staffs[0]);
      }
    });
  }

}

