import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnseignantReferentComponent } from './enseignant-referent.component';

describe('EnseignantReferentComponent', () => {
  let component: EnseignantReferentComponent;
  let fixture: ComponentFixture<EnseignantReferentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnseignantReferentComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EnseignantReferentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
