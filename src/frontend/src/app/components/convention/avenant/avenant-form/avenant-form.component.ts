import { Component, Output, EventEmitter, OnInit, Input, ViewChild  } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { FormBuilder, FormGroup, FormControl, Validators } from "@angular/forms";
import { ServiceService } from "../../../../services/service.service";
import { ContactService } from "../../../../services/contact.service";
import { MessageService } from "../../../../services/message.service";
import { AuthService } from "../../../../services/auth.service";
import { LdapService } from "../../../../services/ldap.service";
import { EnseignantService } from "../../../../services/enseignant.service";
import { ModeVersGratificationService } from "../../../../services/mode-vers-gratification.service";
import { UniteDureeService } from "../../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../../services/unite-gratification.service";
import { DeviseService } from "../../../../services/devise.service";
import { CiviliteService } from "../../../../services/civilite.service";
import { PaysService } from "../../../../services/pays.service";
import { AvenantService } from "../../../../services/avenant.service";
import { ContenuService } from "../../../../services/contenu.service";
import { PeriodeInterruptionStageService } from "../../../../services/periode-interruption-stage.service";
import { PeriodeInterruptionAvenantService } from "../../../../services/periode-interruption-avenant.service";
import { TableComponent } from "../../../table/table.component";
import { ServiceAccueilFormComponent } from '../../../gestion-etab-accueil/service-accueil-form/service-accueil-form.component';
import { ContactFormComponent } from '../../../gestion-etab-accueil/contact-form/contact-form.component';
import { InterruptionsFormComponent } from '../../../convention/stage/interruptions-form/interruptions-form.component';
import { debounceTime } from "rxjs/operators";

@Component({
  selector: 'app-avenant-form',
  templateUrl: './avenant-form.component.html',
  styleUrls: ['./avenant-form.component.scss']
})
export class AvenantFormComponent implements OnInit {

  fieldValidators : any = {
      'dateRupture': [Validators.required],
  }

  checkboxFields : any  = ['dateRupture', 'modificationPeriode', 'modificationLieu', 'modificationSujet', 'modificationSalarie',
       'modificationEnseignant', 'modificationMontantGratification', 'modificationAutre'];

  periodeStageFields : any = ['dateDebutStage', 'dateFinStage'];
  montantGratificationFields : any = ['montantGratification', 'idUniteGratification','idUniteDuree', 'idModeVersGratification','idDevise'];

  modeVersGratifications: any[] = [];
  uniteDurees: any[] = [];
  uniteGratifications: any[] = [];
  devises: any[] = [];
  countries: any[] = [];
  civilites: any[] = [];
  interruptionsStage: any[] = [];
  addedInterruptionsStage: any[] = [];
  modifiedInterruptionsStage: any[] = [];

  enseignants: any[] = [];
  enseignant: any = 0;
  service: any = 0;
  contact: any = 0;

  serviceTableColumns = ['choix','nom', 'voie', 'codePostal','batimentResidence', 'commune'];
  contactTableColumns = ['choix','centreGestionnaire', 'civilite', 'nom','prenom', 'telephone', 'mail', 'fax'];
  enseignantColumns = ['nomprenom', 'mail', 'departement', 'action'];
  serviceSortColumn = 'nom';
  contactSortColumn = 'nom';
  serviceFilters: any[] = [];
  contactFilters: any[] = [];

  @Input() avenant: any;
  @Input() convention: any;

  form: FormGroup;
  enseignantSearchForm: FormGroup;

  texteLimiteRenumeration: string = '';

  minDateDebutStage: Date;
  maxDateDebutStage: Date;
  minDateFinStage: Date;
  maxDateFinStage: Date;

  customValidForm: boolean = false;
  autreModifChecked: boolean = false;

  @Output() updated = new EventEmitter<any>();
  @ViewChild(TableComponent) serviceAppTable: TableComponent | undefined;
  @ViewChild(TableComponent) contactAppTable: TableComponent | undefined;

  constructor(private avenantService: AvenantService,
              public serviceService: ServiceService,
              public contactService: ContactService,
              private modeVersGratificationService: ModeVersGratificationService,
              private uniteDureeService: UniteDureeService,
              private uniteGratificationService: UniteGratificationService,
              private deviseService: DeviseService,
              private enseignantService: EnseignantService,
              private ldapService: LdapService,
              private civiliteService: CiviliteService,
              private paysService: PaysService,
              private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private periodeInterruptionAvenantService: PeriodeInterruptionAvenantService,
              private authService: AuthService,
              private contenuService: ContenuService,
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

    if(this.convention.interruptionStage){
      this.loadInterruptionsStage();
    }

    if (this.avenant.modificationLieu){
      this.service = this.avenant.service;
    }else{
      this.service = this.convention.service;
    }

    if (this.avenant.modificationSalarie){
      this.contact = this.avenant.contact;
    }else{
      this.contact = this.convention.contact;
    }

    if (this.avenant.modificationEnseignant){
      this.enseignant = this.avenant.enseignant;
    }

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
        idUniteGratification: [this.avenant.uniteGratification?this.avenant.uniteGratification.id:null],
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

    this.enseignantSearchForm = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });

    this.form.valueChanges.subscribe(() => {
      this.customFormValidation();
    });
    this.enseignantSearchForm.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.search();
    });

    //controles uniquement pour les non gestionnaires
    if(!this.isGestionnaire()){
      this.minDateDebutStage = new Date(new Date().getTime() + (1000 * 60 * 60 * 24));
      this.maxDateDebutStage = new Date(new Date().getFullYear()+1, 7, 31);
      this.minDateFinStage = new Date(new Date().getTime() + (1000 * 60 * 60 * 24));
      this.maxDateFinStage = new Date(new Date().getFullYear()+2, 0, 1);
    }

    this.contenuService.get('TEXTE_LIMITE_RENUMERATION').subscribe((response: any) => {
      this.texteLimiteRenumeration = response.texte;
    })
  }

  createOrEdit(): void {
    if (this.customValidForm) {

      const data = {...this.form.value};

      data.idConvention = this.convention.id

      if (this.form.get('modificationLieu')!.value){
        data.idService = this.service.id;
      }
      if (this.form.get('modificationSalarie')!.value){
        data.idContact = this.contact.id;
      }
      if (this.form.get('modificationEnseignant')!.value){
        data.idEnseignant = this.enseignant.id;
      }

      if (this.form.get('modificationPeriode')!.value){

        this.modifiedInterruptionsStage = [];
        for(const interruption of this.interruptionsStage){
          if (this.form.get(interruption.dateDebutInterruptionFormControlName)!.value ||
              this.form.get(interruption.dateFinInterruptionFormControlName)!.value){
            const modifiedInterruption = {
              "dateDebutInterruption":this.form.get(interruption.dateDebutInterruptionFormControlName)!.value,
              "dateFinInterruption":this.form.get(interruption.dateFinInterruptionFormControlName)!.value,
              "isModif":true,
              "idPeriodeInterruptionStage":interruption.id,
            };
            this.modifiedInterruptionsStage.push(modifiedInterruption);
          }
        }
        for (let addedInterruption of this.addedInterruptionsStage){
          addedInterruption.isModif = false;
        }
      }
      if (this.avenant.id){
        this.avenantService.update(this.avenant.id,data).subscribe((response: any) => {
          this.avenant = response;
          this.messageService.setSuccess('Avenant modifié avec succès');
          if (this.form.get('modificationPeriode')!.value){
            this.clearAndAddInterruptionsAvenant(response.id)
          }else{
            this.updated.emit();
          }
        });
      }else{
        this.avenantService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Avenant créé avec succès');
          if (this.form.get('modificationPeriode')!.value){
            this.clearAndAddInterruptionsAvenant(response.id)
          }else{
            this.updated.emit();
          }
        });
      }
    }
  }

  customFormValidation(): void {
    let valid = false;

    this.checkboxFields.forEach((field: string) => {
      if (this.form.get(field)!.value){
        valid = true;
      }
    });

    if (this.form.get('modificationPeriode')!.value){
      let periodeStageValid = false;
      this.periodeStageFields.forEach((field: string) => {
        if (this.form.get(field)!.value){
          periodeStageValid = true;
        }
      });
      valid = valid && (periodeStageValid || this.addedInterruptionsStage.length > 0);
    }

    if (this.form.get('modificationMontantGratification')!.value){
      let montantGratificationValid = false;
      this.montantGratificationFields.forEach((field: string) => {
        if (this.form.get(field)!.value){
          montantGratificationValid = true;
        }
      });
      valid = valid && montantGratificationValid;
    }
    if (this.form.get('modificationLieu')!.value){
      if(this.service.id === this.convention.service.id){
        valid = false;
      }
    }
    if (this.form.get('modificationSalarie')!.value){
      if(this.contact.id === this.convention.contact.id){
        valid = false;
      }
    }
    if (this.form.get('modificationEnseignant')!.value){
      if(!this.enseignant || this.enseignant.id === this.convention.enseignant.id){
        valid = false;
      }
    }
    this.customValidForm = valid && this.form.valid;
  }

  cancel(): void {
    this.form.reset();
  }

  delete(): void {
    this.avenantService.delete(this.avenant.id).subscribe((response: any) => {
      this.avenant = response;
      this.messageService.setSuccess('Avenant supprimé avec succès');
      this.updated.emit();
    });

  }

  validate(): void {
    this.avenantService.validate(this.avenant.id).subscribe((response: any) => {
      this.avenant = response;
      this.messageService.setSuccess('L\'avenant a été validé avec succès');
      this.updated.emit();
    });
  }

  selectService(row: any): void{
    this.service = row;
    this.customFormValidation();
  }

  createService(): void {
    this.openServiceFormModal(null);
  }

  selectContact(row: any): void{
    this.contact = row;
    this.customFormValidation();
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
        this.selectService(dialogResponse)
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
        this.selectContact(dialogResponse)
        this.contactAppTable!.update();
      }
    });
  }

  search(): void {
    if (!this.enseignantSearchForm.get('nom')?.value && !this.enseignantSearchForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.enseignant = undefined;
    this.ldapService.searchEnseignants(this.enseignantSearchForm.value).subscribe((response: any) => {
      this.enseignants = response;
    });
  }

  chooseEnseignant(row: any): void {
      this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
        this.enseignant = response;
        this.customFormValidation();
        if (this.enseignant == null){
          this.createEnseignant(row);
        }
      });
  }

  createEnseignant(row: any): void {
    const displayName = row.displayName.split(/(\s+)/);
    const data = {
      "nom": displayName[2],
      "prenom": displayName[0],
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.supannAliasLogin,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      this.enseignant = response;
    });
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  isGestionnaire(): boolean {
    return this.authService.isGestionnaire() || this.authService.isAdmin();
  }

  dateDebutChanged(event: any): void {
    this.updateDateFinBounds(event.value);
  }

  updateDateFinBounds(dateDebut: Date): void {
    this.minDateFinStage = new Date(dateDebut.getTime() + (1000 * 60 * 60 * 24));
    this.maxDateFinStage = new Date(dateDebut.getTime() + (1000 * 60 * 60 * 24 * 365));
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.convention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
      for(let interruption of this.interruptionsStage){
        const dateDebutInterruptionFormControlName = 'dateDebutInterruption' + interruption.id
        const dateFinInterruptionFormControlName = 'dateFinInterruption' + interruption.id
        this.form.addControl(dateDebutInterruptionFormControlName,new FormControl(null));
        this.form.addControl(dateFinInterruptionFormControlName,new FormControl(null));
        this.periodeStageFields.push(dateDebutInterruptionFormControlName);
        this.periodeStageFields.push(dateFinInterruptionFormControlName);
        interruption.dateDebutInterruptionFormControlName = dateDebutInterruptionFormControlName;
        interruption.dateFinInterruptionFormControlName = dateFinInterruptionFormControlName;
      }
      this.loadInterruptionsAvenant();
    });
  }

  loadInterruptionsAvenant() : void {
    if(this.avenant.id){
      this.periodeInterruptionAvenantService.getByAvenant(this.avenant.id).subscribe((response: any) => {
        for(let interruption of response){
          if (interruption.isModif){
            this.modifiedInterruptionsStage.push(interruption);
          }else{
            this.addedInterruptionsStage.push(interruption);
          }
        }
        for(let interruption of this.modifiedInterruptionsStage){
          const dateDebutInterruptionFormControlName = 'dateDebutInterruption' + interruption.periodeInterruptionStage.id
          const dateFinInterruptionFormControlName = 'dateFinInterruption' + interruption.periodeInterruptionStage.id
          this.form.get(dateDebutInterruptionFormControlName)!.setValue(interruption.dateDebutInterruption);
          this.form.get(dateFinInterruptionFormControlName)!.setValue(interruption.dateFinInterruption);
        }
      });
    }
  }

  addInterruptionsAvenant(avenantId: number){
    let finished = 0;
    for (let addedInterruption of this.addedInterruptionsStage){
      addedInterruption.idAvenant = avenantId;
      this.periodeInterruptionAvenantService.create(addedInterruption).subscribe((response: any) => {
        finished++;
        if (finished === (this.addedInterruptionsStage.length + this.modifiedInterruptionsStage.length)){
          this.updated.emit();
        }
      });
    }
    for (let modifiedInterruption of this.modifiedInterruptionsStage){
      modifiedInterruption.idAvenant = avenantId;
      this.periodeInterruptionAvenantService.create(modifiedInterruption).subscribe((response: any) => {
        finished++;
        if (finished === (this.addedInterruptionsStage.length + this.modifiedInterruptionsStage.length)){
          this.updated.emit();
        }
      });
    }
  }

  clearAndAddInterruptionsAvenant(avenantId: number) : void{
    this.periodeInterruptionAvenantService.deleteAll(avenantId).subscribe((response: any) => {
      this.addInterruptionsAvenant(avenantId);
    });
  }

  openInterruptionsCreateFormModal(): void {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '1000px';
    const convention = {
      'id':this.convention.id,
      'dateDebutStage':this.form.get('dateDebutStage')!.value?this.form.get('dateDebutStage')!.value:this.convention.dateDebutStage,
      'dateFinStage':this.form.get('dateFinStage')!.value?this.form.get('dateFinStage')!.value:this.convention.dateFinStage
    }
    dialogConfig.data = {convention: convention,interruptionsStage: [],interruptionStage: null,periodes:this.addedInterruptionsStage};
    const modalDialog = this.matDialog.open(InterruptionsFormComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse) {
        this.addedInterruptionsStage = dialogResponse;
      }
    });
  }
}
