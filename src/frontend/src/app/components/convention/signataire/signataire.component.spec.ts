import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { of } from 'rxjs';

import { SignataireComponent } from './signataire.component';
import { ContactService } from '../../../services/contact.service';
import { ServiceService } from '../../../services/service.service';
import { PaysService } from '../../../services/pays.service';
import { CiviliteService } from '../../../services/civilite.service';
import { MessageService } from '../../../services/message.service';
import { AuthService } from '../../../services/auth.service';
import { ConfigService } from '../../../services/config.service';

describe('SignataireComponent', () => {
  let component: SignataireComponent;
  let fixture: ComponentFixture<SignataireComponent>;
  let contactService: any;

  beforeEach(async () => {
    contactService = jasmine.createSpyObj('ContactService', ['getByService', 'create', 'update']);
    contactService.getByService.and.returnValue(of([]));
    contactService.create.and.returnValue(of({}));
    contactService.update.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      declarations: [SignataireComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: ContactService, useValue: contactService },
        { provide: ServiceService, useValue: { getByStructure: () => of([]) } },
        { provide: PaysService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: CiviliteService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: MessageService, useValue: { setSuccess: () => {}, setError: () => {} } },
        { provide: AuthService, useValue: { getCurrentUser: () => of({ login: 'ges1' }), isEtudiant: () => false, isGestionnaire: () => true } },
        { provide: ConfigService, useValue: { getConfigGenerale: () => of({ autoriserEtudiantAModifierEntreprise: true }) } },
        { provide: MatDialog, useValue: { open: () => ({ afterClosed: () => of(null) }) } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(SignataireComponent);
    component = fixture.componentInstance;
    component.convention = { id: 1, structure: { id: 5 }, signataire: null, centreGestion: { id: 3 } };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('expose le droit d\'opposition dans le formulaire', () => {
    expect(component.form.get('refusEtreContacte')).toBeTruthy();
    expect(component.form.get('refusEtreContacte')!.value).toBeFalse();
  });

  it('reprend le refus enregistré à l\'édition du signataire', () => {
    component.contact = { id: 9, nom: 'Directeur', prenom: 'Anne', civilite: { id: 1 }, fonction: 'Directrice', tel: '0102030405', fax: '', mail: 'dir@acme.fr', refusEtreContacte: true };

    component.edit();

    expect(component.form.get('refusEtreContacte')!.value).toBeTrue();
  });

  it('transmet le refus au backend', () => {
    component.contact = { id: 9 };
    component.form.patchValue({
      nom: 'Directeur', prenom: 'Anne', idCivilite: 1, fonction: 'Directrice',
      tel: '0102030405', mail: 'dir@acme.fr', refusEtreContacte: true
    });

    component.save();

    expect(contactService.update).toHaveBeenCalled();
    expect(contactService.update.calls.mostRecent().args[1].refusEtreContacte).toBeTrue();
  });
});
