import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TachePlanifieComponent } from './tache-planifie.component';

describe('ConfigComponent', () => {
  let component: TachePlanifieComponent;
  let fixture: ComponentFixture<TachePlanifieComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TachePlanifieComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TachePlanifieComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
