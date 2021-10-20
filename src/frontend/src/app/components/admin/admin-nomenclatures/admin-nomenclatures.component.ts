import { Component, OnInit, ViewChild, ViewChildren, QueryList } from '@angular/core';
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatTable } from "@angular/material/table";
import { AuthService } from "../../../services/auth.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import { TempsTravailService } from "../../../services/temps-travail.service";
import { ThemeService } from "../../../services/theme.service";
import { UniteDureeService } from "../../../services/unite-duree.service";
import { UniteGratificationService } from "../../../services/unite-gratification.service";
import { ModeVersGratificationService } from "../../../services/mode-vers-gratification.service";
import { ModeValidationStageService } from "../../../services/mode-validation-stage.service";
import { NiveauFormationService } from "../../../services/niveau-formation.service";
import { OrigineStageService } from "../../../services/origine-stage.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";
import { TypeOffreService } from "../../../services/type-offre.service";
import { ContratOffreService } from "../../../services/contrat-offre.service";
import { PaysService } from "../../../services/pays.service";
import { DeviseService } from "../../../services/devise.service";
import { MessageService } from "../../../services/message.service";
import { TableComponent } from "../../table/table.component";
import { AdminNomenclaturesEditionComponent } from './admin-nomenclatures-edition/admin-nomenclatures-edition.component';
import { AdminNomenclaturesCreationComponent } from './admin-nomenclatures-creation/admin-nomenclatures-creation.component';
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";

@Component({
  selector: 'app-admin-nomenclatures',
  templateUrl: './admin-nomenclatures.component.html',
  styleUrls: ['./admin-nomenclatures.component.scss']
})
export class AdminNomenclaturesComponent implements OnInit {

  columns = ['libelle', 'action'];
  sortColumn = 'libelle';

  filters = [
    { id: 'libelle', libelle: 'Libellé' },
    { id: 'temEnServ', libelle: 'Valeurs actives', type: 'temEnServ' }
  ];
  filtersPays = [
    { id: 'lib', libelle: 'Libellé' },
    { id: 'temEnServPays', libelle: 'Valeurs actives', type: 'temEnServ' }
  ];

  createButton: any = {
    libelle: 'Ajouter un élément',
    service: this.typeConventionService,
    tableIndex: 0,
    creationFormType: 2,
    action: () => this.openCreationModal()
  }

  nomenclatures = [
    { key: 'id', label: 'Type Convention', service: this.typeConventionService, tableIndex: 0, creationFormType: 2, init: false },
    { key: 'code', label: 'Langue Convention', service: this.langueConventionService, tableIndex: 1, creationFormType: 3, init: false },
    { key: 'id', label: 'Pays', service: this.paysService, tableIndex: 2, creationFormType: 3, init: false },
    { key: 'id', label: 'Thème', service: this.themeService, tableIndex: 3, creationFormType: 1, init: false },
    { key: 'id', label: 'Temps Travail', service: this.tempsTravailService, tableIndex: 4, creationFormType: 2, init: false },
    { key: 'id', label: 'Fréquence de versement', service: this.uniteDureeService, tableIndex: 5, creationFormType: 1, init: false },
    { key: 'id', label: 'Devise', service: this.deviseService, tableIndex: 6, creationFormType: 1, init: false },
    { key: 'id', label: 'Type de gratification', service: this.uniteGratificationService, tableIndex: 7, creationFormType: 1, init: false },
    { key: 'id', label: 'Modalité de paiement', service: this.modeVersGratificationService, tableIndex: 8, creationFormType: 1, init: false },
    { key: 'id', label: 'Mode de validation du stage', service: this.modeValidationStageService, tableIndex: 9, creationFormType: 1, init: false },
    { key: 'id', label: 'Niveau de formation', service: this.niveauFormationService, tableIndex: 10, creationFormType: 1, init: false },
    { key: 'id', label: 'Origine du stage', service: this.origineStageService, tableIndex: 11, creationFormType: 1, init: false },
    { key: 'id', label: 'Type de structure', service: this.typeStructureService, tableIndex: 12, creationFormType: 3, init: false },
    { key: 'id', label: 'Statut juridique', service: this.statutJuridiqueService, tableIndex: 13, creationFormType: 3, init: false },
    { key: 'id', label: "Type d'offre de stage", service: this.typeOffreService, tableIndex: 14, creationFormType: 2, init: false },
    { key: 'id', label: "Contrat du stage", service: this.contratOffreService, tableIndex: 15, creationFormType: 3, init: false },
  ];

  data: any;

  appFonctions: any[] = [];

  @ViewChildren(TableComponent) appTables: QueryList<TableComponent> | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public authService: AuthService,
    public typeConventionService: TypeConventionService,
    public langueConventionService: LangueConventionService,
    public tempsTravailService: TempsTravailService,
    public themeService: ThemeService,
    public uniteDureeService: UniteDureeService,
    public uniteGratificationService: UniteGratificationService,
    public modeVersGratificationService: ModeVersGratificationService,
    public modeValidationStageService: ModeValidationStageService,
    public niveauFormationService: NiveauFormationService,
    public origineStageService: OrigineStageService,
    public typeStructureService: TypeStructureService,
    public statutJuridiqueService: StatutJuridiqueService,
    public typeOffreService: TypeOffreService,
    public contratOffreService: ContratOffreService,
    public paysService: PaysService,
    public deviseService: DeviseService,
    public matDialog: MatDialog,
    private messageService: MessageService,
  ) { }

  ngOnInit(): void {
    this.nomenclatures[0].init = true;
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.createButton.service = this.nomenclatures[event.index].service;
    this.createButton.tableIndex = event.index;
    this.createButton.creationFormType = this.nomenclatures[event.index].creationFormType;
    this.nomenclatures[event.index].init = true;
  }

  openEditionModal(service: any, data: any, tableIndex: number) {
    this.data = data;
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {service: service, data: this.data};
    const modalDialog = this.matDialog.open(AdminNomenclaturesEditionComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse == true) {
        if (this.appTables)
          this.appTables.toArray()[tableIndex].update();
        this.messageService.setSuccess("Modification effectuée");
      }
    });
  }

  openCreationModal() {
    let service = this.createButton.service;
    let tableIndex = this.createButton.tableIndex;
    let labelTable = this.nomenclatures[tableIndex].label;
    let creationFormType = this.createButton.creationFormType;
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {service: service, creationFormType: creationFormType, labelTable: labelTable};
    const modalDialog = this.matDialog.open(AdminNomenclaturesCreationComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse == true) {
        if (this.appTables)
          this.appTables.toArray()[tableIndex].update();
        this.messageService.setSuccess("Libellé ajouté");
      }
    });
  }

  setState(service: any, data: any, tableIndex: number) {
    this.data = data;
    this.data.temEnServ = (this.data.temEnServ == "O") ? "N" : "O";
    let key = this.data.id ? this.data.id : this.data.code;
    service.update(key, this.data).subscribe((response: any) => {
      this.data = response;
      if (this.appTables)
        this.appTables.toArray()[tableIndex].update();
      this.messageService.setSuccess("Modification effectuée");
    });
  }

  delete(service: any, data: any, tableIndex: number) {
    this.data = data;
    let key = this.data.id ? this.data.id : this.data.code;
    service.delete(key).subscribe((response: any) => {
      if (this.appTables)
        this.appTables.toArray()[tableIndex].update();
      this.messageService.setSuccess("Suppression effectuée");
    });
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.MODIFICATION]});
  }

  canDelete(): boolean {
    return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.SUPPRESSION]});
  }

}
