import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConventionCreateComponent } from './convention-create.component';

describe('ConventionCreateComponent', () => {
  let component: ConventionCreateComponent;
  let fixture: ComponentFixture<ConventionCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConventionCreateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConventionCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
