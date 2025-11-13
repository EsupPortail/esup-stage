import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef, OnDestroy} from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormControl } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { ImageCroppedEvent, ImageCropperComponent } from 'ngx-image-cropper';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { ConventionService } from "../../../services/convention.service";
import { TemplateConventionService } from "../../../services/template-convention.service";

@Component({
  selector: 'app-logo-centre',
  standalone: false,
  templateUrl: './logo-centre.component.html',
  styleUrl: './logo-centre.component.scss',
})
export class LogoCentreComponent implements OnInit, OnDestroy {
  @Input() centreGestion: any;
  @Output() refreshCentreGestion = new EventEmitter<any>();
  @ViewChild('logoInput') logoInput!: ElementRef<HTMLInputElement>;
  @ViewChild('cropper') cropper?: ImageCropperComponent;

  logoFile: File | undefined;
  previewUrl: string | ArrayBuffer | null = null;
  currentFile: any;
  form: FormGroup;
  dimensions: any[] = [];
  height: any;
  width: any;
  private readonly MAX_IMAGE_BYTES = 5 * 1024 * 1024; // 5 MiB

  // --- Cropper
  imageChangedEvent: Event | null = null;
  croppedImage: SafeUrl | string = '';
  croppedBlob: Blob | null = null;
  outputFormat: 'png' | 'jpeg' | 'webp' = 'png';

  // --- Template de convention
  templateCtrl = new FormControl<number | null>(null);
  templateConvention: Array<any> = [];

  // --- Preview PDF
  showPdfPreview: boolean = false;
  pdfUrl: SafeUrl | null = null;
  pdfLoading: boolean = false;
  pdfError: string | null = null;
  private currentPdfObjectUrl: string | null = null;

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private sanitizer: DomSanitizer,
    private conventionService: ConventionService,
    private templateConventionService: TemplateConventionService,
  ) {
    this.form = this.fb.group({
      content: [null, Validators.required]
    });
  }

  // =========================================================
  // Lifecycle
  // =========================================================
  ngOnInit(): void {
    this.getLogo();
    this.resizeLogoOnChange();

    this.templateConventionService.getAll().subscribe((templates: any[]) => {
      this.templateConvention = templates;
    });
    this.templateCtrl.valueChanges.subscribe((templateId) => {
      const hasLogo = !!this.previewUrl || !!this.currentFile;
      if (hasLogo && templateId) {
        this.generatePdfPreview();
      } else {
        this.hidePdfPreview();
      }
    });
  }

  ngOnDestroy(): void {
    this.cleanupPdfUrl();
  }

  // =========================================================
  // Gestion logo + crop
  // =========================================================
  onLogoChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const file: File | null = target?.files?.item(0) ?? null;

    if (!file || file.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      return;
    }

    if (file.size > this.MAX_IMAGE_BYTES) {
      this.rejectTooLarge(file.size);
      return;
    }

    this.logoFile = file;
    this.imageChangedEvent = event;
    this.croppedImage = '';
    this.croppedBlob = null;

    const reader = new FileReader();
    reader.onload = () => { this.previewUrl = reader.result; };
    reader.readAsDataURL(file);
  }

  imageCropped(event: ImageCroppedEvent): void {
    this.croppedImage = this.sanitizer.bypassSecurityTrustUrl(event.objectUrl || '');
    this.croppedBlob = event.blob || null;
  }

  loadImageFailed(): void {
    this.messageService.setError("Impossible de charger l'image dans l'outil de recadrage");
  }

  uploadCroppedLogo(): void {
    if (!this.croppedBlob) {
      this.messageService.setError("Veuillez recadrer l'image avant d'enregistrer");
      return;
    }

    const fileName = `logo_cropped.${this.outputFormat}`;
    const file = new File([this.croppedBlob], fileName, { type: `image/${this.outputFormat}` });

    const formData = new FormData();
    formData.append('logo', file, file.name);
    this.sendLogo(formData);
  }

  cancelCrop(): void {
    this.imageChangedEvent = null;
    this.croppedImage = '';
    this.croppedBlob = null;
    this.logoFile = undefined;
    if (this.logoInput?.nativeElement) {
      this.logoInput.nativeElement.value = '';
    }
  }

  private sendLogo(formData: FormData): void {
    this.centreGestionService.insertLogoCentre(formData, this.centreGestion.id).subscribe({
      next: (response: any) => {
        this.centreGestion = response;
        this.refreshCentreGestion.emit(this.centreGestion);
        this.imageChangedEvent = null;
        this.croppedImage = '';
        this.croppedBlob = null;
        this.logoFile = undefined;
        if (this.logoInput?.nativeElement) {
          this.logoInput.nativeElement.value = '';
        }
        this.getLogo();
        const templateId = this.templateCtrl.value;
        if (templateId) {
          this.showPdfPreview = true;
          this.generatePdfPreview();
        } else {
          this.hidePdfPreview();
        }

        this.messageService.setSuccess("Logo mis à jour avec succès");
      },
      error: () => {
        this.messageService.setError("Erreur lors de l'envoi du logo");
      }
    });
  }

  getLogo(): void {
    this.centreGestionService.getLogoCentre(this.centreGestion.id).subscribe((response: any) => {
      this.currentFile = response;
      if (this.currentFile && this.currentFile.size > 0) {
        const reader = new FileReader();
        reader.readAsDataURL(this.currentFile);
        reader.onload = (_event) => {
          this.previewUrl = reader.result;
          this.form.get('content')?.setValue('<p style="text-align: center"><img src="' + this.previewUrl + '" /><p>');
        };
      } else {
        this.hidePdfPreview();
      }
    });
  }

  resizeLogoOnChange(): void {
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      if (this.height != null && this.width != null) {
        this.dimensions.push(this.width);
        this.dimensions.push(this.height);
        this.centreGestionService.resizeLogoCentre(this.centreGestion.id, this.dimensions).subscribe((_response: any) => {
          this.dimensions = [];
          this.height = null;
          this.width = null;
          this.getLogo();

          if (this.showPdfPreview && this.templateCtrl.value) {
            this.generatePdfPreview();
          }
        });
      }
    });
  }

  removeLogo(): void {
    this.centreGestionService.deleteLogoCentre(this.centreGestion.id).subscribe({
      next: (_response: any) => {
        this.previewUrl = null;
        this.currentFile = null;
        this.logoFile = undefined;
        this.croppedImage = '';
        this.croppedBlob = null;
        this.imageChangedEvent = null;
        this.form.get('content')?.setValue('');

        this.hidePdfPreview();

        this.messageService.setSuccess("Logo supprimé avec succès");
        this.refreshCentreGestion.emit(this.centreGestion);
      },
      error: () => {
        this.messageService.setError("Erreur lors de la suppression du logo");
      }
    });
  }

  // =========================================================
  // Preview PDF
  // =========================================================
  private generatePdfPreview(): void {
    if (!this.centreGestion?.id) {
      this.pdfError = "ID du centre non disponible";
      this.showPdfPreview = false;
      return;
    }
    const templateId = this.templateCtrl.value;
    if (!templateId) {
      this.hidePdfPreview();
      return;
    }

    this.showPdfPreview = true;
    this.pdfLoading = true;
    this.pdfError = null;
    this.cleanupPdfUrl();

    this.conventionService.getPreviewPdf(this.centreGestion.id, templateId)
      .subscribe({
        next: (blob: Blob) => {
          const url = URL.createObjectURL(blob);
          this.currentPdfObjectUrl = url;
          this.pdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
          this.pdfLoading = false;
        },
        error: (err: any) => {
          console.error('Erreur génération preview PDF:', err);
          this.pdfError = "Impossible de générer l'aperçu de la convention";
          this.pdfLoading = false;
          this.messageService.setError(this.pdfError);
        }
      });
  }

  private hidePdfPreview(): void {
    this.showPdfPreview = false;
    this.pdfUrl = null;
    this.pdfError = null;
    this.pdfLoading = false;
    this.cleanupPdfUrl();
  }

  private cleanupPdfUrl(): void {
    if (this.currentPdfObjectUrl) {
      URL.revokeObjectURL(this.currentPdfObjectUrl);
      this.currentPdfObjectUrl = null;
    }
    this.pdfUrl = null;
  }

  private rejectTooLarge(size: number): void {
    const mb = (size / (1024 * 1024)).toFixed(2);
    this.messageService.setError(`L'image dépasse 5 Mo (${mb} Mo).`);
    this.cancelCrop();
  }
}
