import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SignatureElectroniqueViewComponent } from './signature-electronique-view.component';

describe('SignatureElectroniqueViewComponent', () => {
  let component: SignatureElectroniqueViewComponent;
  let fixture: ComponentFixture<SignatureElectroniqueViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SignatureElectroniqueViewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SignatureElectroniqueViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
