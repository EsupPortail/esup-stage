import {AfterViewInit, ChangeDetectorRef, Component, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {TableComponent} from "../../table/table.component";
import {TemplateMailGroupeService} from "../../../services/template-mail-groupe.service";
import {AppFonction} from "../../../constants/app-fonction";
import {Droit} from "../../../constants/droit";
import {AuthService} from "../../../services/auth.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MatTabChangeEvent, MatTabGroup} from "@angular/material/tabs";
import {MessageService} from "../../../services/message.service";
import {MatDialog} from '@angular/material/dialog';
import {MailTesterComponent} from "../../admin/template-mail/mail-tester/mail-tester.component";
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
  ClassicEditor,
  type EditorConfig,
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
} from "ckeditor5";
import translations from 'ckeditor5/translations/fr.js';

@Component({
  selector: 'app-template-mail-groupe',
  templateUrl: './template-mail-groupe.component.html',
  styleUrls: ['./template-mail-groupe.component.scss'],
  encapsulation : ViewEncapsulation.None
})
export class TemplateMailGroupeComponent implements OnInit, AfterViewInit {

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
    private changeDetector: ChangeDetectorRef,
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

  public isLayoutReady = false;
  public Editor = ClassicEditor;
  public config: EditorConfig = {};
  public ngAfterViewInit() : void {
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
