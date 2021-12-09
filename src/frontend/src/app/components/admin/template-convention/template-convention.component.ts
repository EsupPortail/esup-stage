import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { TemplateConventionService } from "../../../services/template-convention.service";
import { ParamConventionService } from "../../../services/param-convention.service";
import { MessageService } from "../../../services/message.service";
import { AppFonction } from "../../../constants/app-fonction";
import { Droit } from "../../../constants/droit";
import { AuthService } from "../../../services/auth.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import * as Editor from '../../../../custom-ck5/ckeditor';

@Component({
  selector: 'app-template-convention',
  templateUrl: './template-convention.component.html',
  styleUrls: ['./template-convention.component.scss']
})
export class TemplateConventionComponent implements OnInit {

  public Editor = Editor;
  public onReady(editor: any) {
    editor.ui.getEditableElement().parentElement.insertBefore(
      editor.ui.view.toolbar.element,
      editor.ui.getEditableElement()
    );
  }

  columns = ['typeConvention.libelle', 'langueConvention.code', 'action'];
  sortColumn = 'typeConvention.libelle';

  paramColumns = ['code', 'libelle', 'exemple'];
  paramSortColumn = 'code';

  editTabIndex = 1;
  data: any = {};
  form: FormGroup;

  @ViewChild('tableList') appTable: TableComponent | undefined;
  @ViewChild('paramTableList') appParamTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public templateConventionService: TemplateConventionService,
    public paramConventionService: ParamConventionService,
    public messageService: MessageService,
    private authService: AuthService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      typeConvention: [null, [Validators.required]],
      langueConvention: [null, [Validators.required]],
      texte: [null],
      texteAvenant: [null],
    });
    this.form.get('typeConvention')?.disable();
    this.form.get('langueConvention')?.disable();
  }

  ngOnInit(): void {
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.editTabIndex) {
      this.data = {};
      this.form.reset();
    }
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  edit(row: any): void {
    this.data = row;
    this.form.patchValue({
      typeConvention: this.data.typeConvention,
      langueConvention: this.data.langueConvention,
      texte: this.data.texte,
      texteAvenant: this.data.texteAvenant
    });
    if (this.tabs) {
      this.tabs.selectedIndex = this.editTabIndex;
    }
  }

  save(): void {
    if (this.form.valid) {
      const data = {...this.form.value};
      delete data.typeConvention;
      delete data.langueConvention;
      this.templateConventionService.update(this.data.id, data).subscribe((response: any) => {
        this.data = response;
        this.messageService.setSuccess('Template convention modifié');
        this.appTable?.update();
      });
    }
  }

}
