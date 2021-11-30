import { Component, Output, EventEmitter, OnInit, Input, ViewChild  } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ServiceService } from "../../../../services/service.service";
import { ContactService } from "../../../../services/contact.service";
import { MessageService } from "../../../../services/message.service";
import { ModeVersGratificationService } from "../../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../../services/unite-gratification.service";
import { DeviseService } from "../../../../services/devise.service";
import { CiviliteService } from "../../../../services/civilite.service";
import { PaysService } from "../../../../services/pays.service";
import { AvenantService } from "../../../../services/avenant.service";
import { TableComponent } from "../../../table/table.component";
import { ServiceAccueilFormComponent } from '../../../gestion-etab-accueil/service-accueil-form/service-accueil-form.component';
import { ContactFormComponent } from '../../../gestion-etab-accueil/contact-form/contact-form.component';

@Component({
  selector: 'app-avenant-form',
  templateUrl: './avenant-form.component.html',
  styleUrls: ['./avenant-form.component.scss']
})
export class AvenantFormComponent implements OnInit {

  fieldValidators : any = {
      'dateRupture': [Validators.required],
  }

  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  devises: any[] = [];
  countries: any[] = [];
  civilites: any[] = [];

  service: any = 0;
  contact: any = 0;

  serviceTableColumns = ['choix','nom', 'voie', 'codePostal','batimentResidence', 'commune'];
  contactTableColumns = ['choix','centreGestionnaire', 'civilite', 'nom','prenom', 'telephone', 'mail', 'fax'];
  serviceSortColumn = 'nom';
  contactSortColumn = 'nom';
  serviceFilters: any[] = [];
  contactFilters: any[] = [];

  @Input() avenant: any;
  @Input() convention: any;

  form: FormGroup;

  autreModifChecked: boolean = false;

  @Output() validated = new EventEmitter<any>();
  @ViewChild(TableComponent) serviceAppTable: TableComponent | undefined;
  @ViewChild(TableComponent) contactAppTable: TableComponent | undefined;

  constructor(private avenantService: AvenantService,
              public serviceService: ServiceService,
              public contactService: ContactService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private deviseService: DeviseService,
              private civiliteService: CiviliteService,
              private paysService: PaysService,
              private fb: FormBuilder,
              private messageService: MessageService,
              public matDialog: MatDialog,
  ) {
  }

  ngOnInit(): void {
    this.modeVersGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.modeVersGratifications = response.data;
    });
    this.uniteDureeService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteDurees = response.data;
    });
    this.uniteGratificationService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.uniteGratifications = response.data;
    });
    this.deviseService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.devises = response.data;
    });
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
    this.civiliteService.getPaginated(1, 0, 'libelle', 'asc','').subscribe((response: any) => {
      this.civilites = response.data;
    });

    this.serviceFilters = [
        { id: 'structure.id', libelle: 'Structure', type: 'int',value:this.convention.structure.id, hidden : true},
    ];

    this.contactFilters = [
        { id: 'service.id', libelle: 'Service', type: 'int',value:this.convention.service.id, hidden : true},
    ];

    if (this.avenant.modificationLieu){
      this.service = this.avenant.service;
    }else{
      this.service = this.convention.service;
    }

    if (this.avenant.modificationTuteurPro){
      this.contact = this.avenant.contact;
    }else{
      this.contact = this.convention.contact;
    }
    console.log('this.avenant : ' + JSON.stringify(this.avenant, null, 2))
    if (this.avenant.id){
      this.form = this.fb.group({
        titreAvenant: [this.avenant.titreAvenant],
        rupture: [this.avenant.rupture],
        dateRupture: [this.avenant.dateRupture],
        commentaireRupture: [this.avenant.commentaireRupture],
        modificationPeriode: [this.avenant.modificationPeriode],
        dateDebutStage: [this.avenant.dateDebutStage],
        dateFinStage: [this.avenant.dateFinStage],
        interruptionStage: [this.avenant.interruptionStage],
        dateDebutInterruption: [this.avenant.dateDebutInterruption],
        dateFinInterruption: [this.avenant.dateFinInterruption],
        modificationLieu: [this.avenant.modificationLieu],
        modificationSujet: [this.avenant.modificationSujet],
        sujetStage: [this.avenant.sujetStage],
        modificationSalarie: [this.avenant.modificationSalarie],
        modificationEnseignant: [this.avenant.modificationEnseignant],
        modificationMontantGratification: [this.avenant.modificationMontantGratification],
        montantGratification: [this.avenant.montantGratification, [Validators.maxLength(7)]],
        idUniteGratification: [this.avenant.UniteGratification?this.avenant.UniteGratification.id:null],
        idUniteDuree: [this.avenant.uniteDuree?this.avenant.uniteDuree.id:null],
        idModeVersGratification: [this.avenant.modeVersGratification?this.avenant.modeVersGratification.id:null],
        idDevise: [this.avenant.devise?this.avenant.devise.id:null],
        validationAvenant: [this.avenant.validationAvenant],
        modificationAutre: [this.avenant.motifAvenant?true:false],
        motifAvenant: [this.avenant.motifAvenant],
      });
    }else{
      this.form = this.fb.group({
        titreAvenant: [null],
        rupture: [null],
        dateRupture: [null],
        commentaireRupture: [null],
        modificationPeriode: [null],
        dateDebutStage: [null],
        dateFinStage: [null],
        interruptionStage: [null],
        dateDebutInterruption: [null],
        dateFinInterruption: [null],
        modificationLieu: [null],
        modificationSujet: [null],
        sujetStage: [null],
        modificationSalarie: [false],
        modificationEnseignant: [null],
        modificationMontantGratification: [null],
        montantGratification: [null, [Validators.maxLength(7)]],
        idUniteGratification: [null],
        idUniteDuree: [null],
        idModeVersGratification: [null],
        idDevise: [null],
        validationAvenant: [null],
        modificationAutre: [null],
        motifAvenant: [null],
      });
    }

  }

  createOrEdit(): void {
    if (this.customFormValidation()) {

      const data = {...this.form.value};

      data.idConvention = this.convention.id

      if (this.form.get('modificationLieu')!.value){
        data.idService = this.service.id;
      }
      if (this.form.get('modificationSalarie')!.value){
        data.idContact = this.contact.id;
      }

      if (this.avenant.id){
        this.avenantService.update(this.avenant.id,data).subscribe((response: any) => {
          this.avenant = response;
          this.messageService.setSuccess('Avenant modifié avec succès');
          this.validated.emit();
        });
      }else{
        this.avenantService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Avenant créé avec succès');
          this.validated.emit();
        });
      }
    }
  }

  customFormValidation(): boolean {
    let valid = false;
    const checkboxFields = ['dateRupture', 'modificationPeriode', 'modificationLieu', 'modificationSujet', 'modificationSalarie',
       'modificationEnseignant', 'modificationMontantGratification', 'modificationAutre'];

    checkboxFields.forEach((field: string) => {
      if (this.form.get(field)!.value){
        valid = true;
      }
    });

    return valid && this.form.valid
  }

  cancel(): void {
  }

  selectService(row: any): void{
    this.service = row;
  }

  createService(): void {
    this.openServiceFormModal(null);
  }

  selectContact(row: any): void{
    this.contact = row;
  }

  createContact(): void {
    this.openContactFormModal(null);
  }
  openServiceFormModal(service: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {service: service, etab: this.convention.structure, countries: this.countries};
    const modalDialog = this.matDialog.open(ServiceAccueilFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.messageService.setSuccess("Service créé avec succès");
        this.service = dialogResponse;
        this.serviceAppTable!.update();
      }
    });
  }

  openContactFormModal(contact: any) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    dialogConfig.data = {contact: contact, service: this.convention.service, civilites: this.civilites};
    const modalDialog = this.matDialog.open(ContactFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.messageService.setSuccess("Contact créé avec succès");
        this.contact = dialogResponse;
        this.contactAppTable!.update();
      }
    });
  }
}
