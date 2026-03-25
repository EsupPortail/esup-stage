import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogNewFolderDialogComponent } from './log-new-folder-dialog.component';

describe('LogNewFolderDialogComponent', () => {
  let component: LogNewFolderDialogComponent;
  let fixture: ComponentFixture<LogNewFolderDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogNewFolderDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogNewFolderDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
