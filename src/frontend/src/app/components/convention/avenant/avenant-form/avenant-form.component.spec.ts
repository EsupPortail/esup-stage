import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AvenantFormComponent } from './avenant-form.component';

describe('AvenantFormComponent', () => {
  let component: AvenantFormComponent;
  let fixture: ComponentFixture<AvenantFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AvenantFormComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AvenantFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
