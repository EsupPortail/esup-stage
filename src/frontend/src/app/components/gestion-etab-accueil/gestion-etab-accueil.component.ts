import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { StructureService } from "../../services/structure.service";
import { ServiceService } from "../../services/service.service";
import { ContactService } from "../../services/contact.service";
import { PaysService } from "../../services/pays.service";
import { CiviliteService } from "../../services/civilite.service";
import { TypeStructureService } from "../../services/type-structure.service";
import { NafN1Service } from "../../services/naf-n1.service";
import { NafN5Service } from "../../services/naf-n5.service";
import { StatutJuridiqueService } from "../../services/statut-juridique.service";
import { MessageService } from "../../services/message.service";
import { AuthService } from "../../services/auth.service";
import { TableComponent } from "../table/table.component";
import { AppFonction } from "../../constants/app-fonction";
import { Droit } from "../../constants/droit";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { ServiceAccueilFormComponent } from './service-accueil-form/service-accueil-form.component';
import { ContactFormComponent } from './contact-form/contact-form.component';
import { Validators } from '@angular/forms';

@Component({
  selector: 'app-gestion-etab-accueil',
  templateUrl: './gestion-etab-accueil.component.html',
  styleUrls: ['./gestion-etab-accueil.component.scss']
})
export class GestionEtabAccueilComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5.nafN1.libelle', 'pays.lib', 'commune', 'typeStructure.libelle', 'statutJuridique.libelle', 'action'];
  sortColumn = 'raisonSociale';
  serviceTableColumns = ['nom', 'voie', 'codePostal','batimentResidence', 'commune', 'pays', 'telephone',  'actions'];
  contactTableColumns = ['centreGestionnaire', 'civilite', 'nom','prenom', 'telephone', 'mail', 'fax',  'actions'];
  filters: any[] = [];
  countries: any[] = [];
  civilites: any[] = [];
  exportColumns = {
    raisonSociale: { title: 'Raison sociale' },
    numeroSiret: { title: 'SIRET' },
    nafN5: { title: 'Activité' },
    pays: { title: 'Pays' },
    commune: { title: 'Commune' },
    typeStructure: { title: 'Type d\'organisme' },
    statutJuridique: { title: 'Forme juridique' },
  };

  formTabIndex = 1;
  data: any = {};

  services:any[] = [];
  service: any;

  contacts:any[] = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public structureService: StructureService,
    private serviceService: ServiceService,
    private contactService: ContactService,
    private paysService: PaysService,
    private civiliteService: CiviliteService,
    private typeStructureService: TypeStructureService,
    private nafN1Service: NafN1Service,
    private nafN5Service: NafN5Service,
    private statutJuridiqueService: StatutJuridiqueService,
    private messageService: MessageService,
    private authService: AuthService,
    public matDialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.filters = [
      { id: 'raisonSociale', libelle: 'Raison sociale' },
      { id: 'numeroSiret', libelle: 'Numéro SIRET' },
      { id: 'numeroRNE', libelle: 'Numéro UAI', options:[Validators.maxLength(7)] },
      { id: 'nafN1.code', libelle: 'Activité', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'code', specific: true },
      { id: 'pays.id', libelle: 'Pays', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'commune', libelle: 'Commune' },
      { id: 'typeStructure.id', libelle: 'Type d\'organisme', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'statutJuridique.id', libelle: 'Forme juridique', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
    ];
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
      const filter = this.filters.find((f: any) => f.id === 'pays.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'typeStructure.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.nafN1Service.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'nafN1.code');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'statutJuridique.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.civiliteService.getPaginated(1, 0, 'libelle', 'asc','').subscribe((response: any) => {
      this.civilites = response.data;
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.formTabIndex) {
      this.data = {}
    }
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    const hasRight = this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.MODIFICATION]});
    return !this.authService.isEtudiant() && hasRight;
  }

  edit(row: any): void {
    if (this.tabs) {
      this.tabs.selectedIndex = this.formTabIndex;
    }
    this.structureService.getById(row.id).subscribe((response: any) => {
      this.data = response;
      this.refreshServices();
    });
  }

  refreshServices(): void{
    if (this.data){
      this.serviceService.getByStructure(this.data.id, -1).subscribe((response: any) => {
        this.services = response;
      });
    }
  }

  selectService(): void{
    this.refreshContacts();
  }

  createService(): void {
    this.openServiceFormModal(null);
  }

  editService(row: any): void {
    this.openServiceFormModal(row);
  }

  deleteService(row: any): void {
    this.serviceService.delete(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Service supprimé');
      this.service = null;
      this.refreshServices();
    });
  }

  refreshContacts(): void{
    if (this.service){
      this.contactService.getByService(this.service.id, -1).subscribe((response: any) => {
        this.contacts = response;
      });
    }
  }

  createContact(): void {
    this.openContactFormModal(null);
  }

  editContact(row: any): void {
    this.openContactFormModal(row);
  }

  deleteContact(row: any): void {
    this.contactService.delete(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Contact supprimé');
      this.refreshContacts();
    });
  }

  openServiceFormModal(service: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {service: service, etab: this.data, countries: this.countries};
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

  openContactFormModal(contact: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {contact: contact, service: this.service, civilites: this.civilites};
    const modalDialog = this.matDialog.open(ContactFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        if (this.service) {
          this.messageService.setSuccess("Contact modifié avec succès");
        }else{
          this.messageService.setSuccess("Contact créé avec succès");
        }
        this.refreshContacts();
      }
    });
  }

  returnToList(): void {
    if (this.tabs) {
      this.tabs.selectedIndex = 0;
    }
  }

  importCsv(event: any): void {
    this.structureService.import(event.target.files[0]).subscribe((response: any) => {
      this.messageService.setSuccess('Etablissements d\'enseignement importés avec succès');
    });
  }

}
