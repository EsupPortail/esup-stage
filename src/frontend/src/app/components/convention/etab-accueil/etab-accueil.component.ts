import { Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { SecteurActiviteService } from "../../../services/secteur-activite.service";
import { StructureService } from "../../../services/structure.service";
import { TableComponent } from "../../table/table.component";
import { MatTabGroup } from "@angular/material/tabs";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";

@Component({
  selector: 'app-etab-accueil',
  templateUrl: './etab-accueil.component.html',
  styleUrls: ['./etab-accueil.component.scss']
})
export class EtabAccueilComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5', 'pays', 'commune', 'typeStructure', 'statutJuridique'];
  sortColumn = 'raisonSociale';
  filters: any[] = [];

  formTabIndex = 1;
  data: any;

  countries: any[] = [];
  typeStructures: any[] = [];
  secteurActivites: any[] = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  @Output() validated = new EventEmitter<number>();

  constructor(public structureService: StructureService,
              private paysService: PaysService,
              private typeStructureService: TypeStructureService,
              private secteurActiviteService: SecteurActiviteService,
              private statutJuridiqueService: StatutJuridiqueService,
  ) {
  }

  ngOnInit(): void {
    this.filters = [
      { id: 'raisonSociale', libelle: 'Raison sociale' },
      { id: 'numeroSiret', libelle: 'Numéro SIRET' },
      { id: 'nafN1.id', libelle: 'Activité', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id', specific: true },
      { id: 'pays.id', libelle: 'Pays', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'commune', libelle: 'Commune' },
      { id: 'typeStructure.id', libelle: 'Type d\'organisme', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'statutJuridique.id', libelle: 'Forme juridique', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
    ];
    this.paysService.getPaginated(1, 0, 'lib', 'asc', '{}').subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'pays.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'typeStructure.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.secteurActiviteService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'nafN1.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', '{}').subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'statutJuridique.id');
      if (filter) {
        filter.options = response.data;
      }
    });
  }

}
