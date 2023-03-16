import { Component, EventEmitter, Input, Inject, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { AuthService } from "../../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../../services/message.service";
import { EtudiantService } from "../../../../services/etudiant.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { LdapService } from "../../../../services/ldap.service";
import { CPAMService } from "../../../../services/cpam.service";
import { TypeConventionService } from "../../../../services/type-convention.service";
import { LangueConventionService } from "../../../../services/langue-convention.service";
import * as _ from "lodash";
import { CentreGestionService } from "../../../../services/centre-gestion.service";
import { ConventionService } from "../../../../services/convention.service";
import { debounceTime } from "rxjs/operators";
import { ConfigService } from "../../../../services/config.service";
import { ConsigneService } from "../../../../services/consigne.service";
import * as FileSaver from "file-saver";
import { Router } from "@angular/router";
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig, MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';

@Component({
  selector: 'app-cadre-stage-modal',
  templateUrl: './cadre-stage-modal.component.html',
  styleUrls: ['./cadre-stage-modal.component.scss']
})
export class CadreStageModalComponent implements OnInit {

  convention: any;
  etudiant: any;
  selectedNumEtudiant: string|null = null;
  inscriptions: any[] = [];
  centreGestion: any;
  sansElp: boolean = false;

  formConvention: FormGroup;

  typeConventions: any[] = [];
  langueConventions: any[] = [];

  CPAMs: any[] = [];
  regions: any[] = [];
  libelles: any[] = [];

  centreGestionEtablissement: any;
  consigneEtablissement: any;

  constructor(
    private authService: AuthService,
    private etudiantService: EtudiantService,
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
    private dialogRef: MatDialogRef<CadreStageModalComponent>,
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
      });
      this.sansElp = response.autoriserElementPedagogiqueFacultatif;
      if (!this.sansElp) {
        this.formConvention.get('inscriptionElp')?.setValidators([Validators.required]);
      }

      this.formConvention.get('inscription')?.valueChanges.subscribe((inscription: any) => {
        if (inscription) {
          this.centreGestion = inscription.centreGestion;
          this.formConvention.get('inscriptionElp')?.setValue(null);
          if (inscription.typeConvention) {
            this.formConvention.get('idTypeConvention')?.setValue(inscription.typeConvention.id);
            this.formConvention.get('idTypeConvention')?.disable();
          } else {
            this.formConvention.get('idTypeConvention')?.enable();
          }
        }
      });

      let codEtu: string|undefined = undefined;
      codEtu = this.convention.etudiant.numEtudiant;
      this.choose({codEtu: codEtu});

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
  }

  ngOnChanges(changes: SimpleChanges) {
  }

  choose(row: any): void {
    this.etudiantService.getApogeeData(row.codEtu).subscribe((response: any) => {
      this.etudiant = response;
      this.formConvention.get('adresseEtudiant')?.setValue(this.etudiant.mainAddress);
      this.formConvention.get('codePostalEtudiant')?.setValue(this.etudiant.postalCode);
      this.formConvention.get('villeEtudiant')?.setValue(this.etudiant.town);
      this.formConvention.get('paysEtudiant')?.setValue(this.etudiant.country);
      this.formConvention.get('telEtudiant')?.setValue(this.etudiant.phone);
      this.formConvention.get('telPortableEtudiant')?.setValue(this.etudiant.portablePhone);
      this.formConvention.get('courrielPersoEtudiant')?.setValue(this.etudiant.mailPerso);
    });
    this.etudiantService.getApogeeInscriptions(row.codEtu, '').subscribe((response: any) => {
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

  validate(): void {
    if (this.formConvention.valid) {
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

      data.etudiantLogin = this.convention.etudiant.identEtudiant;

      this.conventionService.update(this.convention.id, data).subscribe((response: any) => {
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
      var blob = new Blob([response as BlobPart], {type: mimetype});
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
}
