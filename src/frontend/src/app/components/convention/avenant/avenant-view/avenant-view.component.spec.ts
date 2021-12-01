import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvenantViewComponent } from './avenant-view.component';

describe('AvenantViewComponent', () => {
  let component: AvenantViewComponent;
  let fixture: ComponentFixture<AvenantViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AvenantViewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AvenantViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
