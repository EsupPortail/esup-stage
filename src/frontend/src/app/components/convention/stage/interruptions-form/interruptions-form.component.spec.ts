import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InterruptionsFormComponent } from './interruptions-form.component';

describe('InterruptionsFormComponent', () => {
  let component: InterruptionsFormComponent;
  let fixture: ComponentFixture<InterruptionsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InterruptionsFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InterruptionsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
