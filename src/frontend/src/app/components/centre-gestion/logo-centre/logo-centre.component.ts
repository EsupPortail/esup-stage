import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { ImageCroppedEvent, ImageCropperComponent, LoadedImage } from 'ngx-image-cropper';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

@Component({
    selector: 'app-logo-centre',
    templateUrl: './logo-centre.component.html',
    styleUrls: ['./logo-centre.component.scss'],
    standalone: false
})
export class LogoCentreComponent implements OnInit {
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

  // --- Intégration ngx-image-cropper ---
  imageChangedEvent: Event | null = null;
  croppedImage: SafeUrl | string = '';
  croppedBlob: Blob | null = null; // Pour stocker le blob à envoyer
  outputFormat: 'png' | 'jpeg' | 'webp' = 'png';

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private httpClient: HttpClient,
    private sanitizer: DomSanitizer
  ) {
    this.form = this.fb.group({
      content: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.getLogo();
    this.resizeLogoOnChange();
  }

  /**
   * Sélection d'un fichier
   */
  onLogoChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    const file: File | null = target?.files?.item(0) ?? null;

    if (!file || file.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      return;
    }

    this.logoFile = file;
    this.imageChangedEvent = event;
    this.croppedImage = '';
    this.croppedBlob = null;

    // Preview brute (optionnel)
    const reader = new FileReader();
    reader.onload = () => { this.previewUrl = reader.result; };
    reader.readAsDataURL(file);
  }

  /**
   * Callback ngx-image-cropper : récupère la base64 ET le blob
   */
  imageCropped(event: ImageCroppedEvent): void {
    // Récupération de l'URL sécurisée pour l'affichage
    this.croppedImage = this.sanitizer.bypassSecurityTrustUrl(event.objectUrl || '');

    // Stockage du blob pour l'envoi
    this.croppedBlob = event.blob || null;

    console.log('Image croppée:', !!this.croppedBlob, !!this.croppedImage);
  }

  /**
   * Callback en cas d'erreur de chargement
   */
  loadImageFailed(): void {
    this.messageService.setError("Impossible de charger l'image dans l'outil de recadrage");
  }

  /**
   * Upload de l'image recadrée
   */
  uploadCroppedLogo(): void {
    if (!this.croppedBlob) {
      this.messageService.setError("Veuillez recadrer l'image avant d'enregistrer");
      return;
    }

    const fileName = `logo_cropped.${this.outputFormat}`;
    const file = new File([this.croppedBlob], fileName, { type: `image/${this.outputFormat}` });

    const formData = new FormData();
    formData.append('logo', file, file.name);
    this._sendLogo(formData);
  }

  /**
   * Annuler le recadrage
   */
  cancelCrop(): void {
    this.imageChangedEvent = null;
    this.croppedImage = '';
    this.croppedBlob = null;
    this.logoFile = undefined;
    if (this.logoInput?.nativeElement) {
      this.logoInput.nativeElement.value = '';
    }
  }

  /**
   * Envoi commun (inchangé côté backend)
   */
  private _sendLogo(formData: FormData): void {
    this.centreGestionService.insertLogoCentre(formData, this.centreGestion.id).subscribe((response: any) => {
      this.centreGestion = response;
      this.refreshCentreGestion.emit(this.centreGestion);

      // Reset du cropper / input
      this.imageChangedEvent = null;
      this.croppedImage = '';
      this.croppedBlob = null;
      this.logoFile = undefined;
      if (this.logoInput?.nativeElement) {
        this.logoInput.nativeElement.value = '';
      }

      this.getLogo();

      this.messageService.setSuccess("Logo mis à jour avec succès");
    }, _err => {
      this.messageService.setError("Erreur lors de l'envoi du logo");
    });
  }

  /**
   * Recharger le logo depuis le backend
   */
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
      }
    });
  }

  /**
   * Envoi de l'image redimensionnée au serveur
   */
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
        });
      }
    });
  }

  /**
   * Suppression du logo
   */
  removeLogo(): void {
    this.centreGestionService.deleteLogoCentre(this.centreGestion.id).subscribe((_response: any) => {
      this.previewUrl = null;
      this.currentFile = null;
      this.logoFile = undefined;
      this.croppedImage = '';
      this.croppedBlob = null;
      this.imageChangedEvent = null;
      this.form.get('content')?.setValue('');
      if (this.logoInput && this.logoInput.nativeElement) {
        this.logoInput.nativeElement.value = '';
      }
      this.messageService.setSuccess("Logo supprimé avec succès");
      this.refreshCentreGestion.emit(this.centreGestion);
    }, _error => {
      this.messageService.setError("Erreur lors de la suppression du logo");
    });
  }
}
