import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnseignantGroupeComponent } from './enseignant-groupe.component';

describe('EnseignantGroupeComponent', () => {
  let component: EnseignantGroupeComponent;
  let fixture: ComponentFixture<EnseignantGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnseignantGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EnseignantGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
