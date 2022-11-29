import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CentreSignatureElectroniqueComponent } from './signature-electronique.component';

describe('CentreSignatureElectroniqueComponent', () => {
  let component: CentreSignatureElectroniqueComponent;
  let fixture: ComponentFixture<CentreSignatureElectroniqueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CentreSignatureElectroniqueComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CentreSignatureElectroniqueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
