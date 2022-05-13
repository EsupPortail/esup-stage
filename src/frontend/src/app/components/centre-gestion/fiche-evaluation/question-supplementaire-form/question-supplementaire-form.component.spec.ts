import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuestionSupplementaireFormComponent } from './question-supplementaire-form.component';

describe('QuestionSupplementaireFormComponent', () => {
  let component: QuestionSupplementaireFormComponent;
  let fixture: ComponentFixture<QuestionSupplementaireFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuestionSupplementaireFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QuestionSupplementaireFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
