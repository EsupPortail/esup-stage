export interface ConventionDocumentEtudiant {
  id: number;
  nomReel: string;
  contentType: string;
  taille: number;       // octets
  sha256: string;
  loginCreation: string;
  dateCreation: string; // ISO 8601
}

export interface ConventionDocumentsResponse {
  message: string | null;   // HTML paramétré, global établissement
  tailleMaxMo: number;
  canUpload: boolean;
  canDelete: boolean;
  canDownload: boolean;
  canPreview: boolean;
  documents: ConventionDocumentEtudiant[];
}

export type ConventionDocumentAction = 'READ' | 'PREVIEW' | 'DOWNLOAD' | 'UPLOAD' | 'DELETE';
