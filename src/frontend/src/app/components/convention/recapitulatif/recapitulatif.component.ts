import { Component, OnInit, Input } from '@angular/core';
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { ConventionService } from "../../../services/convention.service";
import { MessageService } from "../../../services/message.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import * as FileSaver from 'file-saver';
import { UserService } from '../../../services/user.service';

@Component({
  selector: 'app-recapitulatif',
  templateUrl: './recapitulatif.component.html',
  styleUrls: ['./recapitulatif.component.scss']
})
export class RecapitulatifComponent implements OnInit {

  @Input() convention: any;
  tmpConvention: any;
  interruptionsStage: any[] = [];
  canPrint: boolean = false;
  printDisabledReason: string = '';
  nomPrenomCreation: string = '';
  nomPrenomModification: string = '';

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private conventionService: ConventionService,
              private messageService: MessageService,
              private authService: AuthService,
              private router: Router,
              private userService: UserService) {
  }

  ngOnInit(): void {
    this.tmpConvention = {
      ...this.convention,
      langueConventionLibelle: this.getNomenclatureValue('langueConvention'),
      typeConventionLibelle: this.getNomenclatureValue('typeConvention'),
      themeLibelle: this.getNomenclatureValue('theme'),
      tempsTravailLibelle: this.getNomenclatureValue('tempsTravail'),
      uniteGratificationLibelle: this.getNomenclatureValue('uniteGratification'),
      uniteDureeGratificationLibelle: this.getNomenclatureValue('uniteDureeGratification'),
      deviseLibelle: this.getNomenclatureValue('devise'),
      modeVersGratificationLibelle: this.getNomenclatureValue('modeVersGratification'),
      origineStageLibelle: this.getNomenclatureValue('origineStage'),
      natureTravailLibelle: this.getNomenclatureValue('natureTravail'),
      modeValidationStageLibelle: this.getNomenclatureValue('modeValidationStage'),
    };

    if(this.tmpConvention.interruptionStage){
      this.loadInterruptionsStage();
    }

    this.updateCanPrintStatus();

    if (this.convention.loginCreation) {
      this.getNomPrenomEnvoiSignature(this.convention.loginCreation, 'creation');
    }

    if (this.convention.loginModif) {
      this.getNomPrenomEnvoiSignature(this.convention.loginModif, 'modif');
    }
  }

  updateCanPrintStatus(): void {
    // Vérifier d'abord si la convention a été validée
    if (!this.tmpConvention.validationCreation) {
      this.canPrint = false;
      this.printDisabledReason = 'Vous devez d\'abord valider la convention';
      return;
    }

    // Si l'utilisateur est un étudiant, appliquer les règles spécifiques
    if (this.authService.isEtudiant()) {
      const centreGestion: any = this.convention.centreGestion;

      // Vérifier si l'impression est autorisée pour ce centre de gestion
      if (!centreGestion.autoriserImpressionConvention) {
        this.canPrint = false;
        this.printDisabledReason = 'L\'impression n\'est pas autorisée pour ce centre de gestion';
        return;
      }

      // Appliquer les conditions de validation selon la configuration du centre
      switch (centreGestion.conditionValidationImpression) {
        case 0:
          // Aucune condition
          this.canPrint = true;
          break;
        case 1:
          // Validation pédagogique requise
          this.canPrint = !!this.convention.validationPedagogique;
          if (!this.canPrint) {
            this.printDisabledReason = 'La validation pédagogique est requise avant impression';
          }
          break;
        case 2:
          // Validation administrative requise
          this.canPrint = !!this.convention.validationConvention;
          if (!this.canPrint) {
            this.printDisabledReason = 'La validation administrative est requise avant impression';
          }
          break;
        case 3:
          // Validations pédagogique et administrative requises
          this.canPrint = !!this.convention.validationPedagogique && !!this.convention.validationConvention;
          if (!this.canPrint) {
            this.printDisabledReason = 'Les validations pédagogique et administrative sont requises avant impression';
          }
          break;
        case 4:
          // Vérification administrative requise
          this.canPrint = !!this.convention.verificationAdministrative;
          if (!this.canPrint) {
            this.printDisabledReason = 'La vérification administrative est requise avant impression';
          }
          break;
        default:
          this.canPrint = false;
          this.printDisabledReason = 'Configuration de validation non reconnue';
      }
    } else {
      // Pour les autres types d'utilisateurs (non-étudiants), l'impression est autorisée par défaut
      this.canPrint = true;
    }
  }

  getNomenclatureValue(key: string) {
    if (this.convention.validationCreation && this.convention.nomenclature) {
      return this.convention.nomenclature[key] ?? '';
    }
    return this.convention[key] ? this.convention[key].libelle : '';
  }

  loadInterruptionsStage() : void{
    this.periodeInterruptionStageService.getByConvention(this.tmpConvention.id).subscribe((response: any) => {
      this.interruptionsStage = response;
    });
  }

  validate(): void {
    this.conventionService.validationCreation(this.tmpConvention.id).subscribe((response: any) => {
      this.messageService.setSuccess('Convention créée avec succès');
      this.tmpConvention.validationCreation = true;
      this.updateCanPrintStatus();
      this.router.navigate([`/conventions/${this.tmpConvention.id}`]);
    });
  }

  printConvention(isRecap : boolean) : void {
    this.conventionService.getConventionPDF(this.tmpConvention.id, isRecap).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: "application/pdf"});
      let filename = 'Convention_' + this.tmpConvention.id + '_' + this.tmpConvention.etudiant.prenom + '_' + this.tmpConvention.etudiant.nom + '.pdf';
      FileSaver.saveAs(blob, filename);
    });
  }

  getNomPrenomEnvoiSignature(login: string, type: 'creation' | 'modif'): void {
    if (login == '(auto)'){
      if (type === 'creation') {
        this.nomPrenomCreation = 'Création automatique';
      } else if (type === 'modif') {
        this.nomPrenomModification = 'Modification automatique';
      }
      return;
    }
    this.userService.getPersonneByLogin(login).subscribe((response: any) => {
      if (response) {
        if (type === 'creation') {
          this.nomPrenomCreation = response.nom + ' ' + response.prenom;
        } else if (type === 'modif') {
          this.nomPrenomModification = response.nom + ' ' + response.prenom;
        }
      } else {
        if (type === 'creation') {
          this.nomPrenomCreation = login;
        } else if (type === 'modif') {
          this.nomPrenomModification = login;
        }
      }
    });
  }
}
