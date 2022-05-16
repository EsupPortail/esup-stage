import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvalStageComponent } from './eval-stage.component';

describe('EvalStageComponent', () => {
  let component: EvalStageComponent;
  let fixture: ComponentFixture<EvalStageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EvalStageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EvalStageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
