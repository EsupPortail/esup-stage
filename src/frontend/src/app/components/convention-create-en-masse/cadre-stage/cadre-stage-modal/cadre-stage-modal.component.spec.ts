import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CadreStageModalComponent } from './cadre-stage-modal.component';

describe('CadreStageModalComponent', () => {
  let component: CadreStageModalComponent;
  let fixture: ComponentFixture<CadreStageModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CadreStageModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CadreStageModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
