import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { ConventionService } from "../../services/convention.service";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { TitleService } from "../../services/title.service";
import { AuthService } from "../../services/auth.service";
import { ConfigService } from "../../services/config.service";
import { AppFonction } from '../../constants/app-fonction';
import { Droit } from '../../constants/droit';
import { getProgressText } from '../../utils/text-progress-bar.utils';

@Component({
    selector: 'app-convention',
    templateUrl: './convention.component.html',
    styleUrls: ['./convention.component.scss'],
    standalone: false
})
export class ConventionComponent implements OnInit {

  convention: any;
  back = 'tableau-de-bord';

  conventionTabIndex: number = 0;

  tabs: any = {
    0: { statut: 0, init: true },
    1: { statut: 0, init: false },
    2: { statut: 0, init: false },
    3: { statut: 0, init: false },
    4: { statut: 0, init: true },
    5: { statut: 0, init: false },
    6: { statut: 0, init: false },
    7: { statut: 0, init: false },
    8: { statut: 0, init: false },
  }

  goToOnglet = 0;
  allValid = false;
  modifiable = true;
  signatureEnabled = false;
  protected readonly getProgressText = getProgressText;

  @ViewChild("tabGroup") tabGroup!: MatTabGroup;

  constructor(
    private activatedRoute: ActivatedRoute,
    private conventionService: ConventionService,
    private titleService: TitleService,
    private authService: AuthService,
    private router: Router,
    private configService: ConfigService,
  ) {
    this.router.routeReuseStrategy.shouldReuseRoute = () => false;
  }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.signatureEnabled = response.signatureEnabled;
    });
    this.activatedRoute.queryParams.subscribe((param: any) => {
      if (param.back) this.back = param.back;
      if (param.goToOnglet) this.goToOnglet = param.goToOnglet;
    });
    this.activatedRoute.params.subscribe((param: any) => {
      const pathId = param.id;
      this.conventionTabIndex = this.goToOnglet;
      if (pathId === 'create') {
        // Récupération de la convention au mode brouillon
        this.conventionService.getBrouillon().subscribe((response: any) => {
          this.convention = response;
          this.titleService.title = 'Création d\'une convention';
          this.majStatus();
        });
        this.modifiable = true;
      } else {
        // Récupération de la convention correspondant à l'id
        this.conventionService.getById(pathId).subscribe((response: any) => {
          this.convention = response;
          this.titleService.title = 'Gestion de la convention n°' + pathId
          + ' ' + this.convention.etudiant.nom + ' ' + this.convention.etudiant.prenom;
          this.majStatus();
          // un admin a tout le temps les droits de modifications
          if (this.authService.isAdmin()) {
            this.modifiable = true;
          } else {
            const canModifConvention = !this.convention.validationConvention && this.authService.checkRights({ fonction: AppFonction.CONVENTION, droits: [Droit.MODIFICATION] });
            if (this.authService.isGestionnaire()) {
              this.modifiable = !this.convention.validationConvention;
            }
            // Récupération du droit en fonction du paramétrage du rôle
            else if (this.authService.isEnseignant()) {
              this.modifiable = canModifConvention;
            }
            // Une convention n'est plus modifiable par l'étudiant dès qu'il y a eu au moins une validation
            else if (this.authService.isEtudiant()) {
              let validee = false;
              if (this.convention.centreGestion) {
                if (this.convention.centreGestion.verificationAdministrative && this.convention.verificationAdministrative) validee = true;
                if (this.convention.centreGestion.validationPedagogique && this.convention.validationPedagogique) validee = true;
                if (this.convention.centreGestion.validationConvention && this.convention.validationConvention) validee = true;
              }
              this.modifiable = !validee;
            }
            // Récupération du droit en fonction du paramétrage du rôle
            else {
              this.modifiable = canModifConvention;
            }
          }
        });
      }
    });
  }

  majStatus(): void {
    if (this.convention.etudiant && this.convention.regionCPAM  && this.convention.libelleCPAM && this.convention.adresseCPAM){
      this.setStatus(0,2);
    }else{
      this.setStatus(0,0);
    }
    if (this.convention.structure){
      this.setStatus(1,2);
    }else{
      this.setStatus(1,0);
    }
    if (this.convention.service){
      this.setStatus(2,2);
    }else{
      this.setStatus(2,0);
    }
    if (this.convention.contact){
      this.setStatus(3,2);
    }else{
      this.setStatus(3,0);
    }
    if (this.convention.enseignant){
      this.setStatus(5,2);
    }else{
      this.setStatus(5,0);
    }
    if (this.convention.signataire){
      this.setStatus(6,2);
    }else{
      this.setStatus(6,0);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
    this.majAllValid();
  }

  majAllValid(): void {
    this.allValid = true;
    for (let key in this.tabs) {
        if (key != '7' && key != '8' && this.tabs[key].statut !== 2) {
          this.allValid = false;
        }
    }
  }

  isCreated(): boolean {
    return this.convention.id && this.convention.id !== 0;
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }

  updateConvention(data: any): void {
    this.convention = data;
    this.setStatus(0,2);
  }

  updateEtab(data: any): void {
    this.updateSingleField('idStructure',data.id);
  }

  updateService(data: any): void {
    this.updateSingleField('idService',data.id);
  }

  updateTuteurPro(data: any): void {
    this.updateSingleField('idContact',data.id);
  }

  updateStage(data: any): void {
    this.updateSingleField(data.field,data.value);
  }

  updateEnseignant(data: any): void {
    this.conventionService.changeEnseignantReferent(this.convention.id, data.id).subscribe((response: any) => {
      this.convention = response;
      this.majStatus();
    });
  }

  updateSignataire(data: any): void {
    this.updateSingleField('idSignataire',data.id);
  }


  updateSingleField(key: string, value: any): void {
    const data = {
      "field":key,
      "value":value,
    };
    this.conventionService.patch(this.convention.id, data).subscribe((response: any) => {
      this.convention = response;
      this.majStatus();
    });
  }

  isConventionValide(): boolean {
    const c = this.convention;

    const cg = c?.centreGestion;
    if (!cg) return false;
    if (!cg.validationPedagogique && !cg.validationConvention && !cg.verificationAdministrative) return false;

    return (!cg.validationPedagogique || c.validationPedagogique)
      && (!cg.validationConvention || c.validationConvention)
      && (!cg.verificationAdministrative || c.verificationAdministrative);
  }

  isEtudiant(): boolean {
    return this.authService.isEtudiant();
  }

  conventionValidated(convention: any): any {
    this.convention = convention;
    if (this.tabGroup) {
      if (this.isConventionValide()) {
        this.tabGroup.selectedIndex = 1;
      } else {
        this.tabGroup.selectedIndex = 8;
      }
    }
  }

  conventionUpdated(convention: any): any {
    this.convention = convention;
  }
}
