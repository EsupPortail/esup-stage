import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EtabAccueilFormComponent } from './etab-accueil-form.component';

describe('EtabAccueilFormComponent', () => {
  let component: EtabAccueilFormComponent;
  let fixture: ComponentFixture<EtabAccueilFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EtabAccueilFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EtabAccueilFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
