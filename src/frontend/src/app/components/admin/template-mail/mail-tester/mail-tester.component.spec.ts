import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MailTesterComponent } from './mail-tester.component';

describe('MailTesterComponent', () => {
  let component: MailTesterComponent;
  let fixture: ComponentFixture<MailTesterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MailTesterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MailTesterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
