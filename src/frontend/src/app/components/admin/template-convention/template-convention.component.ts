import { Component, OnInit, ViewChild, ChangeDetectorRef, ViewEncapsulation } from '@angular/core';
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
import { TitleService } from "../../../services/title.service";
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
} from "ckeditor5";
import translations from 'ckeditor5/translations/fr.js';

@Component({
    selector: 'app-template-convention',
    templateUrl: './template-convention.component.html',
    styleUrls: ['./template-convention.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: false
})
export class TemplateConventionComponent implements OnInit {

  columns = ['typeConvention.libelle', 'langueConvention.code', 'action'];
  sortColumn = 'typeConvention.libelle';
  exportColumns = {
    typeConvention: { title: 'Type de convention' },
    langueConvention: { title: 'Langue de la convention' },
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
    private titleService: TitleService,
    private changeDetector: ChangeDetectorRef
  ) {
    this.form = this.fb.group({
      typeConvention: [null, [Validators.required]],
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
  public ngAfterViewInit():void{
    this.config = {
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
      fontFamily: {
        supportAllValues: true
      },
      fontSize: {
        options: [10, 12, 14, 'default', 18, 20, 22],
        supportAllValues: true
      },
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
      htmlSupport: {
        allow: [
          {
            name: /^.*$/,
            styles: true,
            attributes: true,
            classes: true
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
      style: {
        definitions: [
          {
            name: 'Article category',
            element: 'h3',
            classes: ['category']
          },
          {
            name: 'Title',
            element: 'h2',
            classes: ['document-title']
          },
          {
            name: 'Subtitle',
            element: 'h3',
            classes: ['document-subtitle']
          },
          {
            name: 'Info box',
            element: 'p',
            classes: ['info-box']
          },
          {
            name: 'Side quote',
            element: 'blockquote',
            classes: ['side-quote']
          },
          {
            name: 'Marker',
            element: 'span',
            classes: ['marker']
          },
          {
            name: 'Spoiler',
            element: 'span',
            classes: ['spoiler']
          },
          {
            name: 'Code (dark)',
            element: 'pre',
            classes: ['fancy-code', 'fancy-code-dark']
          },
          {
            name: 'Code (bright)',
            element: 'pre',
            classes: ['fancy-code', 'fancy-code-bright']
          }
        ]
      },
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
  }

  tabChanged(event: MatTabChangeEvent): void {
    if (event.index !== this.editTabIndex) {
      this.resetTitle();
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
      this.titleService.title = `${this.data.typeConvention.libelle} - ${this.data.langueConvention.code}`;
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

  delete(id: number) {
    this.templateConventionService.delete(id).subscribe((response: any) => {
      this.appTable?.update();
      this.messageService.setSuccess("Suppressions effectuée");
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
