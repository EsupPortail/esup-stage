import { formatDate } from '@angular/common';
import { Component, ViewChild } from '@angular/core';
import { MatDialog } from "@angular/material/dialog";
import { MessageService } from "../../../services/message.service";
import { MaintenanceAdminService } from "../../../services/maintenance-admin.service";
import { TableComponent } from "../../table/table.component";
import { MaintenanceDialogComponent } from "./maintenance-dialog/maintenance-dialog.component";

@Component({
  selector: 'app-maintenance',
  templateUrl: './maintenance.component.html',
  styleUrls: ['./maintenance.component.scss'],
  standalone: false
})
export class MaintenanceComponent {

  @ViewChild(TableComponent) appTable: TableComponent | undefined;

  columns = ['id', 'datDebMaint', 'datFinMaint', 'datAlertMaint', 'message', 'createdBy', 'createdAt', 'action'];
  sortColumn = 'id';
  sortDirection: 'asc' | 'desc' = 'desc';

  filters = [
    { id: 'id', libelle: 'ID', type: 'int' },
    { id: 'message', libelle: 'Message' },
    { id: 'createdBy', libelle: 'Créé par' },
  ];

  constructor(
    public maintenanceService: MaintenanceAdminService,
    private messageService: MessageService,
    private dialog: MatDialog
  ) {}

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(MaintenanceDialogComponent, {
      width: '700px',
    });

    dialogRef.afterClosed().subscribe((payload: any) => {
      if (!payload) {
        return;
      }

      this.maintenanceService.create(payload).subscribe(() => {
        this.messageService.setSuccess('Maintenance créée');
        this.appTable?.update();
      });
    });
  }

  openEditDialog(row: any): void {
    const dialogRef = this.dialog.open(MaintenanceDialogComponent, {
      width: '700px',
      data: {
        maintenance: row,
        mode: 'edit'
      }
    });

    dialogRef.afterClosed().subscribe((payload: any) => {
      if (!payload) {
        return;
      }

      this.maintenanceService.update(row.id, payload).subscribe(() => {
        this.messageService.setSuccess('Maintenance modifiée');
        this.appTable?.update();
      });
    });
  }

  activate(row: any): void {
    this.maintenanceService.activate(row.id).subscribe(() => {
      this.messageService.setSuccess('Maintenance activée');
      this.appTable?.update();
    });
  }

  deactivate(row: any): void {
    this.maintenanceService.deactivate(row.id).subscribe(() => {
      this.messageService.setSuccess('Maintenance désactivée');
      this.appTable?.update();
    });
  }

  delete(row: any): void {
    this.maintenanceService.delete(row.id).subscribe(() => {
      this.messageService.setSuccess('Maintenance supprimée');
      this.appTable?.update();
    });
  }

  isActive(row: any): boolean {
    const now = Date.now();
    const startDate = this.toDate(row?.datDebMaint);
    const endDate = this.toDate(row?.datFinMaint);
    const start = startDate ? startDate.getTime() : null;
    const end = endDate ? endDate.getTime() : null;

    if (start === null || Number.isNaN(start)) {
      return false;
    }

    return start <= now && (end === null || Number.isNaN(end) || end > now);
  }

  formatMaintenanceDate(value: any): string {
    const date = this.toDate(value);
    return date ? formatDate(date, 'short', 'fr-FR') : '-';
  }

  private toDate(value: any): Date | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }

    if (value instanceof Date) {
      return Number.isNaN(value.getTime()) ? null : value;
    }

    if (Array.isArray(value)) {
      return this.buildDateFromParts(value);
    }

    if (typeof value === 'number') {
      const date = new Date(value);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    if (typeof value === 'string') {
      const raw = value.trim();
      if (!raw) {
        return null;
      }

      const commaSeparated = raw.replace(/^\[|\]$/g, '');
      if (/^\d{4},\d{1,2},\d{1,2}(,\d{1,2}){0,4}$/.test(commaSeparated)) {
        const parts = commaSeparated.split(',').map((part) => Number(part.trim()));
        return this.buildDateFromParts(parts);
      }

      const normalized = raw.includes('T') ? raw : raw.replace(' ', 'T');
      const date = new Date(normalized);
      return Number.isNaN(date.getTime()) ? null : date;
    }

    return null;
  }

  private buildDateFromParts(parts: number[]): Date | null {
    const [year, month, day, hour = 0, minute = 0, second = 0, nano = 0] = parts;
    if (!year || !month || !day) {
      return null;
    }

    const milliseconds = Math.floor((Number(nano) || 0) / 1_000_000);
    const date = new Date(
      Number(year),
      Number(month) - 1,
      Number(day),
      Number(hour) || 0,
      Number(minute) || 0,
      Number(second) || 0,
      milliseconds
    );

    return Number.isNaN(date.getTime()) ? null : date;
  }
}
