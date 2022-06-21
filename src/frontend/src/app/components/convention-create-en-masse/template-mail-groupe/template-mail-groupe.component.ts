import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { TemplateMailGroupeService } from "../../../services/template-mail-groupe.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { MessageService } from "../../../services/message.service";
import { MatDialog } from "@angular/material/dialog";
import { MailTesterComponent } from "../../admin/template-mail/mail-tester/mail-tester.component";

@Component({
  selector: 'app-template-mail-groupe',
  templateUrl: './template-mail-groupe.component.html',
  styleUrls: ['./template-mail-groupe.component.scss']
})
export class TemplateMailGroupeComponent implements OnInit {

  columns = ['code', 'libelle', 'objet', 'action'];
  sortColumn = 'code';
  filters = [
    { id: 'code', libelle: 'Code' },
    { id: 'libelle', libelle: 'Libellé' },
    { id: 'objet', libelle: 'Objet' },
    { id: 'texte', libelle: 'Texte' },
  ];
  params: any[] = [];
  exportColumns = {
    code: { title: 'Code' },
    libelle: { title: 'Libellé' },
    objet: { title: 'Objet' },
    texte: { title: 'Texte' },
  };

  editTabIndex = 1;
  data: any = {};
  form: FormGroup;

  @ViewChild('tableList') appTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public templateMailGroupeService: TemplateMailGroupeService,
    private authService: AuthService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private dialog: MatDialog,
  ) {
    this.form = this.fb.group({
      code: [null, [Validators.required, Validators.maxLength(150)]],
      libelle: [null, [Validators.required, Validators.maxLength(150)]],
      objet: [null, [Validators.required, Validators.maxLength(250)]],
      texte: [null, [Validators.required]],
    });
  }

  ngOnInit(): void {
    this.templateMailGroupeService.getParams().subscribe((response: any) => {
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
    return this.authService.checkRights({fonction: AppFonction.CREATION_EN_MASSE_CONVENTION, droits: [Droit.MODIFICATION]});
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
      if (this.data.id) {
        const data = {...this.form.value};
        this.templateMailGroupeService.update(this.data.id, data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Template mail modifié avec succès');
          this.appTable?.update();
        });
      }else{
        const data = {...this.form.value};
        this.templateMailGroupeService.create(data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Template mail créé avec succès');
          this.appTable?.update();
        });
      }
    }
  }

  delete(row: any): void {
    this.templateMailGroupeService.delete(row.id).subscribe((response: any) => {
      this.messageService.setSuccess('Template mail supprimé avec succès');
      this.appTable?.update();
    });
  }

  openTestSend(row: any): void {
    const dialogRef = this.dialog.open(MailTesterComponent, {
      data: row,
    });
  }

}
