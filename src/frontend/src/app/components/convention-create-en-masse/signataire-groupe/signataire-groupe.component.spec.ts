import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignataireGroupeComponent } from './signataire-groupe.component';

describe('SignataireGroupeComponent', () => {
  let component: SignataireGroupeComponent;
  let fixture: ComponentFixture<SignataireGroupeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SignataireGroupeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SignataireGroupeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
