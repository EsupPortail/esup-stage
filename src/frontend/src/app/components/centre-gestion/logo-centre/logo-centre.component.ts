import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { CentreGestionService } from "../../../services/centre-gestion.service";
import { MessageService } from "../../../services/message.service";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { debounceTime } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-logo-centre',
  templateUrl: './logo-centre.component.html',
  styleUrls: ['./logo-centre.component.scss']
})
export class LogoCentreComponent implements OnInit {
  @Input() centreGestion: any;
  @Output() refreshCentreGestion = new EventEmitter<any>();
  @ViewChild('logoInput') logoInput!: ElementRef<HTMLInputElement>;

  logoFile: File | undefined;
  previewUrl: string | ArrayBuffer | null = null;
  currentFile: any;

  form: FormGroup;
  dimensions: any[] = [];
  height: any;
  width: any;

  constructor(
    private centreGestionService: CentreGestionService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private httpClient: HttpClient
  ) {
    this.form = this.fb.group({
      content: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.getLogo();
    this.resizeLogoOnChange();
  }

  onLogoChange(event: any): void {
    const file = event.target.files.item(0);

    if (!file || file.type.indexOf('image/') === -1) {
      this.messageService.setError("Le fichier doit être au format image");
      return;
    }

    this.logoFile = file;

    // Génération de la preview
    const reader = new FileReader();
    reader.onload = () => {
      this.previewUrl = reader.result;
    };

    if (this.logoFile) {
      reader.readAsDataURL(this.logoFile);
    } else {
      console.error("Aucun fichier sélectionné.");
    }

    // Ajout au FormData après vérification stricte
    const selectedFile = this.logoFile;
    if (selectedFile) {
      const formData = new FormData();
      formData.append('logo', selectedFile, selectedFile.name);

      this.centreGestionService.insertLogoCentre(formData, this.centreGestion.id).subscribe((response: any) => {
        this.centreGestion = response;
        this.refreshCentreGestion.emit(this.centreGestion);
        this.getLogo();
      });
    }
  }

  onLogoSelected(file: File): void {
    const reader = new FileReader();
    reader.onload = (e: any) => {
      this.previewUrl = e.target.result;
    };
    reader.readAsDataURL(file);
  }

  loadLogo(): void {
    this.httpClient.get('/api/logo', {responseType: 'text'}).subscribe((base64Data) => {
      this.previewUrl = `data:image/png;base64,${base64Data}`;
    });
  }

  getLogo() {
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

  resizeLogoOnChange() {
    this.form.valueChanges.pipe(debounceTime(1000)).subscribe(() => {
      if (this.height != null && this.width != null) {
        this.dimensions.push(this.width);
        this.dimensions.push(this.height);
        this.centreGestionService.resizeLogoCentre(this.centreGestion.id, this.dimensions).subscribe((response: any) => {
          this.dimensions = [];
          this.height = null;
          this.width = null;
          this.getLogo();
        });
      }
    });
  }

  removeLogo() {
    this.centreGestionService.deleteLogoCentre(this.centreGestion.id).subscribe((response: any) => {
      this.previewUrl = null;
      this.currentFile = null;
      this.logoFile = undefined;
      this.form.get('content')?.setValue('');
      if (this.logoInput && this.logoInput.nativeElement) {
        this.logoInput.nativeElement.value = '';
      }
      this.messageService.setSuccess("Logo supprimé avec succès");
      this.refreshCentreGestion.emit(this.centreGestion);
    }, error => {
      this.messageService.setError("Erreur lors de la suppression du logo");
    });
  }
}
