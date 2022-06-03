import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EtabAccueilGroupeComponent } from './etab-accueil-groupe.component';

describe('EtabAccueilGroupeComponent', () => {
  let component: EtabAccueilGroupeComponent;
  let fixture: ComponentFixture<EtabAccueilGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EtabAccueilGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EtabAccueilGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
