import { Component } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import { FileElement } from '../../../models/file-element.model';
import { LogsViewerComponent } from './logs-explorer/logs-viewer/logs-viewer.component';

@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss'],
  standalone: false
})
export class LogsComponent {
  private viewerDialogRef?: MatDialogRef<LogsViewerComponent>;

  constructor(private dialog: MatDialog) {}

  openFile(file: FileElement): void {
    this.viewerDialogRef?.close();
    this.viewerDialogRef = this.dialog.open(LogsViewerComponent, {
      autoFocus: false,
      restoreFocus: false,
      width: '96vw',
      maxWidth: '1240px',
      height: '90vh',
      panelClass: 'logs-viewer-dialog-container',
      data: { file: { ...file } }
    });

    this.viewerDialogRef.afterClosed().subscribe(() => {
      this.viewerDialogRef = undefined;
    });
  }
}