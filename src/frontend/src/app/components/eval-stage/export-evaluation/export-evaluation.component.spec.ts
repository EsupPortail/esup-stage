import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExportEvaluationComponent } from './export-evaluation.component';

describe('ExportEvaluationComponent', () => {
  let component: ExportEvaluationComponent;
  let fixture: ComponentFixture<ExportEvaluationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExportEvaluationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExportEvaluationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
