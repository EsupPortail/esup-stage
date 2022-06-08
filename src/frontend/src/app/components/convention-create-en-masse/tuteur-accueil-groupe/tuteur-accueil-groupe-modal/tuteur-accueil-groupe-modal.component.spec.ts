import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TuteurAccueilGroupeModalComponent } from './tuteur-accueil-groupe-modal.component';

describe('TuteurAccueilGroupeModalComponent', () => {
  let component: TuteurAccueilGroupeModalComponent;
  let fixture: ComponentFixture<TuteurAccueilGroupeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TuteurAccueilGroupeModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TuteurAccueilGroupeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
