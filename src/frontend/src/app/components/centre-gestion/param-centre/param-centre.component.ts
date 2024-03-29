import { Component, OnInit, Input, Output, EventEmitter, ViewChildren, QueryList } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { LdapService } from "../../../services/ldap.service";
import { EnseignantService } from "../../../services/enseignant.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { debounceTime } from "rxjs/operators";
import { ConfigService } from "../../../services/config.service";
import { CdkDragDrop, moveItemInArray } from "@angular/cdk/drag-drop";

@Component({
  selector: 'app-param-centre',
  templateUrl: './param-centre.component.html',
  styleUrls: ['./param-centre.component.scss']
})
export class ParamCentreComponent implements OnInit {

  @Input() centreGestion: any;
  @Input() form!: FormGroup;

  dureeRecupList = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]

  viseurForm: FormGroup;
  columns = ['nomprenom', 'mail', 'departement', 'action'];
  enseignants: any[] = [];
  enseignant: any;
  confidentialites: any[] = [];
  etablissementConfidentialite: any;

  validationLibelles: any = {};
  validationsActives: any[] = [];

  @ViewChildren(MatExpansionPanel) pannels!: QueryList<MatExpansionPanel>;

  @Output() update = new EventEmitter<any>();

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private ldapService: LdapService,
    private enseignantService: EnseignantService,
    private configService: ConfigService,
    ) {
      this.viseurForm = this.fb.group({
        nom: [null, []],
        prenom: [null, []],
      });
    }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.validationLibelles.validationPedagogique = response.validationPedagogiqueLibelle;
      this.validationLibelles.validationConvention = response.validationAdministrativeLibelle;
      if (this.centreGestion.id) {
        this.setFormData();
      }
    });
    this.viseurForm.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      this.search();
    });
    this.getConfidentialites();
    this.getEtablissementConfidentialite();

    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      this.form.get(validation)?.valueChanges.subscribe(val => {
        if (val) {
          this.addValidation(validation);
        } else {
          this.removeValidation(validation);
          // On désactive la vérification administrative
          if (validation === 'validationPedagogique') {
            this.form.get('verificationAdministrative')?.setValue(false);
          }
        }
      });
    }
  }

  getConfidentialites() {
    this.centreGestionService.getConfidentialites().subscribe((response: any) => {
      this.confidentialites = response;
      if (this.centreGestion.id && this.centreGestion.niveauCentre.libelle !== 'ETABLISSEMENT') {
        this.confidentialites.pop();
      }
    });
  }

  getEtablissementConfidentialite() {
    this.centreGestionService.getEtablissementConfidentialite().subscribe((response: any) => {
      this.etablissementConfidentialite = response;
      if (this.centreGestion.id && this.centreGestion.niveauCentre.libelle !== 'ETABLISSEMENT' && this.etablissementConfidentialite.code != 2) {
        this.form.get('codeConfidentialite')?.disable();
      }
    });
  }

  setFormData(): void {
    this.form.setValue({
      codeConfidentialite: this.centreGestion.codeConfidentialite,
      saisieTuteurProParEtudiant: this.centreGestion.saisieTuteurProParEtudiant,
      autoriserImpressionConvention: this.centreGestion.autoriserImpressionConvention,
      conditionValidationImpression: this.centreGestion.conditionValidationImpression,
      autorisationEtudiantCreationConvention: this.centreGestion.autorisationEtudiantCreationConvention,
      validationPedagogique: this.centreGestion.validationPedagogique,
      verificationAdministrative: this.centreGestion.verificationAdministrative,
      validationConvention: this.centreGestion.validationConvention,
      validationPedagogiqueOrdre: this.centreGestion.validationPedagogiqueOrdre,
      verificationAdministrativeOrdre: this.centreGestion.verificationAdministrativeOrdre,
      validationConventionOrdre: this.centreGestion.validationConventionOrdre,
      recupInscriptionAnterieure: this.centreGestion.recupInscriptionAnterieure,
      dureeRecupInscriptionAnterieure: this.centreGestion.dureeRecupInscriptionAnterieure ?? 1,
      urlPageInstruction: this.centreGestion.urlPageInstruction,
      nomViseur: this.centreGestion.nomViseur,
      prenomViseur: this.centreGestion.prenomViseur,
      mailViseur: this.centreGestion.mailViseur,
      qualiteViseur: this.centreGestion.qualiteViseur,
      delaiAlerteConvention: this.centreGestion.delaiAlerteConvention,
      onlyMailCentreGestion: this.centreGestion.onlyMailCentreGestion,
    }, {
      emitEvent: false,
    });
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      if (this.centreGestion[validation]) {
        this.validationsActives.push({ id: validation, ordre: this.centreGestion[validation + 'Ordre'], libelle: this.validationLibelles[validation] ?? 'Vérification administrative'})
      }
    }
    if (this.validationsActives.length > 1) {
      this.validationsActives.sort((a, b) => {
        return a.ordre - b.ordre;
      });
    }
  }

  toggleRecupInscription(): void {
    if (!this.form.get('recupInscriptionAnterieure')?.value) {
      this.form.get('dureeRecupInscriptionAnterieure')?.setValue(1); // Limite de durée par défaut à 1 mois
    }
  }

  search(): void {
    if (!this.viseurForm.get('nom')?.value && !this.viseurForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.enseignant = undefined;
    this.ldapService.searchUsersByName(this.viseurForm.value.nom, this.viseurForm.value.prenom).subscribe((response: any) => {
      this.enseignants = response;
      if (this.enseignants.length === 1) {
        this.choose(this.enseignants[0]);
      }
    });
  }

  choose(row: any): void {
      if (this.pannels) {
        this.pannels.first.open();
      }
      this.enseignantService.getByUid(row.supannAliasLogin).subscribe((response: any) => {
        this.enseignant = response;
        if (this.enseignant == null){
          this.createEnseignant(row);
        }else{
          this.updateEnseignant(this.enseignant.id, row);
        }
      });
  }

  createEnseignant(row: any): void {
    const data = {
      "nom": row.sn.join(' '),
      "prenom": row.givenName.join(' '),
      "mail": row.mail,
      "typePersonne": row.eduPersonPrimaryAffiliation,
      "uidEnseignant": row.uid,
      "tel": row.telephoneNumber,
    };

    this.enseignantService.create(data).subscribe((response: any) => {
      this.enseignant = response;
      this.setViseur(this.enseignant);
    });
  }

  updateEnseignant(id: number, row: any): void {
    const data = {
      nom: row.sn.join(' '),
      prenom: row.givenName.join(' '),
      mail: row.mail,
      typePersonne: row.eduPersonPrimaryAffiliation,
      uidEnseignant: row.uid,
      tel: row.telephoneNumber,
    };

    this.enseignantService.update(id, data).subscribe((response: any) => {
      this.enseignant = response;
      this.setViseur(this.enseignant);
    });
  }

  setViseur(enseignant: any) {
    this.form.get('nomViseur')?.setValue(enseignant.nom);
    this.form.get('prenomViseur')?.setValue(enseignant.prenom);
    this.form.get('mailViseur')?.setValue(enseignant.mail);
    this.form.get('qualiteViseur')?.reset();
  }

  resetViseur() {
    this.form.get('nomViseur')?.reset();
    this.form.get('prenomViseur')?.reset();
    this.form.get('mailViseur')?.reset();
    this.form.get('qualiteViseur')?.reset();
  }

  compareCode(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

  dropValidation(event: CdkDragDrop<string[]>): void {
    moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    this.reorderValidations();
  }

  reorderValidations(): void {
    let ordre = 1;
    this.validationsActives.map((v: any) => v.ordre = ordre++);
    let lastOrdre = 0;
    this.validationsActives.forEach((v: any) => {
      this.form.get(v.id + 'Ordre')?.setValue(v.ordre);
      lastOrdre = v.ordre;
    });
    for (let validation of ['validationPedagogique', 'verificationAdministrative', 'validationConvention']) {
      if (!this.form.get(validation)?.value) {
        this.form.get(validation + 'Ordre')?.setValue(++lastOrdre);
      }
    }
  }

  addValidation(validation: string): void {
    const ordres = this.validationsActives.map((v: any) => v.ordre);
    let lastOrdre = 0;
    if (ordres.length > 0) {
      lastOrdre = Math.max.apply(Math, ordres);
    }
    this.validationsActives.push({ id: validation, ordre: lastOrdre + 1, libelle: this.validationLibelles[validation] ?? 'Vérification administrative'})
    this.reorderValidations();
  }

  removeValidation(validation: string): void {
    const index = this.validationsActives.findIndex((v: any) => v.id === validation);
    if (index > -1) {
      this.validationsActives.splice(index, 1);
      this.reorderValidations();
    }
  }

}
