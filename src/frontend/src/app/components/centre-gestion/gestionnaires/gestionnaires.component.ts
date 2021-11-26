import { Component, OnInit, Input, Output, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PersonnelCentreService } from "../../../services/personnel-centre.service";
import { MessageService } from "../../../services/message.service";
import { LdapService } from "../../../services/ldap.service";
import { EnseignantService } from "../../../services/enseignant.service";
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

  form: FormGroup;
  searchForm: FormGroup;

  droits: any;

  columns = ['civilite.libelle', 'nom', 'prenom', 'droitAdministration.libelle', 'alertesMail', 'droitsEvaluation', 'action'];
  sortColumn = 'nom';
  filters: any[] = [
    { id: 'nom', libelle: 'Nom'},
    { id: 'prenom', libelle: 'Prénom'},
  ];

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

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChildren(MatExpansionPanel) pannels: QueryList<MatExpansionPanel>;

  constructor(
    private fb: FormBuilder,
    public personnelCentreService: PersonnelCentreService,
    private messageService: MessageService,
    private ldapService: LdapService,
    private enseignantService: EnseignantService,
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
      alerteMail: [null, []],
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
    if (this.pannels) {
      this.pannels.last.open();
    }
    this.gestionnaire = row;
    this.form.reset();
    this.setData(this.gestionnaire);
  }

  setData(gestionnaire: any) {
    const displayName = gestionnaire.displayName.split(/(\s+)/);
    this.form.patchValue({
      nom: displayName[2],
      prenom: displayName[0],
      mail: gestionnaire.mail,
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

  edit(gestionnaire: any) {
    if (this.pannels) {
      this.pannels.last.open();
    }
    this.gestionnaire = gestionnaire;
    this.form.reset();
    this.form.patchValue(this.gestionnaire);
  }

  save(): void {
    const data = {...this.form.value}
    if (this.form.invalid) {
      this.messageService.setError("Veuillez remplir les champs obligatoires")
      return;
    }
    if (this.gestionnaire.id) {
      this.personnelCentreService.update(data, this.gestionnaire.id).subscribe((response: any) => {
        this.messageService.setSuccess("Personnel mis à jour");
        if (this.pannels) {
          this.pannels.first.open();
        }
        this.gestionnaire = undefined;
        if (this.appTable)
          this.appTable.update();
      });
    } else {
      this.personnelCentreService.create(data, this.centreGestion.id).subscribe((response: any) => {
        this.messageService.setSuccess("Personnel rattaché");
        if (this.pannels) {
          this.pannels.first.open();
        }
        this.gestionnaire = undefined;
        if (this.appTable)
          this.appTable.update();
      });
    }
  }

  delete(id: number) {
    this.personnelCentreService.delete(id).subscribe((response: any) => {
      this.messageService.setSuccess("Personnel supprimé");
      this.gestionnaire = undefined;
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

}
