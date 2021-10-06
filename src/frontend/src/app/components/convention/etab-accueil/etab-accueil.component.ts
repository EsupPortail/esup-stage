import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { SecteurActiviteService } from "../../../services/secteur-activite.service";

@Component({
  selector: 'app-etab-accueil',
  templateUrl: './etab-accueil.component.html',
  styleUrls: ['./etab-accueil.component.scss']
})
export class EtabAccueilComponent implements OnInit {

  searchType = 1;
  formRaisonSociale: FormGroup;
  formSiret: FormGroup;
  formActivite: FormGroup;
  formTel: FormGroup;
  formAdresse: FormGroup;
  formService: FormGroup;
  errorAtLeastOne = false;

  countries: any[] = [];
  typeStructures: any[] = [];
  secteurActivites: any[] = [];

  @Output() validated = new EventEmitter<number>();

  constructor(private fb: FormBuilder,
              private paysService: PaysService,
              private typeStructureService: TypeStructureService,
              private secteurActiviteService: SecteurActiviteService
  ) {
    this.paysService.getPaginated(1, 0, 'lib', 'asc', '{}').subscribe((response: any) => {
      this.countries = response.data;
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
      this.typeStructures = response.data;
    });
    this.secteurActiviteService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
      this.secteurActivites = response.data;
    });
    this.formRaisonSociale = this.fb.group({
      raisonSociale: [null, []],
      pays: [null, []],
    });
    this.formSiret = this.fb.group({
      siret: [null, []],
      siren: [null, []],
    });
    this.formActivite = this.fb.group({
      typeEtablissement: [null, [Validators.required]],
      secteur: [null, [Validators.required]],
      departement: [null, []],
    });
    this.formTel = this.fb.group({
      tel: [null, []],
      fax: [null, []],
    });
    this.formAdresse = this.fb.group({
      voie: [null, []],
      batiment: [null, []],
      ville: [null, []],
      cp: [null, []],
      pays: [null, []],
    });
    this.formService = this.fb.group({
      service: [null, [Validators.required]],
      departement: [null, []],
    });
  }

  ngOnInit(): void {
  }

  reset(): void {
    this.errorAtLeastOne = false;
  }

  search(): void {
    this.errorAtLeastOne = false;
    switch (this.searchType) {
      case 1:
        this.formRaisonSociale.markAllAsTouched();
        break;
      case 2:
        this.formSiret.markAllAsTouched();
        this.checkAtLeastOne(this.formSiret);
        break;
      case 3:
        this.formActivite.markAllAsTouched();
        break;
      case 4:
        this.formTel.markAllAsTouched();
        this.checkAtLeastOne(this.formTel);
        break;
      case 5:
        this.formAdresse.markAllAsTouched();
        this.checkAtLeastOne(this.formAdresse);
        break;
      case 6:
        this.formService.markAllAsTouched();
        break;
      default:
        break;
    }
    if (this.errorAtLeastOne) {
      return;
    }
  }

  checkAtLeastOne(form: FormGroup): void {
    let hasValue = false;
    Object.keys(form.controls).forEach((key: string) => {
      if (form.get(key)?.value) {
        hasValue = true;
      }
    });
    if (!hasValue) {
      this.errorAtLeastOne = true;
    }
  }

  validate(): void {
    this.validated.emit(1); // TODO get status from form completion
  }

}
