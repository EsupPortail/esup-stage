import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConventionCreateEnMasseComponent } from './convention-create-en-masse.component';

describe('ConventionCreateEnMasseComponent', () => {
  let component: ConventionCreateEnMasseComponent;
  let fixture: ComponentFixture<ConventionCreateEnMasseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConventionCreateEnMasseComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConventionCreateEnMasseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
