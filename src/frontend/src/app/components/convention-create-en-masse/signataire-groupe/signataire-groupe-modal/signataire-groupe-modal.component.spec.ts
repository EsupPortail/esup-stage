import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignataireGroupeModalComponent } from './signataire-groupe-modal.component';

describe('SignataireGroupeModalComponent', () => {
  let component: SignataireGroupeModalComponent;
  let fixture: ComponentFixture<SignataireGroupeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SignataireGroupeModalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SignataireGroupeModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
