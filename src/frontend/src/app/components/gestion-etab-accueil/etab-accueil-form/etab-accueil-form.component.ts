import {Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ChangeDetectorRef, ViewEncapsulation, AfterViewInit, OnDestroy} from '@angular/core';
import { FormBuilder, FormControl, Validators } from "@angular/forms";
import { StructureService } from "../../../services/structure.service";
import { CommuneService } from "../../../services/commune.service";
import { PaysService } from "../../../services/pays.service";
import { TypeStructureService } from "../../../services/type-structure.service";
import { NafN1Service } from "../../../services/naf-n1.service";
import { NafN5Service } from "../../../services/naf-n5.service";
import { StatutJuridiqueService } from "../../../services/statut-juridique.service";
import { EffectifService } from "../../../services/effectif.service";
import { MessageService } from "../../../services/message.service";
import {REGEX} from "../../../utils/regex.utils";
import { ReplaySubject, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
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
import { AuthService } from "../../../services/auth.service"
import {AppFonction} from "../../../constants/app-fonction";
import {Droit} from "../../../constants/droit";
import {ConfigService} from "../../../services/config.service";

@Component({
  selector: 'app-etab-accueil-form',
  standalone: false,
  templateUrl: './etab-accueil-form.component.html',
  styleUrls: ['./etab-accueil-form.component.scss'],
  encapsulation:ViewEncapsulation.None
})
export class EtabAccueilFormComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {

  @Input() etab: any;
  @Output() submitted = new EventEmitter<any>();
  @Output() canceled = new EventEmitter<boolean>();

  countries: any[] = [];
  communes: any[] = [];
  typeStructures: any[] = [];
  secteurs: any[] = [];
  statutJuridiques: any[] = [];
  effectifs: any[] = [];
  selectedNafN5: any;
  nafN5List: any[] = [];
  creationSeulementHorsFrance : boolean = false;
  creationSeulementFrance : boolean = false;

  nafN5FilterCtrl: FormControl = new FormControl();
  filteredNafN5List: ReplaySubject<any> = new ReplaySubject<any>(1);
  paysFilterCtrl: FormControl = new FormControl('');
  filteredCountries: ReplaySubject<any[]> = new ReplaySubject<any[]>(1);
  _onDestroy = new Subject<void>();
  autoUpdating = false;
  isSireneActive = false;
  filterTypeContries!: 0 | 1 | 2  ;

  form: any;

  constructor(
    public structureService: StructureService,
    public communeService: CommuneService,
    private paysService: PaysService,
    private typeStructureService: TypeStructureService,
    private nafN1Service: NafN1Service,
    private nafN5Service: NafN5Service,
    private statutJuridiqueService: StatutJuridiqueService,
    private effectifService: EffectifService,
    private authService: AuthService,
    private fb: FormBuilder,
    private messageService: MessageService,
    private changeDetector: ChangeDetectorRef,
    private configService: ConfigService
  ) { }

  ngOnInit(): void {
    this.configService.getConfigGenerale().subscribe(response=>{
      const autorisationCreationHorsFrance = response.autoriserEtudiantACreerEntrepriseHorsFrance;
      const autorisationCreationFrance = response.autoriserEtudiantACreerEntrepriseFrance;
      this.creationSeulementHorsFrance = autorisationCreationHorsFrance && !autorisationCreationFrance;
      this.creationSeulementFrance = !autorisationCreationHorsFrance && autorisationCreationFrance
    })
    this.paysService.getPaginated(1, 0, 'lib', 'asc', JSON.stringify({ temEnServPays: { value: 'O', type: 'text' } })).subscribe((response: any) => {
        this.countries = response.data;
        this.filterTypeContries = 0;

        // Restriction étudiant : enlever la France si nécessaire
        if (this.authService.isEtudiant() && this.creationSeulementHorsFrance) {
          this.countries = this.countries.filter(c => c.libelle !== 'FRANCE');
          this.filterTypeContries = 1;
        }

        // Restriction étudiant : enlever les autres pays si nécessaire
        if(this.authService.isEtudiant() && this.creationSeulementFrance){
          this.countries = this.countries.filter(c => c.libelle == 'FRANCE');
          this.filterTypeContries = 2;
        }

        this.filteredCountries.next(this.countries.slice());

        this.paysFilterCtrl.valueChanges.pipe(takeUntil(this._onDestroy)).subscribe(() => this.filterCountries());
    });
    this.communeService.getPaginated(1, 0, 'lib', 'asc', "").subscribe((response: any) => {
      this.communes = response;
    });
    this.typeStructureService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.typeStructures = response.data;
    });
    this.nafN1Service.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.secteurs = response.data;
    });
    this.statutJuridiqueService.getPaginated(1, 0, 'libelle', 'asc', JSON.stringify({temEnServ: {value: 'O', type: 'text'}})).subscribe((response: any) => {
      this.statutJuridiques = response.data;
    });
    this.effectifService.getAll().subscribe((response: any) => {
      this.effectifs =  response;
    });
    this.structureService.getSireneInfo().subscribe(res=>{
      this.isSireneActive = res.isApiSireneActive
    });
    this.getNafN5List();
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

  ngOnChanges(changes: SimpleChanges): void {
    this.form = this.fb.group({
      raisonSociale: [{ value: this.etab.raisonSociale, disabled: this.isFieldDisabled() }, [Validators.required, Validators.maxLength(150)]],
      numeroSiret: [{ value: this.etab.numeroSiret, disabled: this.isFieldDisabled() }, [Validators.maxLength(14), Validators.pattern('[0-9]{14}')]],
      idEffectif: [this.etab.effectif ? this.etab.effectif.id : null],
      idTypeStructure: [this.etab.typeStructure ? this.etab.typeStructure.id : null, [Validators.required]],
      idStatutJuridique: [this.etab.statutJuridique?.id ?? null, [Validators.required]],
      codeNafN5: [this.etab.nafN5?.code ?? null, []],
      activitePrincipale: [this.etab.activitePrincipale, []],
      voie: [this.etab.voie, [Validators.required, Validators.maxLength(200)]],
      codePostal: [this.etab.codePostal, [Validators.required, Validators.maxLength(10)]],
      batimentResidence: [this.etab.batimentResidence, [Validators.maxLength(200)]],
      commune: [this.etab.commune, [Validators.required, Validators.maxLength(200)]],
      libCedex: [this.etab.libCedex, [Validators.maxLength(20)]],
      idPays: [this.etab.pays?.id ?? null, [Validators.required]],
      mail: [this.etab.mail, [Validators.pattern(REGEX.EMAIL), Validators.maxLength(255)]],
      telephone: [this.etab.telephone, [Validators.required, Validators.pattern(/^(?:(?:\+|00)\d{1,4}[-.\s]?|0)\d{1,4}([-.\s]?\d{1,4})*$/), Validators.maxLength(50)]],
      siteWeb: [this.etab.siteWeb, [Validators.maxLength(200), Validators.pattern('^https?://(\\w([\\w\\-]{0,61}\\w)?\\.)+[a-zA-Z]{2,6}([/]{1}.*)?$')]],
      fax: [this.etab.fax, [Validators.maxLength(20)]],
      numeroRNE: [this.etab.numeroRNE, [Validators.maxLength(8), Validators.pattern('[0-9]{7}[a-zA-Z]')]],
      verrouillageSynchroStructureSirene: [this.etab.verrouillageSynchroStructureSirene || false]
    });
    this.toggleCommune();
    this.form.get('idPays')?.valueChanges
      .pipe(takeUntil(this._onDestroy))
      .subscribe((idPays: any) => {
        if (this.autoUpdating) return;
        this.toggleCommune();
        this.clearCommune();
      });

    if (this.etab.nafN5) {
      this.selectedNafN5 = this.etab.nafN5;
    }
  }

  getNafN5List(): void {
    this.nafN5Service.findAll().subscribe((response: any) => {
      this.nafN5List = response;
      this.filteredNafN5List.next(this.nafN5List.slice());
      this.nafN5FilterCtrl.valueChanges
        .pipe(takeUntil(this._onDestroy))
        .subscribe(() => {
          this.filterNafN5List();
        });
    });
  }

  filterNafN5List() {
    if (!this.nafN5List) {
      return;
    }

    let search = this.nafN5FilterCtrl.value;
    if (!search) {
      this.filteredNafN5List.next(this.nafN5List.slice());
      return;
    } else {
      search = search.toLowerCase();
    }

    this.filteredNafN5List.next(
      this.nafN5List.filter(nafN5 => nafN5.code.toLowerCase().indexOf(search) > -1 || nafN5.libelle.toLowerCase().indexOf(search) > -1)
    );
  }

  setSelectedNafN5(nafN5: any): void {
    this.selectedNafN5 = nafN5;
  }

  save(): void {
    if (this.form.valid) {
      if (!this.form.get('codeNafN5')?.value && !this.form.get('activitePrincipale')?.value) {
        this.messageService.setError('Une de ces deux informations doivent être renseignée : Code APE, Activité principale');
        return;
      }

      if (this.isFr() && !this.isCodePostalValid()) {
        this.messageService.setError('Code postal inconnu');
        return;
      }

      if (this.form.get('numeroSiret')?.value === "") {
        this.form.get('numeroSiret')?.setValue(null);
      }
      const data = {...this.form.getRawValue()};
      if (data.verrouillageSynchroStructureSirene == null) {
        data.verrouillageSynchroStructureSirene = false;
      }
      data.nafN5 = this.selectedNafN5;
      if (this.etab.id) {
        this.structureService.update(this.etab.id, data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil modifié');
          this.etab = response;
          this.submitted.emit(this.etab);
        });
      } else {
        this.structureService.create(data).subscribe((response: any) => {
          this.messageService.setSuccess('Établissement d\'accueil créé');
          this.etab = response;
          this.submitted.emit(this.etab);
        });
      }
    }
  }

  cancelEdit(): void {
    this.canceled.emit(true);
  }

  setTypeStructure(statutJuridique: any) {
    this.form.get('idTypeStructure')?.setValue(statutJuridique.typeStructure.id);
  }

  numeroSiretRequired() {
    let idTypeStructure = this.form.get('idTypeStructure')?.value;
    if (idTypeStructure) {
      let typeStructure = this.typeStructures.find(type => type.id === idTypeStructure);
      if (typeStructure)
        return typeStructure.siretObligatoire;
    }

    return false;
  }

  clearCommune(): void {
      this.form.get('commune')?.setValue('');
      this.form.get('codePostal')?.setValue('');
      this.form.get('commune')?.markAsPristine();
      this.form.get('codePostal')?.markAsPristine();
  }
  toggleCommune(): void {
      if (!this.isPaysSet()) {
        this.form.get('commune')?.disable();
        this.form.get('codePostal')?.disable();
      }else{
        if (this.isFr()) {
          this.form.get('commune')?.disable();
          this.form.get('codePostal')?.enable();
        }else{
          this.form.get('commune')?.enable();
          this.form.get('codePostal')?.enable();
        }
      }
  }

  updateCommune(commune : any): void {
    this.form.get('commune')?.setValue(commune.split(' - ')[0]);
    this.form.get('codePostal')?.setValue(commune.split(' - ')[1]);
  }

  isPaysSet() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays)
      return true;
    return false;
  }

  isFr() {
    let idPays = this.form.get('idPays')?.value;
    if (idPays) {
      let pays = this.countries.find(c => c.id === idPays);
      if (pays)
        return pays.libelle === 'FRANCE';
    }
    return true;
  }

  isCodePostalValid() {
    let codePostal = this.form.get('codePostal')?.value;
    if (codePostal) {
      let commune = this.communes.find(c => c.codePostal === codePostal);
      if (commune)
        return true;
    }
    return false;
  }

  canEdit(){
    return !this.authService.isEtudiant() && !this.etab.temSiren
  }

  canCreate(): boolean {
    return this.authService.checkRights({fonction: AppFonction.ORGA_ACC, droits: [Droit.CREATION]});
  }

  isFieldDisabled(): boolean {
      return !!this.etab?.id ? !this.canEdit() : !this.canCreate();
  }

  private filterCountries(): void {
    if (!this.countries || !Array.isArray(this.countries)) {
      this.filteredCountries.next([]);
      return;
    }
    let search = this.paysFilterCtrl.value;
    if (!search) {
      this.filteredCountries.next(this.countries.slice());
      return;
    }
    search = ('' + search).toLowerCase().trim();
    this.filteredCountries.next(
      this.countries.filter(c =>
        (c.libelle ?? '').toLowerCase().includes(search) ||
        (c.code ?? '').toLowerCase().includes(search)
      )
    );
  }

  ngOnDestroy(): void {
    this._onDestroy.next();
    this._onDestroy.complete();
  }

  private patchFormFromEtab(updated: any): void {
    if (!this.form) { return; }

    this.form.patchValue({
      raisonSociale: updated?.raisonSociale ?? null,
      numeroSiret: updated?.numeroSiret ?? null,
      idEffectif: updated?.effectif?.id ?? null,
      idTypeStructure: updated?.typeStructure?.id ?? null,
      idStatutJuridique: updated?.statutJuridique?.id ?? null,
      codeNafN5: updated?.nafN5?.code ?? null,
      activitePrincipale: updated?.activitePrincipale ?? null,
      voie: updated?.voie ?? null,
      codePostal: updated?.codePostal ?? null,
      batimentResidence: updated?.batimentResidence ?? null,
      commune: updated?.commune ?? null,
      libCedex: updated?.libCedex ?? null,
      idPays: updated?.pays?.id ?? null,
      mail: updated?.mail ?? null,
      telephone: updated?.telephone ?? null,
      siteWeb: updated?.siteWeb ?? null,
      fax: updated?.fax ?? null,
      numeroRNE: updated?.numeroRNE ?? null,
    }, { emitEvent: true });

    this.selectedNafN5 = updated?.nafN5 ?? null;

    this.toggleCommune();
    this.changeDetector.detectChanges();
  }

  autoUpdateFromApi(): void {
    if (!this.etab?.id || this.authService.isEtudiant()) {
      return;
    }
    this.autoUpdating = true;

    this.structureService.updateFromSirene(this.etab.id).subscribe({
      next: (updated: any) => {
        this.messageService.setSuccess('Mise à jour automatique effectuée.');
        this.etab = updated;
        this.patchFormFromEtab(updated);
      },
      error: () => {
        this.autoUpdating = false;
        this.messageService.setError('Échec de la mise à jour automatique.');
      },
      complete: () => {
        this.autoUpdating = false;
      }
    });
  }

  canUpdateFromApi(): boolean {
    return !(this.authService.isEtudiant() || this.authService.isEnseignant()) && this.isSireneActive && this.etab?.id;
  }

  // Méthode pour basculer l'état du verrouillage
  toggleVerrouillage(): void {
    this.etab.verrouillageSynchroStructureSirene = !this.etab.verrouillageSynchroStructureSirene;
    this.form.get('verrouillageSynchroStructureSirene')?.setValue(this.etab.verrouillageSynchroStructureSirene);
  }
}
