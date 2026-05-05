import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogsDisplayComponent } from './logs-display.component';

describe('LogsDisplayComponent', () => {
  let component: LogsDisplayComponent;
  let fixture: ComponentFixture<LogsDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogsDisplayComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogsDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
