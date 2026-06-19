import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DepotDocumentPreviewDialogComponent } from './preview-dialog.component';

describe('DepotDocumentPreviewDialogComponent', () => {
  let component: DepotDocumentPreviewDialogComponent;
  let fixture: ComponentFixture<DepotDocumentPreviewDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DepotDocumentPreviewDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DepotDocumentPreviewDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
