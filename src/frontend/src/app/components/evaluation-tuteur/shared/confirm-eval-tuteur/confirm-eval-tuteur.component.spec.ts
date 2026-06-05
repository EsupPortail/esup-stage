import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmEvalTuteurComponent } from './confirm-eval-tuteur.component';

describe('ConfirmEvalTuteurComponent', () => {
  let component: ConfirmEvalTuteurComponent;
  let fixture: ComponentFixture<ConfirmEvalTuteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmEvalTuteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConfirmEvalTuteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
