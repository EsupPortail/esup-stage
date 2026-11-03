import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { AdminRoleComponent } from './admin-role.component';
import { RoleService } from '../../../services/role.service';
import { AuthService } from '../../../services/auth.service';
import { MessageService } from '../../../services/message.service';
import { AppFonction } from '../../../constants/app-fonction';

@Pipe({ name: 'contenu', standalone: false })
class ContenuPipeStub implements PipeTransform {
  transform(value: any): any {
    return value;
  }
}

describe('AdminRoleComponent', () => {
  let component: AdminRoleComponent;
  let fixture: ComponentFixture<AdminRoleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AdminRoleComponent, ContenuPipeStub],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: RoleService, useValue: { findAllAppFonction: () => of([]) } },
        { provide: AuthService, useValue: { checkRights: () => true } },
        { provide: MessageService, useValue: { setSuccess: () => {} } },
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminRoleComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('affiche le droit Validation pour CONVENTION et AVENANT uniquement', () => {
    expect(component.showValidationRight(AppFonction.CONVENTION)).toBeTrue();
    expect(component.showValidationRight(AppFonction.AVENANT)).toBeTrue();
    expect(component.showValidationRight(AppFonction.ORGA_ACC)).toBeFalse();
    expect(component.showValidationRight(AppFonction.SERVICE_CONTACT_ACC)).toBeFalse();
    expect(component.showValidationRight(AppFonction.PARAM_GLOBAL)).toBeFalse();
  });
});
