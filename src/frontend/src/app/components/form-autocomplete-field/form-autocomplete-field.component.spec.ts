import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormAutocompleteFieldComponent } from './form-autocomplete-field.component';

describe('FormAutocompleteFieldComponent', () => {
  let component: FormAutocompleteFieldComponent;
  let fixture: ComponentFixture<FormAutocompleteFieldComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FormAutocompleteFieldComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FormAutocompleteFieldComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
