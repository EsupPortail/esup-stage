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
      codeAffectation: ['', []],
      alerteMail: [null, []],
      droitEvaluationEtudiant: [null, []],
      droitEvaluationEnseignant: [null, []],
      droitEvaluationEntreprise: [null, []],
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
    this.ldapService.searchEnseignants(this.searchForm.value).subscribe((response: any) => {
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
      });
    } else {
      this.personnelCentreService.create(data, this.centreGestion.id).subscribe((response: any) => {
        this.messageService.setSuccess("Personnel rattaché");
        if (this.pannels) {
          this.pannels.first.open();
        }
        this.gestionnaire = undefined;
      });
    }
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

}
