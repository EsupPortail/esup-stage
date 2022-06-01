import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadreStageComponent } from './cadre-stage.component';

describe('CadreStageComponent', () => {
  let component: CadreStageComponent;
  let fixture: ComponentFixture<CadreStageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CadreStageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CadreStageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
