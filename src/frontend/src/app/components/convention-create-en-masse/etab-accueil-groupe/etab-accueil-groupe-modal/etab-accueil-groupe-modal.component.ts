import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { PaysService } from "../../../../services/pays.service";
import { TypeStructureService } from "../../../../services/type-structure.service";
import { NafN1Service } from "../../../../services/naf-n1.service";
import { StructureService } from "../../../../services/structure.service";
import { TableComponent } from "../../../table/table.component";
import { StatutJuridiqueService } from "../../../../services/statut-juridique.service";
import { NafN5Service } from "../../../../services/naf-n5.service";
import { Droit } from "../../../../constants/droit";
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-etab-accueil-groupe-modal',
  templateUrl: './etab-accueil-groupe-modal.component.html',
  styleUrls: ['./etab-accueil-groupe-modal.component.scss']
})
export class EtabAccueilGroupeModalComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5', 'pays', 'commune', 'typeStructure', 'statutJuridique', 'action'];
  sortColumn = 'raisonSociale';
  filters: any[] = [];

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  constructor(public structureService: StructureService,
              private paysService: PaysService,
              private typeStructureService: TypeStructureService,
              private nafN1Service: NafN1Service,
              private nafN5Service: NafN5Service,
              private statutJuridiqueService: StatutJuridiqueService,
              private dialogRef: MatDialogRef<EtabAccueilGroupeModalComponent>,
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

  close(): void {
    this.dialogRef.close(null);
  }

  choose(row: any): void {
    this.dialogRef.close(row.id);
  }

}
