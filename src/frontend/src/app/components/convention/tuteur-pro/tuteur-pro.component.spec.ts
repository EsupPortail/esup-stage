import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TuteurProComponent } from './tuteur-pro.component';

describe('TuteurProComponent', () => {
  let component: TuteurProComponent;
  let fixture: ComponentFixture<TuteurProComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TuteurProComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TuteurProComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
