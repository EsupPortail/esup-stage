import {AfterViewInit, ChangeDetectorRef, Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import { TableComponent } from '../../table/table.component';
import { TemplateConventionService } from '../../../services/template-convention.service';
import { ParamConventionService } from '../../../services/param-convention.service';
import { MessageService } from '../../../services/message.service';
import { TypeConventionService } from '../../../services/type-convention.service';
import { LangueConventionService } from '../../../services/langue-convention.service';
import { AppFonction } from '../../../constants/app-fonction';
import { Droit } from '../../../constants/droit';
import { AuthService } from '../../../services/auth.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatTabChangeEvent, MatTabGroup } from '@angular/material/tabs';
import { TitleService } from '../../../services/title.service';
import {
  AccessibilityHelp,
  Alignment,
  Autoformat,
  AutoImage,
  AutoLink,
  Autosave,
  Base64UploadAdapter,
  BlockQuote,
  Bold,
  ClassicEditor, type EditorConfig,
  Essentials,
  FindAndReplace,
  FontBackgroundColor,
  FontColor,
  FontFamily,
  FontSize,
  GeneralHtmlSupport,
  Heading,
  Highlight,
  HorizontalLine,
  ImageBlock,
  ImageCaption,
  ImageInline,
  ImageInsert,
  ImageInsertViaUrl,
  ImageResize,
  ImageStyle,
  ImageTextAlternative,
  ImageToolbar,
  ImageUpload,
  Indent,
  IndentBlock,
  Italic,
  Link,
  LinkImage,
  List,
  ListProperties,
  MediaEmbed,
  PageBreak,
  Paragraph,
  PasteFromOffice,
  RemoveFormat,
  SelectAll,
  SourceEditing,
  SpecialCharacters,
  SpecialCharactersArrows,
  SpecialCharactersCurrency,
  SpecialCharactersEssentials,
  SpecialCharactersLatin,
  SpecialCharactersMathematical,
  SpecialCharactersText,
  Strikethrough,
  Style,
  Subscript,
  Superscript,
  Table,
  TableCaption,
  TableCellProperties,
  TableColumnResize,
  TableProperties,
  TableToolbar,
  TextTransformation, TodoList, Underline, Undo
} from 'ckeditor5';
import translations from 'ckeditor5/translations/fr.js';

@Component({
    selector: 'app-template-convention',
    templateUrl: './template-convention.component.html',
    styleUrls: ['./template-convention.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class TemplateConventionComponent implements OnInit, AfterViewInit {

  columns = ['typeConventionLabel', 'langueConvention.code', 'libelle', 'action'];
  sortColumn = 'libelle';
  exportColumns = {
    libelle: { title: 'Libelle' },
    typeConvention: { title: 'Type de convention' },
    langueConvention: { title: 'Langue de la convention' },
  };

  paramColumns = ['code', 'libelle', 'exemple'];
  paramSortColumn = 'code';
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
    private titleService: TitleService,
    private changeDetector: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      libelle: [null, Validators.maxLength(255)],
      typeConventions: [[], [Validators.required]],
      langueConvention: [null, [Validators.required]],
      texte: [null],
      texteAvenant: [null],
    });
  }

  ngOnInit(): void {
    this.resetTitle();
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

  public isLayoutReady = false;
  public Editor = ClassicEditor;
  public config: EditorConfig = {};

  public ngAfterViewInit(): void {
    this.config = {
      licenseKey: 'GPL',
      toolbar: {
        items: [
          'undo',
          'redo',
          '|',
          'sourceEditing',
          'findAndReplace',
          '|',
          'heading',
          '|',
          'fontSize',
          'fontFamily',
          'fontColor',
          'fontBackgroundColor',
          '|',
          'bold',
          'italic',
          'underline',
          'strikethrough',
          'subscript',
          'superscript',
          'removeFormat',
          '|',
          'specialCharacters',
          'horizontalLine',
          'pageBreak',
          'link',
          'insertImage',
          'mediaEmbed',
          'insertTable',
          'highlight',
          'blockQuote',
          '|',
          'alignment',
          '|',
          'bulletedList',
          'numberedList',
          'todoList',
          'outdent',
          'indent'
        ],
        shouldNotGroupWhenFull: false
      },
      plugins: [
        AccessibilityHelp,
        Alignment,
        Autoformat,
        AutoImage,
        AutoLink,
        Autosave,
        Base64UploadAdapter,
        BlockQuote,
        Bold,
        Essentials,
        FindAndReplace,
        FontBackgroundColor,
        FontColor,
        FontFamily,
        FontSize,
        GeneralHtmlSupport,
        Heading,
        Highlight,
        HorizontalLine,
        ImageBlock,
        ImageCaption,
        ImageInline,
        ImageInsert,
        ImageInsertViaUrl,
        ImageResize,
        ImageStyle,
        ImageTextAlternative,
        ImageToolbar,
        ImageUpload,
        Indent,
        IndentBlock,
        Italic,
        Link,
        LinkImage,
        List,
        ListProperties,
        MediaEmbed,
        PageBreak,
        Paragraph,
        PasteFromOffice,
        RemoveFormat,
        SelectAll,
        SourceEditing,
        SpecialCharacters,
        SpecialCharactersArrows,
        SpecialCharactersCurrency,
        SpecialCharactersEssentials,
        SpecialCharactersLatin,
        SpecialCharactersMathematical,
        SpecialCharactersText,
        Strikethrough,
        Style,
        Subscript,
        Superscript,
        Table,
        TableCaption,
        TableCellProperties,
        TableColumnResize,
        TableProperties,
        TableToolbar,
        TextTransformation,
        TodoList,
        Underline,
        Undo
      ],
      heading: {
        options: [
          {
            model: 'paragraph',
            title: 'Paragraph',
            class: 'ck-heading_paragraph'
          },
          {
            model: 'heading1',
            view: 'h1',
            title: 'Heading 1',
            class: 'ck-heading_heading1'
          },
          {
            model: 'heading2',
            view: 'h2',
            title: 'Heading 2',
            class: 'ck-heading_heading2'
          },
          {
            model: 'heading3',
            view: 'h3',
            title: 'Heading 3',
            class: 'ck-heading_heading3'
          },
          {
            model: 'heading4',
            view: 'h4',
            title: 'Heading 4',
            class: 'ck-heading_heading4'
          },
          {
            model: 'heading5',
            view: 'h5',
            title: 'Heading 5',
            class: 'ck-heading_heading5'
          },
          {
            model: 'heading6',
            view: 'h6',
            title: 'Heading 6',
            class: 'ck-heading_heading6'
          }
        ]
      },
      image: {
        toolbar: [
          'toggleImageCaption',
          'imageTextAlternative',
          '|',
          'imageStyle:inline',
          'imageStyle:wrapText',
          'imageStyle:breakText',
          '|',
          'resizeImage'
        ]
      },
      language: 'fr',
      link: {
        addTargetToExternalLinks: true,
        defaultProtocol: 'https://',
        decorators: {
          toggleDownloadable: {
            mode: 'manual',
            label: 'Downloadable',
            attributes: {
              download: 'file'
            }
          }
        }
      },
      list: {
        properties: {
          styles: true,
          startIndex: true,
          reversed: true
        }
      },
      placeholder: 'Type or paste your content here!',
      table: {
        contentToolbar: ['tableColumn', 'tableRow', 'mergeTableCells', 'tableProperties', 'tableCellProperties']
      },
      translations: [translations]
    };

    this.isLayoutReady = true;
    this.changeDetector.detectChanges();
  }

  resetTitle(): void {
    this.titleService.title = 'Templates de convention';
    this.titleService.titleTooltip = 'Templates de convention';
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.editTabIndex) {
      this.resetTitle();
      this.data = {};
      this.form.reset();
      this.form.get('typeConventions')?.enable();
      this.form.get('langueConvention')?.enable();
      this.form.patchValue({
        libelle: null,
        typeConventions: [],
        langueConvention: null,
        texte: this.defaultConvention,
        texteAvenant: this.defaultAvenant
      });
    } else {
      this.form.get('langueConvention')?.disable();
    }
  }

  canEdit(): boolean {
    return this.authService.checkRights({fonction: AppFonction.PARAM_GLOBAL, droits: [Droit.MODIFICATION]});
  }

  edit(row: any): void {
    this.data = { ...row, id: Number(row?.id) || row?.id };
    const selectedTypeConventions = this.getSelectedTypeConventions(row);
    this.form.patchValue({
      libelle: this.data.libelle ?? null,
      typeConventions: selectedTypeConventions,
      langueConvention: this.data.langueConvention,
      texte: this.data.texte ?? this.defaultConvention,
      texteAvenant: this.data.texteAvenant ?? this.defaultAvenant,
    });
    if (this.tabs) {
      this.tabs.selectedIndex = this.editTabIndex;
      const fullTitle = this.data.libelle || `${this.getTypeConventionLabel(this.data)} - ${this.data?.langueConvention?.code || ''}`;
      this.titleService.title = this.truncateText(fullTitle);
      this.titleService.titleTooltip = fullTitle;
    }
  }

  save(): void {
    if (this.form.valid) {
      const data = this.buildPayload();
      const templateConventionId = Number(this.data?.id);
      if (Number.isInteger(templateConventionId) && templateConventionId > 0) {
        this.templateConventionService.update(templateConventionId, data).subscribe((response: any) => {
          this.data = response;
          this.messageService.setSuccess('Template convention modifie');
          this.appTable?.update();
        });
      } else {
        this.templateConventionService.create(data).subscribe(() => {
          this.messageService.setSuccess('Template convention cree');
          this.appTable?.update();
          if (this.tabs) {
            this.tabs.selectedIndex = 0;
          }
        });
      }
    } else {
      this.messageService.setError('Veuillez remplir les champs obligatoires');
    }
  }

  private buildPayload(): any {
    const rawValue = this.form.getRawValue();
    const selectedTypes = (rawValue.typeConventions || []).map((typeConvention: any) => ({ id: typeConvention.id }));
    const templateConventionId = Number(this.data?.id);
    return {
      id: Number.isInteger(templateConventionId) && templateConventionId > 0 ? templateConventionId : null,
      libelle: rawValue.libelle,
      lookupTypeConventionId: this.data?.typeConvention?.id || null,
      lookupLangueConventionCode: this.data?.langueConvention?.code || null,
      langueConvention: rawValue.langueConvention ? { code: rawValue.langueConvention.code } : null,
      texte: rawValue.texte,
      texteAvenant: rawValue.texteAvenant,
      typeConventions: selectedTypes,
      typeConvention: selectedTypes.length > 0 ? selectedTypes[0] : null,
    };
  }

  getSelectedTypeConventions(templateConvention: any): any[] {
    if (Array.isArray(templateConvention?.typeConventions) && templateConvention.typeConventions.length > 0) {
      return templateConvention.typeConventions;
    }
    if (templateConvention?.typeConvention) {
      return [templateConvention.typeConvention];
    }
    return [];
  }

  truncateText(value: string | null | undefined, maxLength: number = 60): string {
    if (!value) {
      return '';
    }
    return value.length > maxLength ? `${value.slice(0, maxLength)}...` : value;
  }

  getTooltipText(value: string | null | undefined, maxLength: number = 60): string | null {
    if (!value) {
      return null;
    }
    return value.length > maxLength ? value : null;
  }

  getTypeConventionLabel(templateConvention: any): string {
    if (Array.isArray(templateConvention?.typeConventions) && templateConvention.typeConventions.length > 0) {
      return templateConvention.typeConventions
        .map((typeConvention: any) => typeConvention?.libelle)
        .filter((libelle: string | undefined) => !!libelle)
        .join(', ');
    }
    if (templateConvention?.typeConvention?.libelle) {
      return templateConvention.typeConvention.libelle;
    }
    return 'Sans type';
  }

  delete(id: number): void {
    this.templateConventionService.delete(id).subscribe(() => {
      this.appTable?.update();
      this.messageService.setSuccess('Suppression effectuee');
    });
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
