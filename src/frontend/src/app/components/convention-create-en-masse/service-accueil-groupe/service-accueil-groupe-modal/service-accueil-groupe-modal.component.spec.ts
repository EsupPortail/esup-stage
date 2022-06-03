import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceAccueilGroupeModalComponent } from './service-accueil-groupe-modal.component';

describe('ServiceAccueilGroupeModalComponent', () => {
  let component: ServiceAccueilGroupeModalComponent;
  let fixture: ComponentFixture<ServiceAccueilGroupeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceAccueilGroupeModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceAccueilGroupeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
