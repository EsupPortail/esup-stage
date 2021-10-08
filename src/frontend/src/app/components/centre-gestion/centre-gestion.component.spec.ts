import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CentreGestionComponent } from './centre-gestion.component';

describe('CentreGestionComponent', () => {
  let component: CentreGestionComponent;
  let fixture: ComponentFixture<CentreGestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CentreGestionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CentreGestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
