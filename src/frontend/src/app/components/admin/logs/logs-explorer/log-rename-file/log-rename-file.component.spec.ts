import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogRenameFileComponent } from './log-rename-file.component';

describe('LogRenameFileComponent', () => {
  let component: LogRenameFileComponent;
  let fixture: ComponentFixture<LogRenameFileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogRenameFileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogRenameFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
