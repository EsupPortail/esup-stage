import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnvoiMailEnMasseEvalComponent } from './envoi-mail-en-masse-eval.component';

describe('EnvoiMailEnMasseEvalComponent', () => {
  let component: EnvoiMailEnMasseEvalComponent;
  let fixture: ComponentFixture<EnvoiMailEnMasseEvalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvoiMailEnMasseEvalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnvoiMailEnMasseEvalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
