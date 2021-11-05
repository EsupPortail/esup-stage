import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionEtabAccueilComponent } from './gestion-etab-accueil.component';

describe('GestionEtabAccueilComponent', () => {
  let component: GestionEtabAccueilComponent;
  let fixture: ComponentFixture<GestionEtabAccueilComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionEtabAccueilComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GestionEtabAccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
