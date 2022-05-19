import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmDeleteCentreComponent } from './confirm-delete-centre.component';

describe('ConfirmDeleteCentreComponent', () => {
  let component: ConfirmDeleteCentreComponent;
  let fixture: ComponentFixture<ConfirmDeleteCentreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfirmDeleteCentreComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmDeleteCentreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
