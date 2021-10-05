import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CentreGestionSearchComponent } from './centre-gestion-search.component';

describe('CentreGestionSearchComponent', () => {
  let component: CentreGestionSearchComponent;
  let fixture: ComponentFixture<CentreGestionSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CentreGestionSearchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CentreGestionSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
