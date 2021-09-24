import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigGeneraleComponent } from './config-generale.component';

describe('ConfigGeneraleComponent', () => {
  let component: ConfigGeneraleComponent;
  let fixture: ComponentFixture<ConfigGeneraleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConfigGeneraleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigGeneraleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
