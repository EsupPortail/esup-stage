import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from "@angular/core";

import { MaintenanceComponent } from './maintenance.component';
import { MaintenanceAdminService } from "../../../services/maintenance-admin.service";
import { MessageService } from "../../../services/message.service";
import { MatDialog } from "@angular/material/dialog";

describe('MaintenanceComponent', () => {
  let component: MaintenanceComponent;
  let fixture: ComponentFixture<MaintenanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MaintenanceComponent],
      providers: [
        { provide: MaintenanceAdminService, useValue: {} },
        { provide: MessageService, useValue: { setSuccess: () => undefined } },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => ({ subscribe: () => undefined }) }) } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MaintenanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
