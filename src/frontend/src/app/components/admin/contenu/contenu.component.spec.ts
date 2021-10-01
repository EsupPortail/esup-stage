import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContenuComponent } from './contenu.component';

describe('ContenuComponent', () => {
  let component: ContenuComponent;
  let fixture: ComponentFixture<ContenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
