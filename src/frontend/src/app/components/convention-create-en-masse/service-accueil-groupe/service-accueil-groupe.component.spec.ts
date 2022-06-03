import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceAccueilGroupeComponent } from './service-accueil-groupe.component';

describe('ServiceAccueilGroupeComponent', () => {
  let component: ServiceAccueilGroupeComponent;
  let fixture: ComponentFixture<ServiceAccueilGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceAccueilGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceAccueilGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
