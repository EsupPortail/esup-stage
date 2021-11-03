import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { TemplateMailService } from "../../../services/template-mail.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { MessageService } from "../../../services/message.service";

@Component({
  selector: 'app-template-mail',
  templateUrl: './template-mail.component.html',
  styleUrls: ['./template-mail.component.scss']
})
export class TemplateMailComponent implements OnInit {

  columns = ['code', 'libelle', 'objet', 'action'];
  sortColumn = 'code';
  filters = [
    { id: 'code', libelle: 'Code' },
    { id: 'libelle', libelle: 'Libellé' },
    { id: 'objet', libelle: 'Objet' },
    { id: 'texte', libelle: 'Texte' },
  ];
  params: any[] = [];

  editTabIndex = 1;
  data: any = {};
  form: FormGroup;

  @ViewChild('tableList') appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public templateMailService: TemplateMailService,
    private authService: AuthService,
    private fb: FormBuilder,
    private messageService: MessageService,
  ) {
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(150)]],
      libelle: [null, [Validators.required, Validators.maxLength(150)]],
      objet: [null, [Validators.required, Validators.maxLength(250)]],
      texte: [null, [Validators.required]],
    });
    this.form.get('code')?.disable();
  }

  ngOnInit(): void {
    this.templateMailService.getParams().subscribe((response: any) => {
      this.params = response;
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.editTabIndex) {
      this.data = {};
      this.form.reset();
    }
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.NOMENCLATURE, droits: [Droit.MODIFICATION]});
  }

  edit(row: any): void {
    this.data = row;
    this.form.setValue({
      code: this.data.code,
      libelle: this.data.libelle,
      objet: this.data.objet,
      texte: this.data.texte,
    });
    if (this.tabs) {
      this.tabs.selectedIndex = this.editTabIndex;
    }
  }

  save(): void {
    if (this.form.valid) {
      const data = {...this.form.value};
      delete data.code;
      this.templateMailService.update(this.data.id, data).subscribe((response: any) => {
        this.data = response;
        this.messageService.setSuccess('Template mail modifié');
        this.appTable?.update();
      });
    }
  }

}
