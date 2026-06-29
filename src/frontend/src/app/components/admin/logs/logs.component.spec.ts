import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EMPTY, of } from 'rxjs';
import { FormsModule } from '@angular/forms';

import { LogsComponent } from './logs.component';
import { LogsService } from '../../../services/logs.service';

describe('LogsComponent', () => {
  let component: LogsComponent;
  let fixture: ComponentFixture<LogsComponent>;
  const logsServiceMock = {
    status$: of('disconnected'),
    lines$: EMPTY,
    connect: jasmine.createSpy('connect'),
    disconnect: jasmine.createSpy('disconnect'),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [LogsComponent],
      imports: [FormsModule],
      providers: [
        { provide: LogsService, useValue: logsServiceMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
