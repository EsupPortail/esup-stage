import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccueilTuteurComponent } from './accueil-tuteur.component';

describe('AccueilTuteurComponent', () => {
  let component: AccueilTuteurComponent;
  let fixture: ComponentFixture<AccueilTuteurComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccueilTuteurComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccueilTuteurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
