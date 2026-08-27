import { ChangeDetectorRef, Component, OnInit, Input } from '@angular/core';
import { PeriodeInterruptionStageService } from "../../../services/periode-interruption-stage.service";
import { PeriodeStageService } from "../../../services/periode-stage.service"
import { ConventionService } from "../../../services/convention.service";
import { MessageService } from "../../../services/message.service";
import { AuthService } from "../../../services/auth.service";
import { Router } from "@angular/router";
import * as FileSaver from 'file-saver';
import { UserService } from '../../../services/user.service';
import {AvenantService} from "../../../services/avenant.service";

@Component({
    selector: 'app-recapitulatif',
    templateUrl: './recapitulatif.component.html',
    styleUrls: ['./recapitulatif.component.scss'],
    standalone: false
})
export class RecapitulatifComponent implements OnInit {

  @Input() convention: any;
  tmpConvention: any;
  interruptionsStage: any[] = [];
  periodesStage: any[] = [];
  canPrint: boolean = false;
  printDisabledReason: string = '';
  nomPrenomCreation: string = '';
  nomPrenomModification: string = '';
  avenants: any[] = [];
  loadingAvenants = true;
  canEditAccordAnnuaire: boolean = false;

  constructor(private periodeInterruptionStageService: PeriodeInterruptionStageService,
              private periodeStageService: PeriodeStageService,
              private conventionService: ConventionService,
              private messageService: MessageService,
              private authService: AuthService,
              private router: Router,
              private userService: UserService,
              private avenantService: AvenantService,
              private changeDetectorRef: ChangeDetectorRef) {
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

    // Le consentement annuaire reste modifiable après validation de la convention :
    // l'étudiant doit pouvoir retirer son accord à tout moment.
    this.canEditAccordAnnuaire = this.authService.isEtudiant() || this.authService.isGestionnaire() || this.authService.isAdmin();

    if(this.tmpConvention.interruptionStage){
      this.loadInterruptionsStage();
    }

    if(!this.tmpConvention.horairesReguliers){
      this.loadPeriodesStage()
    }

    this.updateCanPrintStatus();

    if (this.convention.loginCreation) {
      this.getNomPrenomEnvoiSignature(this.convention.loginCreation, 'creation');
    }

    if (this.convention.loginModif) {
      this.getNomPrenomEnvoiSignature(this.convention.loginModif, 'modif');
    }
    this.loadAvenants();
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

      if (this.hasValidatedAvenant()) {
        this.canPrint = false;
        this.printDisabledReason =
          'L’impression n\'est pas disponible car un avenant a été validé. '
        return;
      }

      if (this.avenants.length > 0 && !centreGestion.autoriserImpressionConventionApresCreationAvenant) {
        this.canPrint = false;
        this.printDisabledReason =
          'L’impression n’est pas autorisée par votre centre après la création d’un avenant.';
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

  loadPeriodesStage() : void{
    this.periodeStageService.getByConvention(this.tmpConvention.id).subscribe((response : any) => {
      this.periodesStage = response;
    })
  }

  setAccordAnnuaire(value: boolean, checked: boolean): void {
    if (!checked) {
      // Une des deux cases doit rester cochée. Le binding [checked] ne changeant pas de
      // valeur, on force un cycle à null pour que la case décochée soit bien re-cochée.
      const courant = this.tmpConvention.accordAnnuaireEtudiant;
      this.tmpConvention.accordAnnuaireEtudiant = null;
      this.changeDetectorRef.detectChanges();
      this.tmpConvention.accordAnnuaireEtudiant = courant;
      return;
    }
    const accord = value;
    this.conventionService.updateAccordAnnuaire(this.tmpConvention.id, accord).subscribe((response: any) => {
      this.tmpConvention.accordAnnuaireEtudiant = response.accordAnnuaireEtudiant;
      this.convention.accordAnnuaireEtudiant = response.accordAnnuaireEtudiant;
      this.messageService.setSuccess('Votre choix a bien été enregistré');
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

  private hasValidatedAvenant(): boolean {
    return Array.isArray(this.avenants) && this.avenants.some(a => a.validationAvenant);
  }

  private loadAvenants(): void {
    this.loadingAvenants = true;
    this.avenantService.getByConvention(this.tmpConvention.id).subscribe({
      next: (res: any) => {
        this.avenants = Array.isArray(res) ? res : (res?.content ?? []);
        this.loadingAvenants = false;
        this.updateCanPrintStatus();
      },
      error : (err: any) => {
        this.loadingAvenants = false;
        this.messageService.setError('Une erreur est survenue lors du chargement des avenants : ' + err.message);
        if (this.authService.isEtudiant()) {
          this.canPrint = false;
        }
      }
    });
  }

}
