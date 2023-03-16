import { Component, EventEmitter, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ServiceService } from "../../../services/service.service";
import { ContactService } from "../../../services/contact.service";
import { PaysService } from "../../../services/pays.service";
import { CiviliteService } from "../../../services/civilite.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { ConfigService } from "../../../services/config.service";
import { ServiceAccueilFormComponent } from '../../gestion-etab-accueil/service-accueil-form/service-accueil-form.component';

@Component({
  selector: 'app-signataire',
  templateUrl: './signataire.component.html',
  styleUrls: ['./signataire.component.scss']
})
export class SignataireComponent implements OnInit, OnChanges {

  civilites: any[] = [];
  countries: any[] = [];
  contacts:any[] = [];
  services: any[] = [];

  serviceTableColumns = ['nom', 'voie', 'codePostal','batimentResidence', 'commune', 'pays', 'telephone',  'actions'];

  @Input() convention: any;

  etab: any;
  contact: any;
  service: any;

  modif: boolean = false;
  form: FormGroup;

  autorisationModification = false;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  @Input() modifiable: boolean;

  constructor(private contactService: ContactService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private serviceService: ServiceService,
              private paysService: PaysService,
              private civiliteService: CiviliteService,
              private configService: ConfigService,
              private matDialog: MatDialog,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(50)]],
      prenom: [null, [Validators.required, Validators.maxLength(50)]],
      idCivilite: [null, []],
      fonction: [null, [Validators.required, Validators.maxLength(100)]],
      tel: [null, [Validators.required, Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.pattern('[^@ ]+@[^@. ]+\\.[^@ ]+'), Validators.maxLength(50)]],
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
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
  }

  ngOnChanges(): void {
    this.etab = this.convention.structure;
    this.contact = this.convention.signataire;
    this.service = this.contact ? this.contact.service : null;
    this.refreshContacts();
    this.refreshServices();
  }

  refreshServices(): void {
    this.serviceService.getByStructure(this.etab.id, this.convention.centreGestion.id).subscribe((response: any) => {
      this.services = response;
    });
  }

  refreshContacts(): void {
    if (this.service){
      this.contactService.getByService(this.service.id, this.convention.centreGestion.id).subscribe((response: any) => {
        this.contacts = response;
      });
    }
  }

  openServiceFormModal(service: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {service: service, etab: this.etab, countries: this.countries};
    const modalDialog = this.matDialog.open(ServiceAccueilFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        if (this.service) {
          this.messageService.setSuccess("Service modifié avec succès");
        }else{
          this.messageService.setSuccess("Service créé avec succès");
        }
        this.service = dialogResponse;
        this.refreshServices();
        this.refreshContacts();
      }
    });
  }

  selectService(): void{
    this.refreshContacts();
    this.contact = null;
  }

  createService(): void {
    if (this.canCreate()) {
      this.openServiceFormModal(null);
    }
  }

  editService(row: any): void {
    if (this.canEdit()) {
      this.openServiceFormModal(row);
    }
  }

  canCreate(): boolean {
    let hasRight =  this.modifiable && this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
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
          this.choose(this.contact);
        });
      }
    }
  }

}

