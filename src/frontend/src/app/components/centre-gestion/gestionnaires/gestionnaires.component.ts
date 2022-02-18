import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PersonnelCentreService } from "../../../services/personnel-centre.service";
import { CiviliteService } from "../../../services/civilite.service";
import { MessageService } from "../../../services/message.service";
import { LdapService } from "../../../services/ldap.service";
import { EnseignantService } from "../../../services/enseignant.service";
import { ConfigService } from "../../../services/config.service";
import { UserService } from "../../../services/user.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { TableComponent } from "../../table/table.component";
import { debounceTime } from "rxjs/operators";

@Component({
  selector: 'app-gestionnaires',
  templateUrl: './gestionnaires.component.html',
  styleUrls: ['./gestionnaires.component.scss']
})
export class GestionnairesComponent implements OnInit {

  @Input() centreGestion: any;
  @Output() refreshPersonnelsCentre = new EventEmitter<any>();

  form: FormGroup;
  searchForm: FormGroup;

  droits: any;
  civilites: any;

  columns = ['civilite.libelle', 'nom', 'prenom', 'droitAdministration.libelle', 'alertesMail', 'droitsEvaluation', 'action'];
  sortColumn = 'nom';
  filters: any[] = [
    { id: 'nom', libelle: 'Nom'},
    { id: 'prenom', libelle: 'Prénom'},
  ];
  exportColumns = {
    civilite: { title: 'Civilité' },
    nom: { title: 'Nom' },
    prenom: { title: 'Prénom' },
    droitAdministration: { title: 'Droit d\'accès aux conventions' },
    alertesMail: { title: 'Alertes mail' },
    droitsEvaluation: { title: 'Droits évaluation' },
  };

  searchColumns = ['nomprenom', 'mail', 'departement', 'action'];
  gestionnaires: any[] = [];
  gestionnaire: any;

  alertesMail = [
    {id: 'creationConventionEtudiant', libelle: 'Création d\'une convention par l’étudiant'},
    {id: 'modificationConventionEtudiant', libelle: 'Modification d\'une convention par l’étudiant'},
    {id: 'creationConventionGestionnaire', libelle: 'Création d\'une convention par le gestionnaire'},
    {id: 'modificationConventionGestionnaire', libelle: 'Modification d\'une convention par le gestionnaire'},
    {id: 'creationAvenantEtudiant', libelle: 'Création d\'un avenant par l’étudiant'},
    {id: 'modificationAvenantEtudiant', libelle: 'Modification d\'un avenant par l’étudiant'},
    {id: 'creationAvenantGestionnaire', libelle: 'Création d\'un avenant par le gestionnaire'},
    {id: 'modificationAvenantGestionnaire', libelle: 'Modification d\'un avenant par le gestionnaire'},
    {id: 'validationPedagogiqueConvention', libelle: 'Validation pédagogique d\'une convention'},
    {id: 'validationAdministrativeConvention', libelle: 'Validation administrative d\'une convention'},
    {id: 'validationAvenant', libelle: 'Validation d\'un avenant '},
  ];

  configAlertes: any;
  selectedConfig: any;
  toggleAlertes = true;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChildren(MatExpansionPanel) pannels: QueryList<MatExpansionPanel>;

  constructor(
    private fb: FormBuilder,
    public personnelCentreService: PersonnelCentreService,
    private messageService: MessageService,
    private ldapService: LdapService,
    private enseignantService: EnseignantService,
    private civiliteService: CiviliteService,
    private configService: ConfigService,
    private userService: UserService
  ) {
    this.form = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
      mail: [null, []],
      tel: [null, []],
      fax: [null, []],
      civilite: [null, []],
      typePersonne: [null, []],
      uidPersonnel: [null, []],
      fonction: [null, []],
      droitAdministration: [null, []],
      impressionConvention: [null, []],
      campus: [null, []],
      batiment: [null, []],
      bureau: [null, []],
      codeAffectation: [null, []],
      alertesMail: [null, []],
      droitEvaluationEtudiant: [null, []],
      droitEvaluationEnseignant: [null, []],
      droitEvaluationEntreprise: [null, []],
      creationConventionEtudiant: [null, []],
      modificationConventionEtudiant: [null, []],
      creationConventionGestionnaire: [null, []],
      modificationConventionGestionnaire: [null, []],
      creationAvenantEtudiant: [null, []],
      modificationAvenantEtudiant: [null, []],
      creationAvenantGestionnaire: [null, []],
      modificationAvenantGestionnaire: [null, []],
      validationPedagogiqueConvention: [null, []],
      validationAdministrativeConvention: [null, []],
      validationAvenant: [null, []],
    });
    this.searchForm = this.fb.group({
      nom: [null, []],
      prenom: [null, []],
    });
  }

  ngOnInit(): void {
    this.personnelCentreService.getDroitsAdmin().subscribe((response: any) => {
      this.droits = response;
    });
    this.civiliteService.getPaginated(1, 0, 'libelle', 'asc','').subscribe((response: any) => {
      this.civilites = response;
    });
    this.configService.getConfigAlerteMail().subscribe((response: any) => {
      this.configAlertes = response;
    });
    if (this.centreGestion.id) {
      this.filters.push({id: 'centreGestion.id', value: this.centreGestion.id, type: 'int', hidden: true});
    }
    this.searchForm.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      this.search();
    });
  }

  search(): void {
    if (!this.searchForm.get('nom')?.value && !this.searchForm.get('prenom')?.value) {
      this.messageService.setError(`Veuillez renseigner au moins l'un des critères`);
      return;
    }
    this.gestionnaire = undefined;
    this.ldapService.searchUsersByName(this.searchForm.get('nom')?.value, this.searchForm.get('prenom')?.value).subscribe((response: any) => {
      this.gestionnaires = response;
      if (this.gestionnaires.length === 1) {
        this.choose(this.gestionnaires[0]);
      }
    });
  }

  choose(row: any): void {
    let selectedUser;
    this.gestionnaire = row;
    this.form.reset();
    this.setData(this.gestionnaire);
    this.userService.findOneByLogin(this.gestionnaire.supannAliasLogin).subscribe((response: any) => {
      selectedUser = response;
      if (selectedUser != null) {
        if (selectedUser.roles.some((r: any) => r.code === "RESP_GES")) {
          this.selectedConfig = this.configAlertes.alerteRespGestionnaire;
        } else {
          this.selectedConfig = this.configAlertes.alerteGestionnaire;
        }
      } else {
        this.selectedConfig = this.configAlertes.alerteGestionnaire;
      }
      this.form.patchValue(this.selectedConfig)
    });
  }

  setData(gestionnaire: any) {
    const displayName = gestionnaire.displayName.split(/(\s+)/);
    let civiliteGest = this.setCivilite(gestionnaire.supannCivilite);
    this.form.patchValue({
      nom: displayName[2],
      prenom: displayName[0],
      mail: gestionnaire.mail,
      civilite: civiliteGest,
      typePersonne: gestionnaire.eduPersonPrimaryAffiliation,
      uidPersonnel: gestionnaire.supannAliasLogin,
      codeAffectation: '',
      impressionConvention: true,
      droitEvaluationEtudiant: false,
      droitEvaluationEnseignant: false,
      droitEvaluationEntreprise: false,
      droitAdministration: this.droits[0],
    });
  }

  setCivilite(civilite: any) {
    switch (civilite) {
      case 'Mlle':
        return this.civilites.data.find((c: any) => c.id == 1);
      case 'Mme':
        return this.civilites.data.find((c: any) => c.id == 2);
      case 'M.':
        return this.civilites.data.find((c: any) => c.id == 3);
    }
  }

  edit(gestionnaire: any) {
    if (this.pannels) {
      this.pannels.last.open();
    }
    this.gestionnaire = gestionnaire;
    this.form.reset();
    this.form.patchValue(this.gestionnaire);
    for (let alerte of this.alertesMail) {
      if (this.form.get(alerte.id)?.value == true) {
        this.toggleAlertes = false;
        break;
      }
    }
  }

  save(): void {
    const data = {...this.form.value}
    if (this.form.invalid) {
      this.messageService.setError("Veuillez remplir les champs obligatoires");
      return;
    }
    // On met alerteMail à true si au moins une alerte est activée
    data.alertesMail = this.hasAlertesMail(data);
    if (this.gestionnaire.id) {
      this.personnelCentreService.update(data, this.gestionnaire.id).subscribe((response: any) => {
        this.messageService.setSuccess("Personnel mis à jour");
        if (this.pannels) {
          this.pannels.last.open();
        }
        this.gestionnaire = undefined;
        this.refreshPersonnelsCentre.emit();
        if (this.appTable)
          this.appTable.update();
      });
    } else {
      this.personnelCentreService.create(data, this.centreGestion.id).subscribe((response: any) => {
        this.messageService.setSuccess("Personnel rattaché");
        if (this.pannels) {
          this.pannels.last.open();
        }
        this.gestionnaire = undefined;
        this.refreshPersonnelsCentre.emit();
        if (this.appTable)
          this.appTable.update();
      });
    }
  }

  delete(id: number) {
    this.personnelCentreService.delete(id).subscribe((response: any) => {
      this.messageService.setSuccess("Personnel supprimé");
      this.gestionnaire = undefined;
      this.refreshPersonnelsCentre.emit();
      if (this.appTable)
        this.appTable.update();
    });
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  toggleAll() {
    for (let alerte of this.alertesMail) {
      this.form.get(alerte.id)?.setValue(this.toggleAlertes);
    }
    this.toggleAlertes = !this.toggleAlertes;
  }

  hasAlertesMail(gestionnaire: any) {
    if (gestionnaire.creationConventionEtudiant == true
    || gestionnaire.modificationConventionEtudiant == true
    || gestionnaire.creationConventionGestionnaire == true
    || gestionnaire.modificationConventionGestionnaire == true
    || gestionnaire.creationAvenantEtudiant == true
    || gestionnaire.modificationAvenantEtudiant == true
    || gestionnaire.creationAvenantGestionnaire == true
    || gestionnaire.modificationAvenantGestionnaire == true
    || gestionnaire.validationPedagogiqueConvention == true
    || gestionnaire.validationAdministrativeConvention == true
    || gestionnaire.validationAvenant == true) {
      return true;
    }
    return false;
  }

}
