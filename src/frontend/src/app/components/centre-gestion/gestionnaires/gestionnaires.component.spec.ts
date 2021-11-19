import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionnairesComponent } from './gestionnaires.component';

describe('GestionnairesComponent', () => {
  let component: GestionnairesComponent;
  let fixture: ComponentFixture<GestionnairesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GestionnairesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GestionnairesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
