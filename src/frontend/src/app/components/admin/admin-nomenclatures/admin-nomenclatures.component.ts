import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatTable } from "@angular/material/table";
import { TypeConventionService } from "../../../services/type-convention.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
import { TempsTravailService } from "../../../services/temps-travail.service";
import { MessageService } from "../../../services/message.service";
import { TableComponent } from "../../table/table.component";

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
    { key: 'id', label: 'Type Convention', service: this.typeConventionService },
    { key: 'code', label: 'Langue Convention', service: this.langueConventionService },
    { key: 'id', label: 'Temps Travail', service: this.tempsTravailService}
  ];

  formTabIndex = 1;
  data: any;
  form: FormGroup;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public typeConventionService: TypeConventionService,
    public langueConventionService: LangueConventionService,
    public tempsTravailService: TempsTravailService,
    private fb: FormBuilder,
    private messageService: MessageService,
  ) {
    this.form = this.fb.group({
      libelle: [null, [Validators.maxLength(100)]],
    });
    this.emptyData();
  }

  ngOnInit(): void {
  }

  emptyData(): void {
    this.data = {
      libelle: null
    };
    this.setFormData();
  }

  setFormData(): void {
    const data = {...this.data};
    delete data.id;
    delete data.codeCtrl;
    delete data.temEnServ;
    delete data.modifiable;
    this.form.setValue(data);
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.emptyData();
  }

}
