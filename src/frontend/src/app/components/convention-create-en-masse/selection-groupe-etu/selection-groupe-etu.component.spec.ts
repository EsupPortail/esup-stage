import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectionGroupeEtuComponent } from './selection-groupe-etu.component';

describe('SelectionGroupeEtuComponent', () => {
  let component: SelectionGroupeEtuComponent;
  let fixture: ComponentFixture<SelectionGroupeEtuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectionGroupeEtuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectionGroupeEtuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
