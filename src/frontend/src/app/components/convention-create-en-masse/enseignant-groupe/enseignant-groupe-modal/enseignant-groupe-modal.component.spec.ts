import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnseignantGroupeModalComponent } from './enseignant-groupe-modal.component';

describe('EnseignantGroupeModalComponent', () => {
  let component: EnseignantGroupeModalComponent;
  let fixture: ComponentFixture<EnseignantGroupeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnseignantGroupeModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EnseignantGroupeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
