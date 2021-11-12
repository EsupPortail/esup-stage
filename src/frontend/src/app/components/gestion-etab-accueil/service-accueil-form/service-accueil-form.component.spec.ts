import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceAccueilFormComponent } from './service-accueil-form.component';

describe('ServiceAccueilFormComponent', () => {
  let component: ServiceAccueilFormComponent;
  let fixture: ComponentFixture<ServiceAccueilFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ServiceAccueilFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ServiceAccueilFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
