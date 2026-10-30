import { Component, Inject, OnInit, } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../../services/message.service";
import { EtudiantService } from "../../../../services/etudiant.service";
import { CPAMService } from "../../../../services/cpam.service";
import { LangueConventionService } from "../../../../services/langue-convention.service";
import { CentreGestionService } from "../../../../services/centre-gestion.service";
import { ConventionService } from "../../../../services/convention.service";
import { ConfigService } from "../../../../services/config.service";
import { ConsigneService } from "../../../../services/consigne.service";
import * as FileSaver from "file-saver";
import {REGEX} from "../../../../utils/regex.utils";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {forkJoin} from "rxjs";
import { CadreStageConventionPayload } from "../../../../models/cadre-stage-convention-payload.model";

@Component({
    selector: 'app-cadre-stage-modal',
    templateUrl: './cadre-stage-modal.component.html',
    styleUrls: ['./cadre-stage-modal.component.scss'],
    standalone: false
})
export class CadreStageModalComponent implements OnInit {

  convention: any;
  etudiant: any;
  selectedNumEtudiant: string|null = null;
  inscriptions: any[] = [];
  centreGestion: any;
  sansElp: boolean = false;

  formConvention!: FormGroup;

  typeConventions: any[] = [];
  aucunTypeConventionDisponible: boolean = false;
  readonly aucunTypeConventionMessage = "Aucun type de convention n'est disponible pour ce régime d'inscription. Veuillez contacter votre gestionnaire de scolarité.";
  langueConventions: any[] = [];

  CPAMs: any[] = [];
  regions: any[] = [];
  libelles: any[] = [];

  centreGestionEtablissement: any;
  consigneEtablissement: any;

  constructor(
    private readonly etudiantService: EtudiantService,
    public cpamService: CPAMService,
    private readonly fb: FormBuilder,
    private readonly messageService: MessageService,
    private readonly langueConventionService: LangueConventionService,
    private readonly centreGestionService: CentreGestionService,
    private readonly conventionService: ConventionService,
    private readonly configService: ConfigService,
    private readonly consigneService: ConsigneService,
    private readonly dialogRef: MatDialogRef<CadreStageModalComponent>,
    @Inject(MAT_DIALOG_DATA) data: any
  ) {
    this.convention = data.convention;
    this.selectedNumEtudiant = data.convention.etudiant.numEtudiant;
    this.etudiant = data.convention.etudiant;
  }

  ngOnInit(): void {

    this.centreGestionService.getCentreEtablissement().subscribe((response: any) => {
      this.centreGestionEtablissement = response;
      if (this.centreGestionEtablissement) {
        this.consigneService.getConsigneByCentre(this.centreGestionEtablissement.id).subscribe({
          next: (response: any) => {
            if (response?.texte) {
              this.consigneEtablissement = response;
            } else {
              console.info('Pas de consigne disponible pour ce centre');
            }
          },
          error: (err) => {
            console.warn('Erreur lors du chargement de la consigne', err);
          }
        });
      }
    });

    forkJoin([
      this.configService.getConfigGenerale(),
      this.cpamService.findAll(),
    ]).subscribe(([
      configGenerale,
      CPAMs,
    ]: [any,any]) => {
      this.formConvention = this.fb.group({
        adresseEtudiant: [this.convention.adresseEtudiant, [Validators.required]],
        codePostalEtudiant: [this.convention.codePostalEtudiant, [Validators.required]],
        villeEtudiant: [this.convention.villeEtudiant, [Validators.required]],
        paysEtudiant: [this.convention.paysEtudiant, [Validators.required]],
        telEtudiant: [this.convention.telEtudiant, []],
        telPortableEtudiant: [this.convention.telPortableEtudiant, []],
        courrielPersoEtudiant: [this.convention.courrielPersoEtudiant, [Validators.required, Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
        regionCPAM: [this.convention.regionCPAM, []],
        libelleCPAM: [this.convention.libelleCPAM, []],
        adresseCPAM: [this.convention.adresseCPAM, []],
        inscription: [null, [Validators.required]],
        inscriptionElp: [null, []],
        idTypeConvention: [this.convention.typeConvention ? this.convention.typeConvention.id : null, [Validators.required]],
        codeLangueConvention: [this.convention.langueConvention ? this.convention.langueConvention.code : null, [Validators.required]],
      });
      // Options type dès l'init (#557) : ne pas attendre choose()/Apogée
      if (this.convention.typeConvention?.id != null) {
        this.typeConventions = this.ensureConventionTypeInOptions([]);
      }
      this.sansElp = configGenerale.autoriserElementPedagogiqueFacultatif;
      if (!this.sansElp) {
        this.formConvention.get('inscriptionElp')?.setValidators([Validators.required]);
      }

      this.formConvention.get('inscription')?.valueChanges.subscribe((inscription: any) => {
        if (inscription) {
          const autoTypeConventionDisabled = !!inscription.centreGestion?.desactiverSelectionAutomatiqueTypeConvention;
          const currentTypeConventionId = this.formConvention.get('idTypeConvention')?.value;
          this.typeConventions = this.buildTypeConventionsFromInscription(inscription);
          this.aucunTypeConventionDisponible = this.typeConventions.length === 0;
          const autoTypeConvention = autoTypeConventionDisabled
            ? null
            : (inscription.typeConvention || (this.typeConventions.length === 1 ? this.typeConventions[0] : null));
          const isCurrentTypeConventionStillAvailable = this.typeConventions.some(
            (typeConvention: any) => this.sameTypeConventionId(typeConvention.id, currentTypeConventionId)
          );
          this.centreGestion = inscription.centreGestion;
          this.formConvention.get('inscriptionElp')?.setValue(null);

          if (currentTypeConventionId && isCurrentTypeConventionStillAvailable) {
            // Préserver le type déjà posé (#557) ; désactiver s'il correspond au type auto inscription
            if (this.sameTypeConventionId(currentTypeConventionId, inscription.typeConvention?.id)) {
              this.formConvention.get('idTypeConvention')?.disable();
            } else {
              this.formConvention.get('idTypeConvention')?.enable();
            }
          } else if (currentTypeConventionId && !isCurrentTypeConventionStillAvailable) {
            // Type courant plus tenable pour le nouveau régime : auto-type si possible, jamais null si convention existante
            if (autoTypeConvention) {
              this.formConvention.get('idTypeConvention')?.setValue(autoTypeConvention.id);
              this.formConvention.get('idTypeConvention')?.disable();
            } else if (this.aucunTypeConventionDisponible) {
              if (!this.convention?.id) {
                this.formConvention.get('idTypeConvention')?.setValue(null);
              }
              this.formConvention.get('idTypeConvention')?.disable();
              this.messageService.setWarning(this.aucunTypeConventionMessage);
            } else {
              if (!this.convention?.id) {
                this.formConvention.get('idTypeConvention')?.setValue(null);
              }
              this.formConvention.get('idTypeConvention')?.enable();
            }
          } else if (autoTypeConvention) {
            this.formConvention.get('idTypeConvention')?.setValue(autoTypeConvention.id);
            this.formConvention.get('idTypeConvention')?.disable();
          } else if (this.aucunTypeConventionDisponible) {
            if (!this.convention?.id) {
              this.formConvention.get('idTypeConvention')?.setValue(null);
            }
            this.formConvention.get('idTypeConvention')?.disable();
            this.messageService.setWarning(this.aucunTypeConventionMessage);
          } else {
            this.formConvention.get('idTypeConvention')?.enable();
          }
        } else {
          this.typeConventions = [];
          this.aucunTypeConventionDisponible = true;
        }
      });

      let codEtu: string|undefined = undefined;
      codEtu = this.convention.etudiant.numEtudiant;
      this.choose({codEtu: codEtu});

      // Recherche des langues disponible en fonction du type de convention
      this.formConvention.get('idTypeConvention')?.valueChanges.subscribe((val: any) => {
        if (val) {
          this.loadLangues(val, this.formConvention.get('codeLangueConvention')?.value);
        } else {
          this.langueConventions = [];
        }
      });

      this.CPAMs = CPAMs;
      this.regions = [...new Set(CPAMs.map((r: any) => r.region))];
      this.regions = this.regions.sort((a, b) => { return a.localeCompare(b) });
      if (this.formConvention.get('regionCPAM')?.value) {
        this.setCPAMLibelles({value: this.formConvention.get('regionCPAM')?.value});
      } else {
        this.formConvention.get('libelleCPAM')?.disable();
      }
    });
  }

  choose(row: any): void {
    this.etudiantService.getApogeeInscriptions(row.codEtu, '').subscribe((response: any) => {
      this.inscriptions = response;
      if (this.convention.etape) {
        const inscription = this.inscriptions.find((i: any) => {
          return i.etapeInscription.codeEtp === this.convention.etape.id.code
            && i.etapeInscription.codVrsVet === this.convention.etape.id.codeVersionEtape;
        });
        if (inscription) {
          this.formConvention.get('inscription')?.reset(inscription, {emitEvent: false});
          this.syncInscriptionOuverture(inscription);
          if (this.convention.codeElp) {
            const inscriptionElp = inscription.elementPedagogiques.find((i: any) => {
              return i.codElp === this.convention.codeElp;
            });
            this.formConvention.get('inscriptionElp')?.reset(inscriptionElp, {emitEvent: false});
          }
        } else if (this.inscriptions.length >= 1) {
          // Pas de match : forcer un choix manuel (y compris s'il n'y a qu'une inscription).
          // emitEvent:false pour ne pas vider typeConventions via valueChanges avant le choix.
          this.formConvention.get('inscription')?.reset(null, {emitEvent: false});
        }
      } else if (this.inscriptions.length === 1) {
        const inscription = this.inscriptions[0];
        this.formConvention.get('inscription')?.reset(inscription, {emitEvent: false});
        this.syncInscriptionOuverture(inscription);
      }
    });
  }

  get selectedInscription() {
    return this.formConvention.controls.inscription.value;
  }

  getInscriptionLibelle(inscription: any): string {
    return inscription?.etapeInscription?.libWebVet || '';
  }

  getFormationDisplayLabel(inscription: any): string {
    const code = inscription?.etapeInscription?.codeEtp ?? '';
    const libelle = this.getInscriptionLibelle(inscription);
    return libelle ? `${code} - ${libelle}` : code;
  }

  getFormationTooltip(inscription: any): string {
    const code = inscription?.etapeInscription?.codeEtp ?? '';
    const libelle = this.getInscriptionLibelle(inscription);
    const composante = inscription?.etapeInscription?.libComposante;
    return [code, libelle, composante].filter(Boolean).join(' — ');
  }

  validate(): void {
    if (this.aucunTypeConventionDisponible) {
      this.messageService.setError(this.aucunTypeConventionMessage);
      return;
    }
    if (this.formConvention.valid) {
      const data = {...this.formConvention.getRawValue()};
      delete data.inscription;
      delete data.inscriptionElp;
      data.numEtudiant = this.selectedNumEtudiant;
      data.codeComposante = this.formConvention.value.inscription.etapeInscription.codeComposante;
      data.libelleComposante = this.formConvention.value.inscription.etapeInscription.libComposante;
      data.codeEtape = this.formConvention.value.inscription.etapeInscription.codeEtp;
      data.libelleEtape = this.formConvention.value.inscription.etapeInscription.libWebVet;
      data.codeVersionEtape = this.formConvention.value.inscription.etapeInscription.codVrsVet;
      data.annee = this.formConvention.value.inscription.annee;
      data.codeElp = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.codElp : null;
      data.libelleELP = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.libElp : null;
      data.creditECTS = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.nbrCrdElp : null;

      data.etudiantLogin = this.convention.etudiant.identEtudiant;
      const payload = CadreStageConventionPayload.from(data);

      this.conventionService.update(this.convention.id, payload).subscribe((response: any) => {
        this.dialogRef.close(response);
      });
    }
  }

  downloadDoc(event: any, doc: any): void {
    event.preventDefault();
    event.stopPropagation();
    let mimetype = 'application/pdf';
    if (doc.nomReel.endsWith('.doc')) mimetype = 'application/msword';
    if (doc.nomReel.endsWith('.docx')) mimetype = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    this.consigneService.getDocument(this.consigneEtablissement.id, doc.id).subscribe((response: any) => {
      let blob = new Blob([response as BlobPart], {type: mimetype});
      FileSaver.saveAs(blob, doc.nomReel);
    });
  }

  cancel() : void{
    this.dialogRef.close(null);
  }

  setCPAMLibelles(event: any) {
    this.formConvention.get('libelleCPAM')?.enable();
    this.libelles = this.CPAMs.filter((c : any) => c.region === event.value);
    this.libelles = [...new Set(this.libelles.map((c : any) => c.libelle))];
    this.libelles = this.libelles.sort((a, b) => {return a.localeCompare(b)});
  }

  setCPAMRegion(event: any) {
    let adresse = this.CPAMs.find((c : any) => c.libelle === event.option.value);
    if (adresse){
      this.formConvention.get('adresseCPAM')?.setValue(adresse.adresse);
    }
  }

  /**
   * Synchronise options / disable / langues à l'ouverture, sans clear ELP ni auto-type (#557).
   */
  private syncInscriptionOuverture(inscription: any): void {
    this.centreGestion = inscription.centreGestion;
    this.typeConventions = this.buildTypeConventionsFromInscription(inscription);
    this.aucunTypeConventionDisponible = this.typeConventions.length === 0;

    const currentId = this.formConvention.get('idTypeConvention')?.value;
    if (this.sameTypeConventionId(currentId, inscription.typeConvention?.id)) {
      this.formConvention.get('idTypeConvention')?.disable({emitEvent: false});
    } else if (this.aucunTypeConventionDisponible) {
      this.formConvention.get('idTypeConvention')?.disable({emitEvent: false});
      this.messageService.setWarning(this.aucunTypeConventionMessage);
    } else {
      this.formConvention.get('idTypeConvention')?.enable({emitEvent: false});
    }

    if (currentId) {
      this.loadLangues(currentId, this.formConvention.get('codeLangueConvention')?.value);
    }
  }

  private buildTypeConventionsFromInscription(inscription: any): any[] {
    const base = inscription.typeConventionsDisponibles
      || (inscription.typeConvention ? [inscription.typeConvention] : []);
    return this.ensureConventionTypeInOptions(base);
  }

  private ensureConventionTypeInOptions(typeConventions: any[]): any[] {
    const list = [...(typeConventions || [])];
    const current = this.convention?.typeConvention;
    if (current?.id != null && !list.some((tc: any) => this.sameTypeConventionId(tc.id, current.id))) {
      list.push(current);
    }
    return list;
  }

  private sameTypeConventionId(a: any, b: any): boolean {
    return a != null && b != null && Number(a) === Number(b);
  }

  private loadLangues(idTypeConvention: number, currentCode?: string | null): void {
    this.langueConventionService.getListActiveByTypeConvention(idTypeConvention).subscribe((response: any) => {
      this.langueConventions = response.data || [];
      if (this.langueConventions.length === 0) {
        this.messageService.setWarning("Aucune langue disponible pour ce type de convention.");
      }
      const langueControl = this.formConvention.get('codeLangueConvention');
      const hasCurrent = currentCode && this.langueConventions.some((l: any) => l.code === currentCode);
      if (!hasCurrent) {
        const fallback = this.langueConventions.length === 1 ? this.langueConventions[0].code : null;
        langueControl?.setValue(fallback);
      }
    });
  }
}
