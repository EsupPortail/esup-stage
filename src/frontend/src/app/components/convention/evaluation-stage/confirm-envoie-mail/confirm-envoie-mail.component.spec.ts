import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmEnvoieMailComponent } from './confirm-envoie-mail.component';

describe('ConfirmEnvoieMailComponent', () => {
  let component: ConfirmEnvoieMailComponent;
  let fixture: ComponentFixture<ConfirmEnvoieMailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ConfirmEnvoieMailComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConfirmEnvoieMailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
