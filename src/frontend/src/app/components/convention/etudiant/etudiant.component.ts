import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { EtudiantService } from "../../../services/etudiant.service";
import { CommuneService } from "../../../services/commune.service";
import { CPAMService } from "../../../services/cpam.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../services/ldap.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import * as _ from "lodash";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { ConventionService } from "../../../services/convention.service";
import { debounceTime } from "rxjs/operators";
import { ConfigService } from "../../../services/config.service";
import { ConsigneService } from "../../../services/consigne.service";
import * as FileSaver from "file-saver";
import { ActivatedRoute, Router } from "@angular/router";
import { TitleService } from 'src/app/services/title.service';
import { DomSanitizer } from '@angular/platform-browser';
import {REGEX} from "../../../utils/regex.utils";

@Component({
    selector: 'app-convention-etudiant',
    templateUrl: './etudiant.component.html',
    styleUrls: ['./etudiant.component.scss'],
    standalone: false
})
export class EtudiantComponent implements OnInit, OnChanges {

  isEtudiant: boolean = true;

  form: FormGroup;
  columns = ['numetudiant', 'nomprenom', 'action'];
  etudiants: any[] = [];
  etudiant: any;
  selectedRow: any;
  selectedNumEtudiant: string|null = null;
  inscriptions: any[] = [];
  centreGestion: any;
  sansElp: boolean = false;

  formConvention!: FormGroup;

  typeConventions: any[] = [];
  langueConventions: any[] = [];

  CPAMs: any[] = [];
  regions: any[] = [];
  libelles: any[] = [];

  centreGestionEtablissement: any;
  consigneEtablissement: any;

  communes: any[] = [];

  hasDefaultVolumeHoraire: boolean = false;

  @Input() convention: any;
  @Input() modifiable: boolean = false;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(MatExpansionPanel) searchEtudiantPanel: MatExpansionPanel|undefined;

  constructor(
    private activatedRoute: ActivatedRoute,
    private authService: AuthService,
    private etudiantService: EtudiantService,
    public communeService: CommuneService,
    public cpamService: CPAMService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private ldapService: LdapService,
    private typeConventionService: TypeConventionService,
    private langueConventionService: LangueConventionService,
    private centreGestionService: CentreGestionService,
    private conventionService: ConventionService,
    private configService: ConfigService,
    private consigneService: ConsigneService,
    private router: Router,
    private titleService: TitleService,
    private sanitizer: DomSanitizer,
  ) {
    this.form = this.fb.group({
      codEtu: [null, []],
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();
    this.communeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.communes = response;
    });
    this.centreGestionService.getCentreEtablissement().subscribe((response: any) => {
      this.centreGestionEtablissement = response;
      if(response.consigne && ((response.consigne.texte) || (response.consigne.documents && response.consigne.documents.length > 0))){
        this.consigneEtablissement = response.consigne;
      }
    });

    this.configService.getConfigGenerale().subscribe((response: any) => {
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
        volumeHoraireFormation: [this.convention.volumeHoraireFormation ? this.convention.volumeHoraireFormation : null, []],
        volumeHoraireFormationBool: [this.convention.volumeHoraireFormation !== null && this.convention.volumeHoraireFormation !== '200+', []],
      });
      this.sansElp = response.autoriserElementPedagogiqueFacultatif;

      const typeConventionControl = this.formConvention.get('idTypeConvention');
      const langueConventionControl = this.formConvention.get('codeLangueConvention');
      const initialTypeConvention = typeConventionControl?.value;
      const initialLangueConvention = langueConventionControl?.value;
      if (initialTypeConvention) {
        this.loadLangues(initialTypeConvention, initialLangueConvention);
      }

      this.formConvention.get('inscription')?.valueChanges.subscribe((inscription: any) => {
        if (inscription) {
          if (!this.sansElp && inscription.elementPedagogiques && inscription.elementPedagogiques.length > 0) {
            this.formConvention.get('inscriptionElp')?.setValidators([Validators.required]);
          }
          if(!this.centreGestion){
            this.centreGestion = inscription.centreGestion;
          }
          this.formConvention.get('inscriptionElp')?.setValue(null);
          if (inscription.typeConvention) {
            this.formConvention.get('idTypeConvention')?.setValue(inscription.typeConvention.id);
            this.formConvention.get('idTypeConvention')?.disable();
          } else {
            this.formConvention.get('idTypeConvention')?.enable();
          }
          if (!this.convention.volumeHoraireFormation) {
            this.formConvention.get('volumeHoraireFormation')?.setValue(inscription.etapeInscription.volumeHoraire);
          }
          this.hasDefaultVolumeHoraire = inscription.etapeInscription.volumeHoraire && inscription.etapeInscription.volumeHoraire != "0";
          if (this.hasDefaultVolumeHoraire) {
            this.formConvention.get('volumeHoraireFormationBool')?.setValue(true);
          }
        }
      });

      if (!this.modifiable) {
        this.formConvention.disable();
      }

      let codEtu: string|undefined = undefined;
      if (this.convention && this.convention.etudiant) {
        codEtu = this.convention.etudiant.numEtudiant;
        this.choose({codEtu: codEtu});
      } else {
        if (this.isEtudiant) {
          this.etudiantService.getByLogin(this.authService.userConnected.uid).subscribe((response: any) => {
            codEtu = response.numEtudiant;
            this.choose({codEtu: codEtu});
          });
        }
      }

      // Recherche des langues disponible en fonction du type de convention
      this.formConvention.get('idTypeConvention')?.valueChanges.subscribe((val: any) => {
        if (val) {
          this.loadLangues(val, this.formConvention.get('codeLangueConvention')?.value);
        } else {
          this.langueConventions = [];
          this.formConvention.get('codeLangueConvention')?.setValue(null);
          this.messageService.setWarning("Aucune langue disponible pour ce type de convention.");
        }
      });

      this.cpamService.findAll().subscribe((response: any) => {
        this.CPAMs = response;
        this.regions = [...new Set(response.map((r : any) => r.region))];
        this.regions = this.regions.sort((a, b) => {return a.localeCompare(b)});
        this.regions.push('Autre')
        if (this.formConvention.get('regionCPAM')?.value && this.modifiable) {
          this.setCPAMLibelles({value: this.formConvention.get('regionCPAM')?.value});
        } else {
          this.formConvention.get('libelleCPAM')?.disable();
        }
      });
    });

    this.typeConventionService.getListActiveWithTemplate().subscribe((response: any) => {
      this.typeConventions = response.data;
    });

    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      this.search();
    });
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  search(): void {
    this.selectedRow = undefined;
    if (!this.form.get('codEtu')?.value && !this.form.get('nom')?.value && !this.form.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.etudiant = undefined;
    this.selectedNumEtudiant = null;
    this.ldapService.searchEtudiants(this.form.value).subscribe((response: any) => {
      this.etudiants = response;
      if (this.etudiants.length === 1) {
        this.choose(this.etudiants[0]);
      }
    });
  }

  choose(row: any): void {
    this.selectedRow = row;
    this.selectedNumEtudiant = row.codEtu;

    // Ferme le panel de recherche d'étudiant
    if (this.searchEtudiantPanel) {
      this.searchEtudiantPanel.expanded = false;
    }

    // Vérifie si la convention existe déjà avec un ID
    if (this.convention && this.convention.id) {
      this.etudiant = this.convention.etudiant;
      // Remplir le formulaire avec les valeurs de la convention
      this.formConvention.get('adresseEtudiant')?.setValue(this.convention.adresseEtudiant);
      this.formConvention.get('codePostalEtudiant')?.setValue(this.convention.codePostalEtudiant);
      this.formConvention.get('villeEtudiant')?.setValue(this.convention.villeEtudiant);
      this.formConvention.get('paysEtudiant')?.setValue(this.convention.paysEtudiant);
      this.formConvention.get('telEtudiant')?.setValue(this.convention.telEtudiant);
      this.formConvention.get('telPortableEtudiant')?.setValue(this.convention.telPortableEtudiant);
      this.formConvention.get('courrielPersoEtudiant')?.setValue(this.convention.courrielPersoEtudiant);
      this.centreGestion = this.convention.centreGestion;

      if (this.convention.etape) {
        // Recrée un objet inscription avec les données de la convention existante
        const annee = parseInt(this.convention.annee.split('/')[0]) ;

        // Définir le tableau elementPedagogiques comme un tableau d'objet any[]
        const elementPedagogiques: any[] = [];

        if (this.convention.codeElp) {
          // Ajoute l'élément pédagogique si présent dans la convention
          elementPedagogiques.push({
            codElp: this.convention.codeElp,
            libElp: this.convention.libelleELP,
            nbrCrdElp: this.convention.creditECTS
          });
        }

        const inscription = {
          annee: annee,
          anneeFormatted: annee + '/' + (+annee + 1),
          etapeInscription: {
            codeEtp: this.convention.etape.id.code,
            codVrsVet: this.convention.etape.id.codeVersionEtape,
            libWebVet: this.convention.libWebVet,
            codeComposante: this.convention.codeComposante,
            libComposante: this.convention.ufr?.libelle || this.convention.libelleComposante
          },
          typeConvention: this.convention.typeConvention ? {id: this.convention.typeConvention.id, libelle: this.convention.typeConvention.libelle} : null,
          elementPedagogiques: elementPedagogiques
        };

        this.formConvention.get('inscription')?.setValue(inscription);
        if (inscription.typeConvention) {
          this.formConvention.get('idTypeConvention')?.setValue(inscription.typeConvention.id);
          this.formConvention.get('idTypeConvention')?.disable();
        }

        if (this.convention.codeElp && elementPedagogiques.length > 0) {
          this.formConvention.get('inscriptionElp')?.setValue(elementPedagogiques[0]);
        }

        // S'assurer que l'inscription est disponible pour l'interface
        this.inscriptions = [inscription];
      }
    } else {
      // Pour une nouvelle convention, obtenir les données depuis Apogee
      this.etudiantService.getApogeeData(row.codEtu).subscribe((response: any) => {
        this.etudiant = response;

        // Remplir le formulaire avec les valeurs de l'étudiant
        this.formConvention.get('adresseEtudiant')?.setValue(this.convention?.adresseEtudiant || this.etudiant.mainAddress);
        this.formConvention.get('codePostalEtudiant')?.setValue(this.convention?.codePostalEtudiant || this.etudiant.postalCode);
        this.formConvention.get('villeEtudiant')?.setValue(this.convention?.villeEtudiant || this.etudiant.town);
        this.formConvention.get('paysEtudiant')?.setValue(this.convention?.paysEtudiant || this.etudiant.country);
        this.formConvention.get('telEtudiant')?.setValue(this.convention?.telEtudiant || this.etudiant.phone);
        this.formConvention.get('telPortableEtudiant')?.setValue(this.convention?.telPortableEtudiant || this.etudiant.portablePhone);
        this.formConvention.get('courrielPersoEtudiant')?.setValue(this.convention?.courrielPersoEtudiant || this.etudiant.mailPerso);

        this.activatedRoute.params.subscribe((param: any) => {
          const pathId = param.id;
          if (pathId === 'create') {
            this.titleService.title = 'Création d\'une convention pour ' + this.etudiant.nompatro + ' ' + this.etudiant.prenom;
          }
        });
      });

      // Récupérer les inscriptions
      this.etudiantService.getApogeeInscriptions(row.codEtu, this.convention ? this.convention.annee : null).subscribe((response: any) => {
        this.inscriptions = response;
        this.inscriptions.sort((a,b) => a.annee < b.annee ? 1 : -1);
        if (this.inscriptions.length === 1) {
          this.formConvention.get('inscription')?.setValue(this.inscriptions[0]);
        }

        if (this.convention?.etape) {
          const inscription = this.inscriptions.find((i: any) => {
            return i.etapeInscription.codeEtp === this.convention.etape.id.code;
          });

          if (inscription) {
            this.formConvention.get('inscription')?.setValue(inscription);

            if (this.convention.codeElp) {
              const inscriptionElp = inscription.elementPedagogiques.find((i: any) => {
                return i.codElp === this.convention.codeElp;
              });

              this.formConvention.get('inscriptionElp')?.setValue(inscriptionElp);
            }
          } else if (this.inscriptions.length > 1) {
            this.formConvention.get('inscription')?.reset();
          }
        }
      });
    }
  }

  get selectedInscription() {
    return this.formConvention.controls.inscription.value;
  }

  isSelected(row: any): boolean {
    return _.isEqual(row, this.selectedRow);
  }

  private loadLangues(idTypeConvention: number, currentCode?: string | null): void {
    this.langueConventionService.getListActiveByTypeConvention(idTypeConvention).subscribe((response: any) => {
      this.langueConventions = response.data || [];
      const langueControl = this.formConvention.get('codeLangueConvention');
      const hasCurrent = currentCode && this.langueConventions.some((l: any) => l.code === currentCode);
      if (!hasCurrent) {
        const fallback = this.langueConventions.length === 1 ? this.langueConventions[0].code : null;
        langueControl?.setValue(fallback);
      }
    });
  }

  validate(): void {
    if (this.formConvention.valid) {
      // Contrôle code postal commune
      if (this.isFr() && !this.isCodePostalValid()) {
        this.messageService.setError('Code postal inconnu');
        return;
      }
      const data = {...this.formConvention.getRawValue()};
      delete data.inscription;
      delete data.inscriptionElp;
      data.numEtudiant = this.selectedNumEtudiant;

      if (this.formConvention.value.inscription) {
        data.codeComposante = this.formConvention.value.inscription.etapeInscription.codeComposante;
        data.libelleComposante = this.formConvention.value.inscription.etapeInscription.libComposante;
        data.codeEtape = this.formConvention.value.inscription.etapeInscription.codeEtp;
        data.libelleEtape = this.formConvention.value.inscription.etapeInscription.libWebVet;
        data.codeVersionEtape = this.formConvention.value.inscription.etapeInscription.codVrsVet;
        data.annee = this.formConvention.value.inscription.annee;
      } else if (this.convention) {
        data.codeComposante = this.convention.codeComposante;
        data.libelleComposante = this.convention.libelleComposante;
        data.codeEtape = this.convention.etape?.id?.code;
        data.codeVersionEtape = this.convention.etape?.id?.codeVersionEtape;
        data.libelleEtape = this.convention.libWebVet;
        data.annee = this.convention.annee ? parseInt(this.convention.annee.split('/')[0]) : null;
      }
      if (!data.codeVersionEtape && this.convention?.etape?.id?.codeVersionEtape) {
        data.codeVersionEtape = this.convention.etape.id.codeVersionEtape;
      }
      if (!data.codeEtape && this.convention?.etape?.id?.code) {
        data.codeEtape = this.convention.etape.id.code;
      }
      if (!data.libelleEtape && this.convention?.libWebVet) {
        data.libelleEtape = this.convention.libWebVet;
      }
      if (!data.codeComposante && this.convention?.codeComposante) {
        data.codeComposante = this.convention.codeComposante;
      }
      if (!data.libelleComposante && this.convention?.libelleComposante) {
        data.libelleComposante = this.convention.libelleComposante;
      }
      if (!data.codeComposante && this.convention?.ufr?.id?.code) {
        data.codeComposante = this.convention.ufr.id.code;
      }
      if (!data.libelleComposante && this.convention?.ufr?.libelle) {
        data.libelleComposante = this.convention.ufr.libelle;
      }
      if (!data.libelleEtape && this.convention?.etape?.libelle) {
        data.libelleEtape = this.convention.etape.libelle;
      }
      if (!data.codeLangueConvention && this.convention?.langueConvention?.code) {
        data.codeLangueConvention = this.convention.langueConvention.code;
      }
      if (data.annee != null && typeof data.annee !== 'string') {
        data.annee = data.annee.toString();
      } else if (data.annee == null && this.convention?.annee) {
        data.annee = this.convention.annee.split('/')[0];
      }
      if (this.formConvention.value.inscriptionElp) {
        Object.assign(data, {
          codeElp: this.formConvention.value.inscriptionElp.codElp,
          libelleELP: this.formConvention.value.inscriptionElp.libElp,
          creditECTS: this.formConvention.value.inscriptionElp.nbrCrdElp,
        });
      }
      if (this.isEtudiant) {
        data.etudiantLogin = this.authService.userConnected.uid;
      } else if (this.selectedRow && this.selectedRow.uid) {
        data.etudiantLogin = this.selectedRow.uid;
      } else if (this.convention && this.convention.etudiant) {
        data.etudiantLogin = this.convention.etudiant.identEtudiant;
      }
      if (!data.volumeHoraireFormationBool)
        data.volumeHoraireFormation = "200+";
      if (!this.convention || !this.convention.id) {
        this.conventionService.create(data).subscribe((response: any) => {
          this.validated.emit(response);
        });
      } else {
        this.conventionService.update(this.convention.id, data).subscribe((response: any) => {
          this.validated.emit(response);
        });
      }
    }
  }

  downloadDoc(event: any, doc: any): void {
    event.preventDefault();
    event.stopPropagation();
    let mimetype = 'application/pdf';
    if (doc.nomReel.endsWith('.doc')) mimetype = 'application/msword';
    if (doc.nomReel.endsWith('.docx')) mimetype = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    this.consigneService.getDocument(this.consigneEtablissement.id, doc.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: mimetype});
      FileSaver.saveAs(blob, doc.nomReel);
    });
  }

  deleteConventionBrouillon(): void {
    this.conventionService.deleteConventionBrouillon().subscribe((response: any) => {
      this.messageService.setSuccess('Création de la convention réinitialisée');
      this.router.onSameUrlNavigation = 'reload';
      this.router.navigate([this.router.url]);
    });
  }

  deleteConvention(): void {
    this.conventionService.deleteConvention(this.convention.id).subscribe((response: any) => {
      this.messageService.setSuccess('Convention supprimée');
      this.router.navigate(['tableau-de-bord']);
    });
  }

  canDelete(): boolean {
    if (this.convention.validationCreation) {
      let hasValidation = false;
      for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
        if (this.convention.centreGestion[validation] && this.convention[validation]) {
          hasValidation = true;
        }
      }
      return !hasValidation;
    }
    return false;
  }

  updateCommune(commune : any): void {
    this.formConvention.get('villeEtudiant')?.setValue(commune.split(' - ')[0]);
    this.formConvention.get('codePostalEtudiant')?.setValue(commune.split(' - ')[1]);
  }

  isFr() {
    let pays = this.formConvention.get('paysEtudiant')?.value;
    return pays.toLowerCase() === 'france';
  }

  isCodePostalValid() {
    let codePostal = this.formConvention.get('codePostalEtudiant')?.value;
    if (codePostal) {
      let commune = this.communes.find(c => c.codePostal === codePostal);
      if (commune)
        return true;
    }
    return false;
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

  trustHtml(html: string){
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }

  isUserEtudiant(): boolean {
    return this.authService.isEtudiant();
  }
}
