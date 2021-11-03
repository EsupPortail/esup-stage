import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ParamCentreComponent } from './param-centre.component';

describe('ParamCentreComponent', () => {
  let component: ParamCentreComponent;
  let fixture: ComponentFixture<ParamCentreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ParamCentreComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ParamCentreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
