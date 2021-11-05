import { Component, OnInit, ViewChild } from '@angular/core';
import { StructureService } from "../../services/structure.service";
import { PaysService } from "../../services/pays.service";
import { TypeStructureService } from "../../services/type-structure.service";
import { NafN1Service } from "../../services/naf-n1.service";
import { NafN5Service } from "../../services/naf-n5.service";
import { StatutJuridiqueService } from "../../services/statut-juridique.service";
import { MessageService } from "../../services/message.service";
import { AuthService } from "../../services/auth.service";
import { TableComponent } from "../table/table.component";
import { AppFonction } from "../../constants/app-fonction";
import { Droit } from "../../constants/droit";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";

@Component({
  selector: 'app-gestion-etab-accueil',
  templateUrl: './gestion-etab-accueil.component.html',
  styleUrls: ['./gestion-etab-accueil.component.scss']
})
export class GestionEtabAccueilComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5', 'pays', 'commune', 'typeStructure', 'statutJuridique', 'action'];
  sortColumn = 'raisonSociale';
  filters: any[] = [];

  // createButton = {
  //   libelle: 'Créer un établissement d\'accueil',
  //   action: () => this.initCreate(),
  // }
  formTabIndex = 1;
  data: any = {};

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public structureService: StructureService,
    private paysService: PaysService,
    private typeStructureService: TypeStructureService,
    private nafN1Service: NafN1Service,
    private nafN5Service: NafN5Service,
    private statutJuridiqueService: StatutJuridiqueService,
    private messageService: MessageService,
    private authService: AuthService,
  ) { }

  ngOnInit(): void {
    this.filters = [
      { id: 'raisonSociale', libelle: 'Raison sociale' },
      { id: 'numeroSiret', libelle: 'Numéro SIRET' },
      { id: 'nafN1.code', libelle: 'Activité', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'code', specific: true },
      { id: 'pays.id', libelle: 'Pays', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'commune', libelle: 'Commune' },
      { id: 'typeStructure.id', libelle: 'Type d\'organisme', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
      { id: 'statutJuridique.id', libelle: 'Forme juridique', type: 'list', options: [], keyLibelle: 'libelle', keyId: 'id' },
    ];
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({temEnServPays: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'pays.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'typeStructure.id');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.nafN1Service.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'nafN1.code');
      if (filter) {
        filter.options = response.data;
      }
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      const filter = this.filters.find((f: any) => f.id === 'statutJuridique.id');
      if (filter) {
        filter.options = response.data;
      }
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.formTabIndex) {
      this.data = {}
    }
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.CREATION]});
  }

  canEdit(): boolean {
    const hasRight = this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.MODIFICATION]});
    return !this.authService.isEtudiant() && hasRight;
  }

  edit(row: any): void {
    if (this.tabs) {
      this.tabs.selectedIndex = this.formTabIndex;
    }
    this.data = row;
  }

}
