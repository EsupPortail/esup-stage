import {
  Component,
  Input,
  OnInit,
  Output,
  EventEmitter,
  OnChanges,
  SimpleChanges,
  ChangeDetectorRef,
  ViewChild, ElementRef, ViewEncapsulation, AfterViewInit
} from '@angular/core';
import * as FileSaver from "file-saver";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { ConsigneService } from "../../../services/consigne.service";
import { MessageService } from "../../../services/message.service";
import {
  ClassicEditor,
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
  Undo,
  type EditorConfig
} from 'ckeditor5';

import translations from 'ckeditor5/translations/fr.js';

@Component({
  selector: 'app-consigne',
  templateUrl: './consigne.component.html',
  styleUrls: ['./consigne.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class ConsigneComponent implements OnInit, OnChanges, AfterViewInit {

  public Editor = ClassicEditor;
  public config: EditorConfig = {};
  public isLayoutReady = false;


  @Input() consigne: any;
  @Input() idCentreGestion!: number;
  @Output() sumitted = new EventEmitter<any>();

  form!: FormGroup;
  @ViewChild('editor', { static: false }) editorElement!: ElementRef;

  constructor(
    private fb: FormBuilder,
    private consigneService: ConsigneService,
    private messageService: MessageService,
    private changeDetector : ChangeDetectorRef
  ) {
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      texte: [this.consigne ? this.consigne.texte : null],
      idCentreGestion: [this.idCentreGestion, [Validators.required]],
    });
  }

  ngOnChanges(changes: SimpleChanges) {
  }


  saveConsigne(): void {
    if (this.form.valid) {
      if (this.consigne) {
        this.consigneService.updateConsigne(this.consigne.id, this.form.value).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Consigne modifiée');
          this.sumitted.emit(this.consigne);
        });
      } else {
        this.consigneService.createConsigne(this.form.value).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Consigne créée');
          this.sumitted.emit(this.consigne);
        });
      }
    }
  }

  downloadDoc(event: any, doc: any): void {
    event.preventDefault();
    event.stopPropagation();
    let mimetype = 'application/pdf';
    if (doc.nomReel.endsWith('.doc')) mimetype = 'application/msword';
    if (doc.nomReel.endsWith('.docx')) mimetype = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    this.consigneService.getDocument(this.consigne.id, doc.id).subscribe((response: any) => {
      var blob = new Blob([response as BlobPart], {type: mimetype});
      FileSaver.saveAs(blob, doc.nomReel);
    });
  }

  addDoc(event: any): void {
    if (this.consigne) {
      const doc = event.target.files.item(0);
      if (doc) {
        if (['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].indexOf(doc?.type) === -1) {
          this.messageService.setError("Le fichier doit être au format pdf, doc ou docx");
          return;
        }
        if (doc.size > 10485760) {
          this.messageService.setError("Le fichier ne doit pas dépasser 10Mo");
          return;
        }
        const formData = new FormData();
        formData.append('doc', doc, doc.name);
        this.consigneService.addDoc(this.consigne.id, formData).subscribe((response: any) => {
          this.consigne = response;
          this.messageService.setSuccess('Document ajouté');
        });
      }
    }
  }

  deleteDoc(idDoc: number): void {
    this.consigneService.deleteDoc(this.consigne.id, idDoc).subscribe((response: any) => {
      this.consigne = response;
      this.messageService.setSuccess('Document supprimé');
    });
  }

  public ngAfterViewInit(): void {
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
      placeholder: 'Tapez ou collez votre contenu ici…',
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
    }
    this.isLayoutReady = true;
    this.changeDetector.detectChanges();
  }

  deleteConsigne() {
    if (this.consigne) {
      this.consigneService.deleteConsigne(this.consigne.id).subscribe({
        next: () => {
          this.messageService.setSuccess('Consigne supprimée');
          this.form.get('texte')?.setValue('');     // ou this.form.patchValue({ texte: '' });
          this.form.markAsPristine();
          this.form.markAsUntouched();
          this.consigne = null;
          this.sumitted.emit(null);
        },
        error: (err) => {
          this.messageService.setError('Erreur lors de la suppression');
        }
      });
    }
  }

}
