import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ConventionDocumentService } from '../../../../services/convention-documents.service';
import { ConventionDocumentEtudiant } from '../../../../models/convention-document.model';

export interface PreviewDialogData {
  idConvention: number;
  document: ConventionDocumentEtudiant;
  canDelete: boolean;
}

/**
 * Résultat retourné à la fermeture de la modale :
 * - 'deleted' si le document a été supprimé depuis la preview
 * - undefined sinon (simple fermeture)
 */
export type PreviewDialogResult = 'deleted' | undefined;

@Component({
  selector: 'app-depot-document-preview-dialog',
  standalone: false,
  templateUrl: './preview-dialog.component.html',
  styleUrls: ['./preview-dialog.component.scss']
})
export class DepotDocumentPreviewDialogComponent implements OnInit, OnDestroy {

  loading = true;
  previewFailed = false;
  safePdfUrl: SafeResourceUrl | null = null;

  private objectUrl: string | null = null;

  constructor(
    public dialogRef: MatDialogRef<DepotDocumentPreviewDialogComponent, PreviewDialogResult>,
    @Inject(MAT_DIALOG_DATA) public data: PreviewDialogData,
    private documentService: ConventionDocumentService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadPreview();
  }

  ngOnDestroy(): void {
    // Révocation systématique de l'URL locale : on ne garde jamais
    // le contenu du PDF accessible après fermeture de la modale.
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
    }
  }

  private loadPreview(): void {
    this.loading = true;
    this.previewFailed = false;

    this.documentService.preview(this.data.idConvention, this.data.document.id).subscribe({
      next: (blob: Blob | MediaSource) => {
        this.objectUrl = URL.createObjectURL(blob);
        this.safePdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.objectUrl);
        this.loading = false;
      },
      error: () => {
        this.previewFailed = true;
        this.loading = false;
      }
    });
  }

  download(): void {
    this.documentService.download(this.data.idConvention, this.data.document.id).subscribe((blob: Blob | MediaSource) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = this.data.document.nomReel;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  /**
   * Appelé via (confirm) de la directive de confirmation maison, placée
   * dans le template sur le bouton "Supprimer". La confirmation a donc
   * déjà eu lieu avant l'appel de cette méthode.
   */
  delete(): void {
    this.documentService.delete(this.data.idConvention, this.data.document.id).subscribe(() => {
      this.dialogRef.close('deleted');
    });
  }

  close(): void {
    this.dialogRef.close();
  }
}
