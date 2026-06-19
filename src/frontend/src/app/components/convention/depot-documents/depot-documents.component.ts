import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ConventionDocumentService } from '../../../services/convention-documents.service';
import {ConventionDocumentEtudiant, ConventionDocumentsResponse} from '../../../models/convention-document.model';
import {DepotDocumentPreviewDialogComponent, PreviewDialogData} from './preview-dialog/preview-dialog.component';

/**
 * Onglet "Dépôt de documents" d'une convention.
 *
 * Reste visible quel que soit l'état de validation/signature et n'entre
 * jamais dans le calcul de complétude (cf. spec). À insérer juste après
 * l'onglet "Stage" dans convention.component.html.
 *
 * Usage : <app-depot-documents [idConvention]="convention.idConvention"></app-depot-documents>
 */
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
  tailleMaxMo = 10;

  canUpload = false;
  canDelete = false;
  canDownload = false;
  canPreview = false;

  loading = false;
  uploading = false;

  readonly displayedColumns = ['nom', 'taille', 'depose', 'actions'];

  constructor(
    private documentService: ConventionDocumentService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
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

  loadDocuments(): void {
    this.loading = true;
    this.documentService.list(this.idConvention).subscribe({
      next: (response) => this.applyResponse(response),
      error: () => {
        this.loading = false;
        this.snackBar.open('Erreur lors du chargement des documents.', 'Fermer', { duration: 4000 });
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

  /** Gère la sélection multiple : upload séquentiel, fichier par fichier (spec API : 1 fichier / requête). */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;
    if (!files || files.length === 0) {
      return;
    }

    this.uploadFilesSequentially(Array.from(files), 0);
    input.value = ''; // permet de re-sélectionner le même fichier plus tard
  }

  private uploadFilesSequentially(files: File[], index: number): void {
    if (index >= files.length) {
      return;
    }

    const file = files[index];
    const clientError = this.validateFileClientSide(file);
    if (clientError) {
      this.snackBar.open(clientError, 'Fermer', { duration: 5000 });
      this.uploadFilesSequentially(files, index + 1);
      return;
    }

    this.uploading = true;
    this.documentService.upload(this.idConvention, file).subscribe({
      next: (response) => {
        this.applyResponse(response);
        this.uploading = false;
        this.snackBar.open('Document ajouté.', 'Fermer', { duration: 3000 });
        this.uploadFilesSequentially(files, index + 1);
      },
      error: (err) => {
        this.uploading = false;
        const msg = err?.error?.message || 'Le fichier doit être un PDF valide et sécurisé.';
        this.snackBar.open(msg, 'Fermer', { duration: 5000 });
        this.uploadFilesSequentially(files, index + 1);
      }
    });
  }

  /**
   * Contrôle côté front, purement indicatif/confort utilisateur.
   * La validation de sécurité réelle est entièrement backend (cf. spec).
   */
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
      width: '720px',
      maxWidth: '95vw',
      autoFocus: false,
      data: <PreviewDialogData>{
        idConvention: this.idConvention,
        document: doc,
        canDelete: this.canDelete
      }
    });

    ref.afterClosed().subscribe(result => {
      if (result === 'deleted') {
        this.snackBar.open('Document supprimé.', 'Fermer', { duration: 3000 });
        this.loadDocuments();
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
        this.snackBar.open('Erreur lors du téléchargement du document.', 'Fermer', { duration: 4000 });
      }
    });
  }

  /**
   * Appelé via (confirm) de la directive de confirmation maison, placée
   * dans le template sur le bouton de suppression. La confirmation a
   * donc déjà eu lieu avant l'appel de cette méthode.
   */
  deleteDoc(doc: ConventionDocumentEtudiant): void {
    this.documentService.delete(this.idConvention, doc.id).subscribe({
      next: (response) => {
        this.applyResponse(response);
        this.snackBar.open('Document supprimé.', 'Fermer', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression du document.', 'Fermer', { duration: 4000 });
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
}
