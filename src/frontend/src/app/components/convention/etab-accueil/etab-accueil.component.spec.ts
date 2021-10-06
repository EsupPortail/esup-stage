import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EtabAccueilComponent } from './etab-accueil.component';

describe('EtabAccueilComponent', () => {
  let component: EtabAccueilComponent;
  let fixture: ComponentFixture<EtabAccueilComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EtabAccueilComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EtabAccueilComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
