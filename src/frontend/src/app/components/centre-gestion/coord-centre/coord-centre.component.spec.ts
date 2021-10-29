import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoordCentreComponent } from './coord-centre.component';

describe('CoordCentreComponent', () => {
  let component: CoordCentreComponent;
  let fixture: ComponentFixture<CoordCentreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CoordCentreComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CoordCentreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
