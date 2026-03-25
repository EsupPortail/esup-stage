export interface FileElement {
  id: string;
  name: string;
  path: string;
  isFolder: boolean;
  size?: number;
  lastModified?: Date;
  extension?: string;
  parent: string;
}

export interface FileContent {
  fileName: string;
  content: string;
  totalLines: number;
  page: number;
  pageSize: number;
}

export interface ZipResult {
  downloadUrl: string;
}

export type ExportFormat = 'original' | 'zip' | 'csv';
