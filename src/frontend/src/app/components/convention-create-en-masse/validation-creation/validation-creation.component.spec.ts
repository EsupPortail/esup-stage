import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ValidationCreationComponent } from './validation-creation.component';

describe('ValidationCreationComponent', () => {
  let component: ValidationCreationComponent;
  let fixture: ComponentFixture<ValidationCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ValidationCreationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ValidationCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
