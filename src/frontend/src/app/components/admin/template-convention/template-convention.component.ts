import { Component, OnInit, ViewChild } from '@angular/core';
import { TableComponent } from "../../table/table.component";
import { TemplateConventionService } from "../../../services/template-convention.service";
import { ParamConventionService } from "../../../services/param-convention.service";
import { MessageService } from "../../../services/message.service";
import { TypeConventionService } from "../../../services/type-convention.service";
import { LangueConventionService } from "../../../services/langue-convention.service";
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
  exportColumns = {
    typeConvention: { title: 'Type de convention' },
    langueConvention: { title: 'Langue de la convnention' },
  };

  paramColumns = ['code', 'libelle', 'exemple'];
  paramSortColumn = 'code';
  // TODO export de la liste des paramètres
  exportColumnsParams = {
    code: { title: 'Code' },
    libelle: { title: 'Libelle' },
    exemple: { title: 'Exemple' },
  };

  createTabIndex = 1
  editTabIndex = 2;
  data: any = {};
  form: FormGroup;

  typesConvention: any;
  languesConvention: any;
  defaultConvention: any;
  defaultAvenant: any;

  @ViewChild('tableList') appTable: TableComponent | undefined;
  @ViewChild('paramTableList') appParamTable: TableComponent | undefined;
  @ViewChild('tabs') tabs: MatTabGroup | undefined;

  constructor(
    public templateConventionService: TemplateConventionService,
    public paramConventionService: ParamConventionService,
    private messageService: MessageService,
    private typeConventionService: TypeConventionService,
    private langueConventionService: LangueConventionService,
    private authService: AuthService,
    private fb: FormBuilder,
  ) {
    this.form = this.fb.group({
      typeConvention: [null, [Validators.required]],
      langueConvention: [null, [Validators.required]],
      texte: [null],
      texteAvenant: [null],
    });
  }

  ngOnInit(): void {
    this.typeConventionService.getListActive().subscribe((response: any) => {
      this.typesConvention = response.data;
    });
    this.langueConventionService.getListActive().subscribe((response: any) => {
      this.languesConvention = response.data;
    });
    this.templateConventionService.getDefaultTemplateConvention().subscribe((response: any) => {
      this.defaultConvention = response;
    });
    this.templateConventionService.getDefaultTemplateAvenant().subscribe((response: any) => {
      this.defaultAvenant = response;
    });
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.editTabIndex) {
      this.data = {};
      this.form.reset();
      this.form.get('typeConvention')?.enable();
      this.form.get('langueConvention')?.enable();
      this.form.patchValue({
        texte: this.defaultConvention,
        texteAvenant: this.defaultAvenant
      });
    } else {
      this.form.get('typeConvention')?.disable();
      this.form.get('langueConvention')?.disable();
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
      texte: this.data.texte ?? this.defaultConvention,
      texteAvenant: this.data.texteAvenant ?? this.defaultAvenant,
    });
    if (this.tabs) {
      this.tabs.selectedIndex = this.editTabIndex;
    }
  }

  save(): void {
    if (this.form.valid) {
      const data = {...this.form.value};
      if (this.data.id) {
        delete data.typeConvention;
        delete data.langueConvention;
        this.templateConventionService.update(this.data.id, data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Template convention modifié');
          this.appTable?.update();
        });
      } else {
        this.templateConventionService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Template convention créé');
          this.appTable?.update();
          if (this.tabs) {
            this.tabs.selectedIndex = 0;
          }
        });
      }

    } else {
      this.messageService.setError("Veuillez remplir les champs obligatoires");
    }
  }

  compare(option: any, value: any): boolean {
    if (option && value) {
      return option.id === value.id;
    }
    return false;
  }

  compareCode(option: any, value: any): boolean {
    if (option && value) {
      return option.code === value.code;
    }
    return false;
  }

}
