import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ConventionDocumentService } from '../../../services/convention-documents.service';
import { ConventionDocumentEtudiant, ConventionDocumentsResponse } from '../../../models/convention-document.model';
import { DepotDocumentPreviewDialogComponent, PreviewDialogData } from './preview-dialog/preview-dialog.component';

type DepotDocumentsStatusType = 'success' | 'error' | 'info';

@Component({
  selector: 'app-depot-documents',
  standalone: false,
  templateUrl: './depot-documents.component.html',
  styleUrls: ['./depot-documents.component.scss']
})
export class DepotDocumentsComponent implements OnInit, OnChanges {

  @Input() idConvention!: number;

  documents: ConventionDocumentEtudiant[] = [];
  message: SafeHtml | null = null;
  statusMessage: string | null = null;
  statusType: DepotDocumentsStatusType = 'info';
  tailleMaxMo = 10;

  canUpload = false;
  canDelete = false;
  canDownload = false;
  canPreview = false;

  loading = false;
  uploading = false;
  dragOver = false;

  readonly displayedColumns = ['nom', 'taille', 'depose', 'actions'];

  constructor(
    private documentService: ConventionDocumentService,
    private dialog: MatDialog,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    if (this.idConvention) {
      this.loadDocuments();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['idConvention'] && !changes['idConvention'].firstChange) {
      this.loadDocuments();
    }
  }

  loadDocuments(clearStatus = true): void {
    this.loading = true;
    if (clearStatus) {
      this.clearStatusMessage();
    }
    this.documentService.list(this.idConvention).subscribe({
      next: (response) => this.applyResponse(response),
      error: () => {
        this.loading = false;
        this.setStatusMessage('Impossible de charger les fichiers.', 'error');
      }
    });
  }

  private applyResponse(response: ConventionDocumentsResponse): void {
    this.documents = response.documents;
    this.message = response.message ? this.sanitizer.bypassSecurityTrustHtml(response.message) : null;
    this.tailleMaxMo = response.tailleMaxMo;
    this.canUpload = response.canUpload;
    this.canDelete = response.canDelete;
    this.canDownload = response.canDownload;
    this.canPreview = response.canPreview;
    this.loading = false;
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.uploadSelectedFiles(input.files);
    input.value = '';
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.canUpload || this.uploading) {
      return;
    }

    this.dragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.dragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    if (!this.canUpload || this.uploading) {
      return;
    }

    this.dragOver = false;
    this.uploadSelectedFiles(event.dataTransfer?.files ?? null);
  }

  private uploadSelectedFiles(fileList: FileList | null): void {
    if (!fileList || fileList.length === 0) {
      return;
    }

    this.clearStatusMessage();
    this.uploading = true;
    this.uploadFilesSequentially(Array.from(fileList), 0, 0, false);
  }

  private uploadFilesSequentially(files: File[], index: number, uploadedCount: number, hasError: boolean): void {
    if (index >= files.length) {
      this.uploading = false;
      if (uploadedCount > 0 && hasError) {
        this.setStatusMessage(`${uploadedCount} document${uploadedCount > 1 ? 's' : ''} ajouté${uploadedCount > 1 ? 's' : ''}. Certains fichiers ont été refusés.`, 'info');
      } else if (uploadedCount > 0) {
        this.setStatusMessage(`${uploadedCount} document${uploadedCount > 1 ? 's' : ''} ajouté${uploadedCount > 1 ? 's' : ''}.`, 'success');
      }
      return;
    }

    const file = files[index];
    const clientError = this.validateFileClientSide(file);
    if (clientError) {
      this.setStatusMessage(clientError, 'error');
      this.uploadFilesSequentially(files, index + 1, uploadedCount, true);
      return;
    }

    this.documentService.upload(this.idConvention, file).subscribe({
      next: (response) => {
        this.applyResponse(response);
        this.uploadFilesSequentially(files, index + 1, uploadedCount + 1, hasError);
      },
      error: (err) => {
        this.setStatusMessage(
          err?.error?.message || 'Le fichier doit être un PDF valide et sécurisé.',
          'error'
        );
        this.uploadFilesSequentially(files, index + 1, uploadedCount, true);
      }
    });
  }

  private validateFileClientSide(file: File): string | null {
    if (!/\.pdf$/i.test(file.name)) {
      return 'Le fichier doit être au format PDF.';
    }
    if (file.size === 0) {
      return 'Le fichier sélectionné est vide.';
    }
    const maxBytes = this.tailleMaxMo * 1024 * 1024;
    if (file.size > maxBytes) {
      return `Le fichier dépasse la taille maximale autorisée (${this.tailleMaxMo} Mo).`;
    }
    return null;
  }

  openPreview(doc: ConventionDocumentEtudiant): void {
    if (!this.canPreview) {
      return;
    }

    const ref = this.dialog.open(DepotDocumentPreviewDialogComponent, {
      width: '100vw',
      height: '100vh',
      maxWidth: '100vw',
      maxHeight: '100vh',
      panelClass: 'depot-document-preview-dialog-panel',
      autoFocus: false,
      data: <PreviewDialogData>{
        idConvention: this.idConvention,
        document: doc,
        canDelete: this.canDelete
      }
    });

    ref.afterClosed().subscribe(result => {
      if (result === 'deleted') {
        this.setStatusMessage('Document supprimé.', 'success');
        this.loadDocuments(false);
      }
    });
  }

  downloadDoc(doc: ConventionDocumentEtudiant): void {
    this.documentService.download(this.idConvention, doc.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = doc.nomReel;
        link.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.setStatusMessage('Erreur lors du téléchargement du document.', 'error');
      }
    });
  }

  deleteDoc(doc: ConventionDocumentEtudiant): void {
    this.documentService.delete(this.idConvention, doc.id).subscribe({
      next: (response) => {
        this.applyResponse(response);
        this.setStatusMessage('Document supprimé.', 'success');
      },
      error: () => {
        this.setStatusMessage('Erreur lors de la suppression du document.', 'error');
      }
    });
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} o`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} Ko`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(2)} Mo`;
  }

  getStatusIcon(): string {
    if (this.statusType === 'success') {
      return 'check_circle';
    }
    if (this.statusType === 'error') {
      return 'error_outline';
    }
    return 'info';
  }

  private setStatusMessage(message: string, type: DepotDocumentsStatusType): void {
    this.statusMessage = message;
    this.statusType = type;
  }

  private clearStatusMessage(): void {
    this.statusMessage = null;
    this.statusType = 'info';
  }
}
