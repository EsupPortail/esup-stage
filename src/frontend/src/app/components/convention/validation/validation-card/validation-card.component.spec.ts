import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { ValidationCardComponent } from './validation-card.component';
import { AuthService } from '../../../../services/auth.service';
import { ConventionService } from '../../../../services/convention.service';
import { MessageService } from '../../../../services/message.service';

describe('ValidationCardComponent', () => {
  let component: ValidationCardComponent;
  let fixture: ComponentFixture<ValidationCardComponent>;
  let authState: { enseignant: boolean; gestionnaire: boolean; admin: boolean };

  beforeEach(async () => {
    authState = { enseignant: false, gestionnaire: false, admin: false };

    await TestBed.configureTestingModule({
      declarations: [ValidationCardComponent],
      providers: [
        {
          provide: AuthService,
          useValue: {
            isEnseignant: () => authState.enseignant,
            isGestionnaire: () => authState.gestionnaire,
            isAdmin: () => authState.admin,
          },
        },
        {
          provide: ConventionService,
          useValue: {
            validate: () => of({}),
            unvalidate: () => of({}),
          },
        },
        { provide: MessageService, useValue: { setSuccess: () => undefined } },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ValidationCardComponent);
    component = fixture.componentInstance;
    component.convention = {
      validationConvention: false,
      centreGestion: {
        validationConventionOrdre: 1,
        validationPedagogiqueOrdre: 2,
      },
    };
    component.validation = 'validationConvention';
    component.validationsActives = ['validationConvention', 'validationPedagogique'];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('refuse la validation administrative pour un enseignant seul', () => {
    authState.enseignant = true;
    authState.gestionnaire = false;
    expect(component.canValidate()).toBeFalse();
    expect(component.canRevertValidation()).toBeFalse();
  });

  it('autorise la validation administrative pour un cumul gestionnaire + enseignant', () => {
    authState.enseignant = true;
    authState.gestionnaire = true;
    expect(component.canValidate()).toBeTrue();
  });

  it('autorise la validation administrative pour un gestionnaire seul', () => {
    authState.enseignant = false;
    authState.gestionnaire = true;
    expect(component.canValidate()).toBeTrue();
  });
});
