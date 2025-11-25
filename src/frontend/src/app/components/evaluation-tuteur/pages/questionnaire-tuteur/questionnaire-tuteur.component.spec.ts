import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionnaireTuteurComponent } from './questionnaire-tuteur.component';

describe('QuestionnaireTuteurComponent', () => {
  let component: QuestionnaireTuteurComponent;
  let fixture: ComponentFixture<QuestionnaireTuteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QuestionnaireTuteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QuestionnaireTuteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
