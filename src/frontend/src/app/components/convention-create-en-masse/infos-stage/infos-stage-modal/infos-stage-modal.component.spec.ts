import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InfosStageModalComponent } from './infos-stage-modal.component';

describe('InfosStageModalComponent', () => {
  let component: InfosStageModalComponent;
  let fixture: ComponentFixture<InfosStageModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InfosStageModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InfosStageModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
