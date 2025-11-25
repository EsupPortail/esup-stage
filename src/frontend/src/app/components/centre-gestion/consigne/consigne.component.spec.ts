import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConsigneComponent } from './consigne.component';

describe('ConsigneComponent', () => {
  let component: ConsigneComponent;
  let fixture: ComponentFixture<ConsigneComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConsigneComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConsigneComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
