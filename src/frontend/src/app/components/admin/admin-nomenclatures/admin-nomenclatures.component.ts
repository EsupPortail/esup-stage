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
import { MessageService } from "../../../services/message.service";
import { TableComponent } from "../../table/table.component";
import { AdminNomenclaturesEditionComponent } from './admin-nomenclatures-edition/admin-nomenclatures-edition.component';
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";

@Component({
  selector: 'app-admin-nomenclatures',
  templateUrl: './admin-nomenclatures.component.html',
  styleUrls: ['./admin-nomenclatures.component.scss']
})
export class AdminNomenclaturesComponent implements OnInit {

  columns = ['id', 'libelle', 'action'];
  sortColumn = 'libelle';
  filters = [
    { id: 'id', libelle: 'Id' },
    { id: 'libelle', libelle: 'Libellé' },
  ];
  nomenclatures = [
    { key: 'id', label: 'Type Convention', service: this.typeConventionService, tableIndex: 0 },
    { key: 'code', label: 'Langue Convention', service: this.langueConventionService, tableIndex: 1 },
    { key: 'id', label: 'Thème', service: this.themeService, tableIndex: 2 },
    { key: 'id', label: 'Temps Travail', service: this.tempsTravailService, tableIndex: 3 },
    { key: 'id', label: 'Fréquence de versement', service: this.uniteDureeService, tableIndex: 4 },
    { key: 'id', label: 'Type de gratification', service: this.uniteGratificationService, tableIndex: 5 },
    { key: 'id', label: 'Modalité de paiement', service: this.modeVersGratificationService, tableIndex: 6 },
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
    public matDialog: MatDialog,
    private messageService: MessageService,
  ) { }

  ngOnInit(): void {
  }

  tabChanged(event: MatTabChangeEvent): void {
  }

  openEditionModal(service: any, data: any, tableIndex: number) {
    this.data = data;
    const dialogConfig = new MatDialogConfig();
    dialogConfig.data = {service: service, data: this.data};
    dialogConfig.width = "50%";
    const modalDialog = this.matDialog.open(AdminNomenclaturesEditionComponent, dialogConfig);
    modalDialog.afterClosed().subscribe(dialogResponse => {
      if (dialogResponse == true) {
        if (this.appTables)
          this.appTables.toArray()[tableIndex].update();
        this.messageService.setSuccess("Modification effectuée");
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
