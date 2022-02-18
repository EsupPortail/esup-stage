import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RetourListeComponent } from './retour-liste.component';

describe('RetourListeComponent', () => {
  let component: RetourListeComponent;
  let fixture: ComponentFixture<RetourListeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RetourListeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RetourListeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
