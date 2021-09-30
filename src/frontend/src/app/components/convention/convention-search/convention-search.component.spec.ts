import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConventionSearchComponent } from './convention-search.component';

describe('ConventionSearchComponent', () => {
  let component: ConventionSearchComponent;
  let fixture: ComponentFixture<ConventionSearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConventionSearchComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConventionSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
