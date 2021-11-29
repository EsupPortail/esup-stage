import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogoCentreComponent } from './logo-centre.component';

describe('LogoCentreComponent', () => {
  let component: LogoCentreComponent;
  let fixture: ComponentFixture<LogoCentreComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LogoCentreComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LogoCentreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
