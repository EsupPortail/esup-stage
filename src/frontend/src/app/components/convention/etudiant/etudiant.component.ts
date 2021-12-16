import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import { EtudiantService } from "../../../services/etudiant.service";
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

  formConvention: FormGroup;

  typeConventions: any[] = [];
  langueConventions: any[] = [];

  consigneEtablissement: any;

  @Input() convention: any;
  @Input() modifiable: boolean;
  @Output() validated = new EventEmitter<any>();

  @ViewChild(MatExpansionPanel) searchEtudiantPanel: MatExpansionPanel|undefined;

  constructor(
    private authService: AuthService,
    private etudiantService: EtudiantService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private ldapService: LdapService,
    private typeConventionService: TypeConventionService,
    private langueConventionService: LangueConventionService,
    private centreGestionService: CentreGestionService,
    private conventionService: ConventionService,
    private configService: ConfigService,
    private consigneService: ConsigneService,
  ) {
    this.form = this.fb.group({
      id: [null, []],
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.isEtudiant = this.authService.isEtudiant();

    this.consigneService.getConsigneByCentre(null).subscribe((response: any) => {
      this.consigneEtablissement = response;
    });

    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.formConvention = this.fb.group({
        adresseEtudiant: [this.convention.adresseEtudiant, [Validators.required]],
        codePostalEtudiant: [this.convention.codePostalEtudiant, [Validators.required]],
        villeEtudiant: [this.convention.villeEtudiant, [Validators.required]],
        paysEtudiant: [this.convention.paysEtudiant, [Validators.required]],
        telEtudiant: [this.convention.telEtudiant, []],
        telPortableEtudiant: [this.convention.telPortableEtudiant, []],
        courrielPersoEtudiant: [this.convention.courrielPersoEtudiant, [Validators.required, Validators.email]],
        inscription: [null, [Validators.required]],
        inscriptionElp: [null, []],
        idTypeConvention: [this.convention.typeConvention ? this.convention.typeConvention.id : null, [Validators.required]],
        codeLangueConvention: [this.convention.langueConvention ? this.convention.langueConvention.code : null, [Validators.required]],
      });
      this.sansElp = response.autoriserElementPedagogiqueFacultatif;
      if (!this.sansElp) {
        this.formConvention.get('inscriptionElp')?.setValidators([Validators.required]);
      }

      this.formConvention.get('inscription')?.valueChanges.subscribe((inscription: any) => {
        if (inscription) {
          this.centreGestion = inscription.centreGestion;
          this.formConvention.get('inscriptionElp')?.setValue(null);
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
          this.etudiantService.getByLogin(this.authService.userConnected.login).subscribe((response: any) => {
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
    });

    this.typeConventionService.getListActiveWithTemplate().subscribe((response: any) => {
      this.typeConventions = response.data;
    });

    this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.search();
    });
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  search(): void {
    this.selectedRow = undefined;
    if (!this.form.get('id')?.value && !this.form.get('nom')?.value && !this.form.get('prenom')?.value) {
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
    });
    this.etudiantService.getApogeeInscriptions(row.codEtu).subscribe((response: any) => {
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
      const data = {...this.formConvention.value};
      delete data.isncription;
      data.numEtudiant = this.selectedNumEtudiant;
      data.codeComposante = this.formConvention.value.inscription.etapeInscription.codeComposante;
      data.codeEtape = this.formConvention.value.inscription.etapeInscription.codeEtp;
      data.codeVerionEtape = this.formConvention.value.inscription.etapeInscription.codVrsVet;
      data.annee = this.formConvention.value.inscription.annee;
      data.codeElp = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.codElp : null;
      data.libelleELP = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.libElp : null;
      data.creditECTS = this.formConvention.value.inscriptionElp ? this.formConvention.value.inscriptionElp.nbrCrdElp : null;
      if (this.isEtudiant) {
        data.etudiantLogin = this.authService.userConnected.login;
      } else if (this.selectedRow && this.selectedRow.supannAliasLogin) {
        data.etudiantLogin = this.selectedRow.supannAliasLogin
      } else if (this.convention && this.convention.etudiant) {
        data.etudiantLogin = this.convention.etudiant.identEtudiant;
      }
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
    let mimetype = 'applicaton/pdf';
    if (doc.nomReel.endsWith('.doc')) mimetype = 'application/msword';
    if (doc.nomReel.endsWith('.docx')) mimetype = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    this.consigneService.getDocument(this.consigneEtablissement.id, doc.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: mimetype});
      FileSaver.saveAs(blob, doc.nomReel);
    });
  }

}
