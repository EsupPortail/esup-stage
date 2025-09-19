import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireCompletTuteurComponent } from './questionnaire-complet-tuteur.component';

describe('QuestionnaireCompletTuteurComponent', () => {
  let component: QuestionnaireCompletTuteurComponent;
  let fixture: ComponentFixture<QuestionnaireCompletTuteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionnaireCompletTuteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionnaireCompletTuteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
