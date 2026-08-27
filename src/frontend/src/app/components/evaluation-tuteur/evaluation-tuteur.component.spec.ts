import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluationTuteurComponent } from './evaluation-tuteur.component';

describe('EvaluationTuteurComponent', () => {
  let component: EvaluationTuteurComponent;
  let fixture: ComponentFixture<EvaluationTuteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EvaluationTuteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluationTuteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
