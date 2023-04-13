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
import { Router } from "@angular/router";
import { TitleService } from 'src/app/services/title.service';

@Component({
  selector: 'app-convention-etudiant',
  templateUrl: './etudiant.component.html',
  styleUrls: ['./etudiant.component.scss']
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

  volumeHoraireFormationBool: boolean = false;
  defaultVolumeHoraire: any = null;

  @Input() convention: any;
  @Input() modifiable: boolean = false;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(MatExpansionPanel) searchEtudiantPanel: MatExpansionPanel|undefined;

  constructor(
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
      if (this.centreGestionEtablissement) {
        this.consigneService.getConsigneByCentre(this.centreGestionEtablissement.id).subscribe((response: any) => {
          this.consigneEtablissement = response;
        });
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
        courrielPersoEtudiant: [this.convention.courrielPersoEtudiant, [Validators.required, Validators.pattern('[^@ ]+@[^@. ]+\\.[^@ ]+')]],
        regionCPAM: [this.convention.regionCPAM, []],
        libelleCPAM: [this.convention.libelleCPAM, []],
        adresseCPAM: [this.convention.adresseCPAM, []],
        inscription: [null, [Validators.required]],
        inscriptionElp: [null, []],
        idTypeConvention: [this.convention.typeConvention ? this.convention.typeConvention.id : null, [Validators.required]],
        codeLangueConvention: [this.convention.langueConvention ? this.convention.langueConvention.code : null, [Validators.required]],
        volumeHoraireFormation: [this.convention.volumeHoraireFormation ? this.convention.volumeHoraireFormation : null, []],
        volumeHoraireFormationBool: [false, ],
      });
      this.sansElp = response.autoriserElementPedagogiqueFacultatif;

      this.formConvention.get('inscription')?.valueChanges.subscribe((inscription: any) => {
        if (inscription) {
          this.sansElp = this.sansElp || !inscription.elementPedagogiques || inscription.elementPedagogiques.length == 0;
          if (!this.sansElp) {
            this.formConvention.get('inscriptionElp')?.setValidators([Validators.required]);
          }
          this.centreGestion = inscription.centreGestion;
          this.formConvention.get('inscriptionElp')?.setValue(null);
          if (inscription.typeConvention) {
            this.formConvention.get('idTypeConvention')?.setValue(inscription.typeConvention.id);
            this.formConvention.get('idTypeConvention')?.disable();
          } else {
            this.formConvention.get('idTypeConvention')?.enable();
          }
          this.formConvention.get('volumeHoraireFormation')?.setValue(inscription.etapeInscription.volumeHoraire);
          this.defaultVolumeHoraire = this.formConvention.get('volumeHoraireFormation')?.value;
          if(inscription.etapeInscription.volumeHoraire && inscription.etapeInscription.volumeHoraire != "0")
            this.volumeHoraireFormationBool = true;
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
          this.langueConventionService.getListActiveByTypeConvention(val).subscribe((response: any) => {
            this.langueConventions = response.data;
          });
        } else {
          this.langueConventions = [];
          this.messageService.setWarning("Aucune langue disponible pour ce type de convention.");
        }
      });

      this.cpamService.findAll().subscribe((response: any) => {
        this.CPAMs = response;
        this.regions = [...new Set(response.map((r : any) => r.region))];
        this.regions = this.regions.sort((a, b) => {return a.localeCompare(b)});
        if (this.formConvention.get('regionCPAM')?.value) {
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
    this.etudiantService.getApogeeData(row.codEtu).subscribe((response: any) => {
      this.selectedNumEtudiant = row.codEtu;
      if (this.searchEtudiantPanel) {
        this.searchEtudiantPanel.expanded = false;
      }
      this.etudiant = response;
      this.formConvention.get('adresseEtudiant')?.setValue(this.etudiant.mainAddress);
      this.formConvention.get('codePostalEtudiant')?.setValue(this.etudiant.postalCode);
      this.formConvention.get('villeEtudiant')?.setValue(this.etudiant.town);
      this.formConvention.get('paysEtudiant')?.setValue(this.etudiant.country);
      this.formConvention.get('telEtudiant')?.setValue(this.etudiant.phone);
      this.formConvention.get('telPortableEtudiant')?.setValue(this.etudiant.portablePhone);
      this.formConvention.get('courrielPersoEtudiant')?.setValue(this.etudiant.mailPerso);
      this.titleService.title = 'Création d\'une convention pour ' + this.etudiant.nompatro + ' ' + this.etudiant.prenom;
    });
    this.etudiantService.getApogeeInscriptions(row.codEtu, this.convention ? this.convention.annee : null).subscribe((response: any) => {
      this.inscriptions = response;
      if (this.inscriptions.length === 1) {
        this.formConvention.get('inscription')?.setValue(this.inscriptions[0]);
      }
      if (this.convention.etape) {
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

  get selectedInscription() {
    return this.formConvention.controls.inscription.value;
  }

  isSelected(row: any): boolean {
    return _.isEqual(row, this.selectedRow);
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
      if (this.isEtudiant) {
        data.etudiantLogin = this.authService.userConnected.uid;
      } else if (this.selectedRow && this.selectedRow.uid) {
        data.etudiantLogin = this.selectedRow.uid;
      } else if (this.convention && this.convention.etudiant) {
        data.etudiantLogin = this.convention.etudiant.identEtudiant;
      }
      if(this.volumeHoraireFormationBool == false)
        data.volumeHoraireFormation = "200";
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

  changevolumeHoraireFormationBool(value: boolean)
  {
    if (!(this.defaultVolumeHoraire && this.defaultVolumeHoraire > 0))
    {
      this.volumeHoraireFormationBool = value;
      this.formConvention.controls['volumeHoraireFormationBool'].setValue(value);
    }
  }
}
