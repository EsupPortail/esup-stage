import { Component, OnInit, ViewChild } from '@angular/core';
import { ContenuService } from "../../../services/contenu.service";
import { TableComponent } from "../../table/table.component";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MessageService } from "../../../services/message.service";
import * as Editor from '../../../../custom-ck5/ckeditor';

@Component({
  selector: 'app-contenu',
  templateUrl: './contenu.component.html',
  styleUrls: ['./contenu.component.scss']
})
export class ContenuComponent implements OnInit {

  public Editor = Editor;

  columns = ['code', 'texte', 'action'];
  sortColumn = 'code';
  filters = [
    { id: 'code', libelle: 'Code' },
    { id: 'texte', libelle: 'Texte' },
  ];
  exportColumns = {
    code: { title: 'Code' },
    texte: { title: 'Texte' },
  };

  formTabIndex = 1;
  data: any;
  form!: FormGroup;

  @ViewChild(TableComponent) appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(public contenuService: ContenuService, private authService: AuthService, private fb: FormBuilder, private messageService: MessageService) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(100)]],
      libelle: [true, []],
      texte: [null, [Validators.required]],
    });
    this.form.get('code')?.disable();
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.formTabIndex) {
      this.data = undefined;
    }
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  edit(data: any): void {
    this.data = data;
    if (this.tabs) {
      this.tabs.selectedIndex = this.formTabIndex;
    }
    this.form.setValue({
      code: this.data.code,
      libelle: this.data.libelle,
      texte: this.data.texte,
    });
  }

  save(): void {
    if (this.form.valid) {
      this.contenuService.update(this.data.code, this.form.value).subscribe((response: any) => {
        this.data = response;
        this.appTable?.update();
        this.contenuService.getAllLibelle(true);
        this.messageService.setSuccess('Contenu modifié');
      });
    }
  }

}
