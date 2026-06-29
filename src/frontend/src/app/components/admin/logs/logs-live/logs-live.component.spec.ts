import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogsLiveComponent } from './logs-live.component';

describe('LogsLiveComponent', () => {
  let component: LogsLiveComponent;
  let fixture: ComponentFixture<LogsLiveComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogsLiveComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogsLiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
