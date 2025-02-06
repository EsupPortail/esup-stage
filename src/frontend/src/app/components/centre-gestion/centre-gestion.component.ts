import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { MessageService } from "../../services/message.service";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';
import { ConsigneService } from "../../services/consigne.service";
import {REGEX} from "../../utils/regex.utils";

@Component({
  selector: 'app-centre-gestion',
  templateUrl: './centre-gestion.component.html',
  styleUrls: ['./centre-gestion.component.scss']
})
export class CentreGestionComponent implements OnInit {

  statuts: any = {
    statutCoordCentre: 0,
    statutParamCentre: 0,
    statutPersoCentre: 0,
    statutRattachGest: 0,
    statutAlerteGest: 0,
  };

  tabs: any = {
    0: { statut: 0, init: true },
    1: { statut: 0, init: false },
    2: { statut: 0, init: false },
    3: { statut: 0, init: false },
    4: { statut: 0, init: false },
    5: { statut: 2, init: false },
    6: { statut: 0, init: false },
  }

  allValid = false;

  centreGestion: any;
  centreGestionInited = false;

  isCreate!: boolean;
  pathId: any;

  coordCentreForm!: FormGroup;
  paramCentreForm!: FormGroup;
  consigneCentre: any;
  signatureElectroniqueForm!: FormGroup;

  @ViewChild('matTabs') matTabs: MatTabGroup | undefined;

  constructor(private activatedRoute: ActivatedRoute, private centreGestionService: CentreGestionService, private messageService: MessageService, private fb: FormBuilder, private router: Router, private consigneService: ConsigneService) {
    this.setCoordCentreForm();
    this.setParamCentreForm();
    this.setSignatureElectroniqueForm();
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((param: any) => {
      this.pathId = param.id;
      if (this.pathId === 'create') {
        this.isCreate = true;
        this.centreGestionService.getBrouillonByLogin().subscribe((response: any) => {
          this.centreGestion = response;
          this.consigneCentre = this.centreGestion.consigne;
          this.centreGestionInited = true;
          if (this.centreGestion.id) {
            this.updateOnChanges();
          }
          this.majStatus();
        });
      } else {
        this.isCreate = false;
        this.centreGestionService.getById(this.pathId).subscribe((response: any) => {
          this.centreGestion = response;
          this.consigneCentre = this.centreGestion.consigne;
          this.centreGestionInited = true;
          if (this.centreGestion.id) {
            this.updateOnChanges();
          }
          this.majStatus();
        });
      }
    });
  }

  majStatus(): void {
    if (this.coordCentreForm.valid) {
      this.setStatus(0,2);
    } else {
      this.setStatus(0,0);
    }

    if (this.centreGestion.validationConvention == true || this.centreGestion.validationPedagogique == true) {
      if (this.invalidOrdresValidations())
        this.setStatus(1, 0);
      else
        this.setStatus(1, 2);
    } else {
      this.setStatus(1, 0);
    }

    if (this.centreGestion.fichier != null) {
      this.setStatus(2, 2);
    } else {
      this.setStatus(2, 1);
    }

    if (this.centreGestion.personnels && this.centreGestion.personnels.length > 0) {
      this.setStatus(3, 2);
    } else {
      this.setStatus(3, 0);
    }

    if (this.consigneCentre) {
      if (this.consigneCentre.texte) this.setStatus(4, 2);
      else this.setStatus(4, 1);
    } else {
      this.setStatus(4, 0);
    }

    if (this.signatureElectroniqueForm.valid) {
      this.setStatus(6,2);
    } else {
      this.setStatus(6,1);
    }
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
    this.majAllValid();
  }

  majAllValid(): void {
    this.allValid = true;
    for (let key in this.tabs) {
        if ((key == '0' || key == '1') && this.tabs[key].statut == 0) {
          this.allValid = false;
        }
    }
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }

  refreshCentreGestion(value: any): void {
    this.centreGestion = value;
    this.consigneService.getConsigneByCentre(this.centreGestion.id).subscribe((response: any) => {
      this.consigneCentre = response;
      this.majStatus();
    });
    this.updateOnChanges();
  }

  refreshPersonnelsCentre(): void {
    if (this.isCreate) {
      this.centreGestionService.getBrouillonByLogin().subscribe((response: any) => {
        this.centreGestion = response;
        this.majStatus();
      });
    } else {
      this.centreGestionService.getById(this.pathId).subscribe((response: any) => {
        this.centreGestion = response;
        this.majStatus();
      });
    }
  }

  update() {
    this.centreGestionService.update(this.centreGestion).subscribe((response: any) => {
      this.centreGestion = response;
      this.majStatus();
    });
  }

  updateOnChanges(): void {
    this.coordCentreForm.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      this.setCentreGestionCoordCentre();
      this.update();
    });
    this.paramCentreForm.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      this.setCentreGestionParamCentre();
      this.update();
    });
    this.signatureElectroniqueForm.valueChanges.pipe(debounceTime(1000)).subscribe(val => {
      this.setCentreGestionSignatureElectronique();
      this.update();
    });
  }

  validationCreation() {
    if (!this.allValid) {
      this.messageService.setError('Vous devez compléter les onglets "Coordonnées" et "Paramètres" avant de pouvoir valider la création du centre');
      return;
    }

    this.centreGestionService.validationCreation(this.centreGestion.id).subscribe((response: any) => {
      this.centreGestion = response;
      this.router.navigate([`/centre-gestion/search`], );
    });
  }

  delete() {
    this.centreGestionService.delete(this.centreGestion.id).subscribe((response: any) => {
      this.router.navigate([`/centre-gestion/search`], );
    });
  }

  setCoordCentreForm() {
    this.coordCentreForm = this.fb.group({
      nomCentre: [null, [Validators.required, Validators.maxLength(100)]],
      niveauCentre: [null, [Validators.required]],
      siteWeb: [null, [Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
      telephone: [null, [Validators.required, Validators.maxLength(20)]],
      fax: [null, [Validators.maxLength(20)]],
      adresse: [null, [Validators.maxLength(200)]],
      voie: [null, [Validators.required, Validators.maxLength(200)]],
      commune: [null, [Validators.required, Validators.maxLength(200)]],
      codePostal: [null, [Validators.required, Validators.maxLength(10)]],
    });
  }

  setCentreGestionCoordCentre() {
    this.centreGestion.nomCentre = this.coordCentreForm.get('nomCentre')?.value;
    this.centreGestion.niveauCentre = this.coordCentreForm.get('niveauCentre')?.value;
    this.centreGestion.siteWeb = this.coordCentreForm.get('siteWeb')?.value;
    this.centreGestion.mail = this.coordCentreForm.get('mail')?.value;
    this.centreGestion.telephone = this.coordCentreForm.get('telephone')?.value;
    this.centreGestion.fax = this.coordCentreForm.get('fax')?.value;
    this.centreGestion.adresse = this.coordCentreForm.get('adresse')?.value;
    this.centreGestion.voie = this.coordCentreForm.get('voie')?.value;
    this.centreGestion.commune = this.coordCentreForm.get('commune')?.value;
    this.centreGestion.codePostal = this.coordCentreForm.get('codePostal')?.value;
  }

  setParamCentreForm() {
    this.paramCentreForm = this.fb.group({
      codeConfidentialite: [null],
      saisieTuteurProParEtudiant: [null],
      autoriserImpressionConvention: [null],
      conditionValidationImpression: [null],
      autorisationEtudiantCreationConvention: [null],
      validationPedagogique: [null],
      verificationAdministrative: [null],
      validationConvention: [null],
      validationPedagogiqueOrdre: [null],
      verificationAdministrativeOrdre: [null],
      validationConventionOrdre: [null],
      recupInscriptionAnterieure: [null],
      dureeRecupInscriptionAnterieure: [1],
      urlPageInstruction: [null, [Validators.maxLength(200)]],
      nomViseur: [null, [Validators.maxLength(50)]],
      prenomViseur: [null, [Validators.maxLength(50)]],
      mailViseur: [null, [Validators.maxLength(255)]],
      qualiteViseur: [null, [Validators.maxLength(100)]],
      delaiAlerteConvention: [null, [Validators.required, Validators.min(0)]],
      onlyMailCentreGestion: [null],
    });
  }

  setCentreGestionParamCentre() {
    this.centreGestion.codeConfidentialite = this.paramCentreForm.get('codeConfidentialite')?.value;
    this.centreGestion.saisieTuteurProParEtudiant = this.paramCentreForm.get('saisieTuteurProParEtudiant')?.value;
    this.centreGestion.autoriserImpressionConvention = this.paramCentreForm.get('autoriserImpressionConvention')?.value;
    this.centreGestion.conditionValidationImpression = this.paramCentreForm.get('conditionValidationImpression')?.value;
    this.centreGestion.autorisationEtudiantCreationConvention = this.paramCentreForm.get('autorisationEtudiantCreationConvention')?.value;
    this.centreGestion.validationPedagogique = this.paramCentreForm.get('validationPedagogique')?.value;
    this.centreGestion.verificationAdministrative = this.paramCentreForm.get('verificationAdministrative')?.value;
    this.centreGestion.validationConvention = this.paramCentreForm.get('validationConvention')?.value;
    this.centreGestion.validationPedagogiqueOrdre = this.paramCentreForm.get('validationPedagogiqueOrdre')?.value;
    this.centreGestion.verificationAdministrativeOrdre = this.paramCentreForm.get('verificationAdministrativeOrdre')?.value;
    this.centreGestion.validationConventionOrdre = this.paramCentreForm.get('validationConventionOrdre')?.value;
    this.centreGestion.recupInscriptionAnterieure = this.paramCentreForm.get('recupInscriptionAnterieure')?.value;
    this.centreGestion.dureeRecupInscriptionAnterieure = this.paramCentreForm.get('dureeRecupInscriptionAnterieure')?.value ?? 1;
    this.centreGestion.urlPageInstruction = this.paramCentreForm.get('urlPageInstruction')?.value;
    this.centreGestion.nomViseur = this.paramCentreForm.get('nomViseur')?.value;
    this.centreGestion.prenomViseur = this.paramCentreForm.get('prenomViseur')?.value;
    this.centreGestion.mailViseur = this.paramCentreForm.get('mailViseur')?.value;
    this.centreGestion.qualiteViseur = this.paramCentreForm.get('qualiteViseur')?.value;
    this.centreGestion.delaiAlerteConvention = this.paramCentreForm.get('delaiAlerteConvention')?.value;
    this.centreGestion.onlyMailCentreGestion = this.paramCentreForm.get('onlyMailCentreGestion')?.value;
  }

  setSignatureElectroniqueForm() {
    this.signatureElectroniqueForm = this.fb.group({
      circuitSignature: [null, [Validators.maxLength(255)]],
      signataires: [null, []],
      envoiDocumentSigne: [null, []],
    });
  }

  setCentreGestionSignatureElectronique() {
    this.centreGestion.circuitSignature = this.signatureElectroniqueForm.get('circuitSignature')?.value;
    this.centreGestion.signataires = this.signatureElectroniqueForm.get('signataires')?.value;
    this.centreGestion.envoiDocumentSigne = this.signatureElectroniqueForm.get('envoiDocumentSigne')?.value;
  }

  invalidOrdresValidations() {
    // Les ordres de validations doivent être 1, 2 ou 3, et on ne peut pas avoir le même ordre
    let ordres = [1, 2, 3, null];
    return (!ordres.some(o => o === this.centreGestion.validationPedagogiqueOrdre)
          || !ordres.some(o => o === this.centreGestion.validationConventionOrdre)
          || !ordres.some(o => o === this.centreGestion.verificationAdministrativeOrdre)
    )
  }
}
