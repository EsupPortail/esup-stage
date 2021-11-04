import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from "@angular/router";
import { CentreGestionService } from "../../services/centre-gestion.service";
import { MessageService } from "../../services/message.service";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';

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
  }

  initedTabs = [0];
  centreGestion: any;
  centreGestionInited = false;

  coordCentreForm: FormGroup;
  paramCentreForm: FormGroup;

  @ViewChild('matTabs') matTabs: MatTabGroup | undefined;

  constructor(private activatedRoute: ActivatedRoute, private centreGestionService: CentreGestionService, private messageService: MessageService, private fb: FormBuilder) {
    this.setCoordCentreForm();
    this.setParamCentreForm();
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      if (pathId === 'create') {
        this.centreGestionService.getBrouillonByLogin().subscribe((response: any) => {
          this.centreGestion = response;
          this.centreGestionInited = true;
          if (this.centreGestion.id) {
            this.updateOnChanges();
          }
        });
      } else {
        // todo edit
      }
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
    if (this.initedTabs.indexOf(event.index) === -1) {
      this.initedTabs.push(event.index);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }

  refreshCentreGestion(value: any): void {
    this.centreGestion = value;
    this.updateOnChanges();
  }

  update() {
    this.centreGestionService.update(this.centreGestion).subscribe((response: any) => {
      this.centreGestion = response;
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
  }

  setCoordCentreForm() {
    this.coordCentreForm = this.fb.group({
      nomCentre: [null, [Validators.required, Validators.maxLength(100)]],
      niveauCentre: [null, [Validators.required]],
      siteWeb: [null, [Validators.maxLength(50)]],
      mail: [null, [Validators.required, Validators.email, Validators.maxLength(50)]],
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
      saisieTuteurProParEtudiant: [null],
      autorisationEtudiantCreationConvention: [null],
      validationPedagogique: [null],
      recupInscriptionAnterieure: [null],
      dureeRecupInscriptionAnterieure: [null],
      urlPageInstruction: [null, [Validators.maxLength(200)]],
      nomViseur: [null, [Validators.maxLength(50)]],
      prenomViseur: [null, [Validators.maxLength(50)]],
      qualiteViseur: [null, [Validators.maxLength(100)]],
    });
  }

  setCentreGestionParamCentre() {
    this.centreGestion.saisieTuteurProParEtudiant = this.paramCentreForm.get('saisieTuteurProParEtudiant')?.value;
    this.centreGestion.autorisationEtudiantCreationConvention = this.paramCentreForm.get('autorisationEtudiantCreationConvention')?.value;
    this.centreGestion.validationPedagogique = this.paramCentreForm.get('validationPedagogique')?.value;
    this.centreGestion.recupInscriptionAnterieure = this.paramCentreForm.get('recupInscriptionAnterieure')?.value;
    this.centreGestion.dureeRecupInscriptionAnterieure = this.paramCentreForm.get('dureeRecupInscriptionAnterieure')?.value;
    this.centreGestion.urlPageInstruction = this.paramCentreForm.get('urlPageInstruction')?.value;
    this.centreGestion.nomViseur = this.paramCentreForm.get('nomViseur')?.value;
    this.centreGestion.prenomViseur = this.paramCentreForm.get('prenomViseur')?.value;
    this.centreGestion.qualiteViseur = this.paramCentreForm.get('qualiteViseur')?.value;
  }

}
