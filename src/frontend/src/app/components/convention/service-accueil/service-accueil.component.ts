import { Component, EventEmitter, OnInit, OnChanges, Input, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { ServiceService } from "../../../services/service.service";
import { CommuneService } from "../../../services/commune.service";
import { MessageService } from "../../../services/message.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { ConfigService } from "../../../services/config.service";

@Component({
  selector: 'app-service-accueil',
  templateUrl: './service-accueil.component.html',
  styleUrls: ['./service-accueil.component.scss']
})
export class ServiceAccueilComponent implements OnInit, OnChanges {

  countries: any[] = [];
  communes: any[] = [];

  data: any;

  @Input() centreGestion: any;

  @Input() etab: any;
  services:any[] = [];

  @Input() service: any;
  modif: boolean = false;
  form: FormGroup;

  autorisationModification = false;

  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;

  @Output() validated = new EventEmitter<number>();

  @Input() modifiable: boolean;

  constructor(public serviceService: ServiceService,
              public communeService: CommuneService,
              private fb: FormBuilder,
              private messageService: MessageService,
              private authService: AuthService,
              private paysService: PaysService,
              private configService: ConfigService,
  ) {
    this.form = this.fb.group({
      nom: [null, [Validators.required, Validators.maxLength(70)]],
      voie: [null, [Validators.required, Validators.maxLength(200)]],
      codePostal: [null, [Validators.required, Validators.maxLength(10), Validators.pattern('[0-9]+')]],
      batimentResidence: [null, [Validators.maxLength(200)]],
      commune: [null, [Validators.required, Validators.maxLength(200)]],
      idPays: [null, [Validators.required]],
      telephone: [null, [Validators.maxLength(20)]],
    });
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.autorisationModification = response.autoriserEtudiantAModifierEntreprise;
    });
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.countries = response.data;
    });
    this.communeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.communes = response;
    });
    this.toggleCommune();
    this.form.get('idPays')?.valueChanges.subscribe((idPays: any) => {
      this.toggleCommune();
      if(this.modif)
        this.clearCommune();
    });
  }

  ngOnChanges(): void{
    this.refreshServices();
  }

  refreshServices(): void{
    this.serviceService.getByStructure(this.etab.id, this.centreGestion.id).subscribe((response: any) => {
      this.services = response;
    });
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
    this.validated.emit(this.service);
  }

  initCreate(): void {
    this.service = {};
    this.form.reset();
    this.form.setValue({
      nom: null,
      voie: this.etab.voie,
      codePostal: this.etab.codePostal,
      batimentResidence: this.etab.batimentResidence,
      commune: this.etab.commune,
      idPays: this.etab.pays ? this.etab.pays.id : null,
      telephone: this.etab.telephone,
    });
    this.modif = true;
  }

  edit(): void {
    this.form.setValue({
      nom: this.service.nom,
      voie: this.service.voie,
      codePostal: this.service.codePostal,
      batimentResidence: this.service.batimentResidence,
      commune: this.service.commune,
      idPays: this.service.pays ? this.service.pays.id : null,
      telephone: this.service.telephone,
    });
    this.modif = true;
  }

  cancelEdit(): void {
    this.modif = false;
  }

  save(): void {
    if (this.form.valid) {

      // Contrôle code postal commune
      if (this.isFr() && !this.isCodePostalValid()) {
        this.messageService.setError('Code postal inconnu');
        return;
      }

      const data = {...this.form.getRawValue()};

      if (this.service.id) {
        this.serviceService.update(this.service.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Service modifié');
          this.service = response;
          this.refreshServices();
          this.modif = false;
        });
      } else {
        //ajoute idStructure à l'objet service
        data.idStructure = this.etab.id;

        // ajout idCentreGestion à l'objet service
        data.idCentreGestion = this.centreGestion.id;
        this.serviceService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Service créé');
          this.service = response;
          this.refreshServices();
          this.choose(this.service);
        });
      }
    }
  }

  clearCommune(): void {
      this.form.get('commune')?.setValue('');
      this.form.get('codePostal')?.setValue('');
      this.form.get('commune')?.markAsPristine();
      this.form.get('codePostal')?.markAsPristine();
  }
  toggleCommune(): void {
      if (!this.isPaysSet()) {
        this.form.get('commune')?.disable();
        this.form.get('codePostal')?.disable();
      }else{
        if (this.isFr()) {
          this.form.get('commune')?.disable();
          this.form.get('codePostal')?.enable();
        }else{
          this.form.get('commune')?.enable();
          this.form.get('codePostal')?.enable();
        }
      }
  }

  updateCommune(commune : any): void {
    this.form.get('commune')?.setValue(commune.split(' - ')[0]);
    this.form.get('codePostal')?.setValue(commune.split(' - ')[1]);
  }

  isPaysSet() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays)
      return true;
    return false;
  }

  isFr() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays) {
      let pays = this.countries.find(c => c.id === idPays);
      if (pays)
        return pays.libelle === 'FRANCE';
    }
    return true;
  }

  isCodePostalValid() {
    let codePostal = this.form.get('codePostal')?.value;
    if (codePostal) {
      let commune = this.communes.find(c => c.codePostal === codePostal);
      if (commune)
        return true;
    }
    return false;
  }
}

