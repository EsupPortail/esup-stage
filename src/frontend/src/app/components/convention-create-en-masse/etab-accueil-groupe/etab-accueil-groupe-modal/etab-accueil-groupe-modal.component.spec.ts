import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EtabAccueilGroupeModalComponent } from './etab-accueil-groupe-modal.component';

describe('EtabAccueilGroupeModalComponent', () => {
  let component: EtabAccueilGroupeModalComponent;
  let fixture: ComponentFixture<EtabAccueilGroupeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EtabAccueilGroupeModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EtabAccueilGroupeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
