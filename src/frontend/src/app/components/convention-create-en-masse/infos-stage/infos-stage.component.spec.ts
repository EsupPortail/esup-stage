import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InfosStageComponent } from './infos-stage.component';

describe('InfosStageComponent', () => {
  let component: InfosStageComponent;
  let fixture: ComponentFixture<InfosStageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InfosStageComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InfosStageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
