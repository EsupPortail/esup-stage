import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidationCardComponent } from './validation-card.component';

describe('ValidationCardComponent', () => {
  let component: ValidationCardComponent;
  let fixture: ComponentFixture<ValidationCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ValidationCardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ValidationCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
