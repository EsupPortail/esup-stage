import { Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ConventionDocumentService } from '../../../services/convention-documents.service';
import { ConventionDocumentEtudiant, ConventionDocumentsResponse } from '../../../models/convention-document.model';
import { DepotDocumentPreviewDialogComponent, PreviewDialogData } from './preview-dialog/preview-dialog.component';
import { ConfirmComponent } from '../../confirm/confirm.component';

type DepotDocumentsStatusType = 'success' | 'error' | 'info';
type UploadResultStatus = 'success' | 'error' | 'info';

interface UploadResult {
  fileName: string;
  status: UploadResultStatus;
  message: string;
}

interface PendingReplacement {
  files: File[];
  index: number;
  successCount: number;
  skippedCount: number;
  hasError: boolean;
  file: File;
}

@Component({
  selector: 'app-depot-documents',
  standalone: false,
  templateUrl: './depot-documents.component.html',
  styleUrls: ['./depot-documents.component.scss']
})
export class DepotDocumentsComponent implements OnInit, OnChanges {

  @Input() idConvention!: number;
  @ViewChild('replaceConfirm') replaceConfirm!: ConfirmComponent;

  documents: ConventionDocumentEtudiant[] = [];
  statusMessage: string | null = null;
  statusType: DepotDocumentsStatusType = 'info';
  uploadResults: UploadResult[] = [];
  replaceConfirmMessage = '';
  tailleMaxMo = 10;

  canUpload = false;
  canDelete = false;
  canDownload = false;
  canPreview = false;

  loading = false;
  uploading = false;
  dragOver = false;

  readonly displayedColumns = ['nom', 'taille', 'depose', 'actions'];

  private pendingReplacement: PendingReplacement | null = null;

  constructor(
    private readonly documentService: ConventionDocumentService,
    private readonly dialog: MatDialog
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
      this.uploadResults = [];
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
    this.uploadResults = [];
    this.uploading = true;
    this.uploadFilesSequentially(Array.from(fileList), 0, 0, 0, false);
  }

  private uploadFilesSequentially(files: File[], index: number, successCount: number, skippedCount: number, hasError: boolean): void {
    if (index >= files.length) {
      this.uploading = false;
      this.setUploadSummary(files.length, successCount, skippedCount, hasError);
      return;
    }

    const file = files[index];
    const clientError = this.validateFileClientSide(file);
    if (clientError) {
      this.addUploadResult(file.name, 'error', clientError);
      this.uploadFilesSequentially(files, index + 1, successCount, skippedCount, true);
      return;
    }

    if (this.documents.some((doc) => doc.nomReel === file.name)) {
      this.askReplaceConfirmation(files, index, successCount, skippedCount, hasError, file);
      return;
    }

    this.uploadFile(files, index, successCount, skippedCount, hasError, false);
  }

  private askReplaceConfirmation(files: File[], index: number, successCount: number, skippedCount: number, hasError: boolean, file: File): void {
    this.pendingReplacement = { files, index, successCount, skippedCount, hasError, file };
    this.replaceConfirmMessage = `Un document nommé <strong>${this.escapeHtml(file.name)}</strong> existe déjà. Voulez-vous remplacer le document existant ?`;
    this.replaceConfirm.onClick();
    this.replaceConfirm.dialogRef.afterClosed().subscribe(() => {
      if (this.pendingReplacement?.file !== file) {
        return;
      }
      this.pendingReplacement = null;
      this.addUploadResult(file.name, 'info', 'Remplacement annulé, document existant conservé.');
      this.uploadFilesSequentially(files, index + 1, successCount, skippedCount + 1, hasError);
    });
  }

  confirmReplace(): void {
    if (!this.pendingReplacement) {
      return;
    }

    const pending = this.pendingReplacement;
    this.pendingReplacement = null;
    this.uploadFile(pending.files, pending.index, pending.successCount, pending.skippedCount, pending.hasError, true);
  }

  private uploadFile(files: File[], index: number, successCount: number, skippedCount: number, hasError: boolean, remplacer: boolean): void {
    const file = files[index];
    this.documentService.upload(this.idConvention, file, remplacer).subscribe({
      next: (response) => {
        this.applyResponse(response);
        this.addUploadResult(file.name, 'success', remplacer ? 'Document modifié.' : 'Document ajouté.');
        this.uploadFilesSequentially(files, index + 1, successCount + 1, skippedCount, hasError);
      },
      error: (err) => {
        if (!remplacer && err?.status === 409) {
          this.askReplaceConfirmation(files, index, successCount, skippedCount, hasError, file);
          return;
        }
        this.addUploadResult(
          file.name,
          'error',
          err?.error?.message || 'Le fichier doit être un PDF valide et sécurisé.'
        );
        this.uploadFilesSequentially(files, index + 1, successCount, skippedCount, true);
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
        this.uploadResults = [];
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
        this.uploadResults = [];
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

  dismissStatusMessages(): void {
    this.clearStatusMessage();
    this.uploadResults = [];
  }

  getUploadResultIcon(result: UploadResult): string {
    if (result.status === 'success') {
      return 'check_circle';
    }
    if (result.status === 'error') {
      return 'error_outline';
    }
    return 'info';
  }

  private setUploadSummary(totalCount: number, successCount: number, skippedCount: number, hasError: boolean): void {
    const rejectedCount = totalCount - successCount - skippedCount;
    const successLabel = `${successCount} document${successCount > 1 ? 's' : ''} ajouté${successCount > 1 ? 's' : ''} ou modifié${successCount > 1 ? 's' : ''}`;

    if (successCount > 0 && (rejectedCount > 0 || skippedCount > 0)) {
      const details = [];
      if (rejectedCount > 0) {
        details.push(`${rejectedCount} refusé${rejectedCount > 1 ? 's' : ''}`);
      }
      if (skippedCount > 0) {
        details.push(`${skippedCount} ignoré${skippedCount > 1 ? 's' : ''}`);
      }
      this.setStatusMessage(`${successLabel}, ${details.join(', ')}.`, hasError ? 'info' : 'success');
      return;
    }
    if (successCount > 0) {
      this.setStatusMessage(`${successLabel}.`, 'success');
      return;
    }
    if (hasError) {
      this.setStatusMessage('Aucun document ajouté ou modifié. Consultez le détail ci-dessous.', 'error');
      return;
    }
    if (skippedCount > 0) {
      this.setStatusMessage('Aucun document modifié.', 'info');
    }
  }

  private addUploadResult(fileName: string, status: UploadResultStatus, message: string): void {
    this.uploadResults = [...this.uploadResults, { fileName, status, message }];
  }

  private setStatusMessage(message: string, type: DepotDocumentsStatusType): void {
    this.statusMessage = message;
    this.statusType = type;
  }

  private clearStatusMessage(): void {
    this.statusMessage = null;
    this.statusType = 'info';
  }

  private escapeHtml(value: string): string {
    const div = document.createElement('div');
    div.innerText = value;
    return div.innerHTML;
  }
}
