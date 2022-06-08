import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TuteurAccueilGroupeComponent } from './tuteur-accueil-groupe.component';

describe('TuteurAccueilGroupeComponent', () => {
  let component: TuteurAccueilGroupeComponent;
  let fixture: ComponentFixture<TuteurAccueilGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TuteurAccueilGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TuteurAccueilGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
