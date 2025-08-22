import {Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { NafN1Service } from "../../../services/naf-n1.service";
import { StructureService } from "../../../services/structure.service";
import { TableComponent } from "../../table/table.component";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";
import { MatExpansionPanel } from "@angular/material/expansion";
import { MessageService } from "../../../services/message.service";
import { NafN5Service } from "../../../services/naf-n5.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { ConfigService } from "../../../services/config.service";
import {ContenuService} from "../../../services/contenu.service";
import {MatDialog} from "@angular/material/dialog";


@Component({
  selector: 'app-etab-accueil',
  templateUrl: './etab-accueil.component.html',
  styleUrls: ['./etab-accueil.component.scss']
})
export class EtabAccueilComponent implements OnInit {

  columns = ['raisonSociale', 'numeroSiret', 'nafN5', 'pays', 'commune', 'typeStructure', 'statutJuridique', 'action'];
  sortColumn = 'raisonSociale';
  filters: any[] = [];

  formTabIndex = 1;
  data: any;

  createButton = {
    libelle: 'Créer un établissement',
    action: () => this.initCreate(),
  }

  @Input() etab: any;
  modif: boolean = false;
  selectedRow: any = undefined;

  @Input() modifiable!: boolean;

  autorisationModification = false;
  autorisationCreation=false;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild(MatExpansionPanel) firstPanel: MatExpansionPanel|undefined;
  @ViewChild('confirmDialog') confirmDialog!: TemplateRef<any>;

  @Output() validated = new EventEmitter<any>();
  @Output() cbyChange = new EventEmitter<any>();
  @Output() cbnChange = new EventEmitter<any>();

  constructor(public structureService: StructureService,
              private paysService: PaysService,
              private typeStructureService: TypeStructureService,
              private nafN1Service: NafN1Service,
              private nafN5Service: NafN5Service,
              private statutJuridiqueService: StatutJuridiqueService,
              private messageService: MessageService,
              private contenuService: ContenuService,
              private authService: AuthService,
              private configService: ConfigService,
              private dialog: MatDialog,
  ) { }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe((response: any) => {
      this.autorisationModification = response.autoriserEtudiantAModifierEntreprise;
      this.autorisationCreation = response.autoriserEtudiantACreerEntreprise;
    });
    this.contenuService.get('BOUTON_CREER_ETAB_ACCUEIL').subscribe((response: any) => {
      this.createButton.libelle = response.texte
    });
    this.filters = [
      { id: 'raisonSociale', libelle: 'Raison sociale' },
      { id: 'numeroSiret', libelle: 'Numéro SIRET' },
      { id: 'numeroRNE', libelle: 'Numéro UAI' },
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

  canCreate(): boolean {
    let hasRight = this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
    if (this.authService.isEtudiant() && !this.autorisationCreation) {
      hasRight = false;
    }
    return this.modifiable && hasRight;
  }

  canEdit(): boolean {
    let hasRight = this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.MODIFICATION]});
    if (this.authService.isEtudiant() && !this.autorisationModification) {
      hasRight = false;
    }
    return this.modifiable && hasRight;
  }

  choose(row: any): void {
    this.modif = false;
    this.selectedRow = row;
    this.structureService.getOrCreate(row).subscribe((response:any)=>{
        this.etab = response;
        if (this.firstPanel) {
          this.firstPanel.expanded = false;
        }
        this.validated.emit(this.etab);
    })
  }

  initCreate(): void {
    this.etab = {};
    this.modif = true;
    this.selectedRow = undefined;
  }

  edit(): void {
    this.modif = true;
  }

  // changeOnglet(): void {
  //   if (this.firstPanel) {
  //     this.firstPanel.expanded = true;
  //   }
  // }


}
