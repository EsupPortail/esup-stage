import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { of } from 'rxjs';

import { TuteurProComponent } from './tuteur-pro.component';
import { ContactService } from '../../../services/contact.service';
import { MessageService } from '../../../services/message.service';
import { AuthService } from '../../../services/auth.service';
import { LdapService } from '../../../services/ldap.service';
import { CiviliteService } from '../../../services/civilite.service';
import { ConfigService } from '../../../services/config.service';

describe('TuteurProComponent', () => {
  let component: TuteurProComponent;
  let fixture: ComponentFixture<TuteurProComponent>;
  let contactService: any;

  beforeEach(async () => {
    contactService = jasmine.createSpyObj('ContactService', ['getByService', 'create', 'update']);
    contactService.getByService.and.returnValue(of([]));
    contactService.create.and.returnValue(of({}));
    contactService.update.and.returnValue(of({}));

    await TestBed.configureTestingModule({
      declarations: [TuteurProComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: ContactService, useValue: contactService },
        { provide: MessageService, useValue: { setSuccess: () => {}, setError: () => {} } },
        { provide: AuthService, useValue: { getCurrentUser: () => of({ login: 'ges1' }), isEtudiant: () => false, isGestionnaire: () => true } },
        { provide: LdapService, useValue: { searchUsersByName: () => of([]) } },
        { provide: CiviliteService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: ConfigService, useValue: { getConfigGenerale: () => of({ autoriserEtudiantAModifierEntreprise: true }) } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(TuteurProComponent);
    component = fixture.componentInstance;
    component.service = { id: 7 };
    component.centreGestion = { id: 3 };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('expose le droit d\'opposition dans le formulaire', () => {
    expect(component.form.get('refusEtreContacte')).toBeTruthy();
  });

  it('reprend le refus enregistré à l\'édition du tuteur', () => {
    component.contact = { id: 9, nom: 'Martin', prenom: 'Claire', civilite: { id: 1 }, fonction: 'RH', tel: '0102030405', fax: '', mail: 'c.martin@acme.fr', refusEtreContacte: true };

    component.edit();

    expect(component.form.get('refusEtreContacte')!.value).toBeTrue();
  });

  it('n\'hérite pas du refus quand le tuteur est repris depuis le LDAP', () => {
    component.form.get('refusEtreContacte')!.setValue(true);

    component.chooseStaff({ sn: ['Martin'], givenName: ['Claire'], eduPersonPrimaryAffiliation: 'staff', telephoneNumber: '0102030405', mail: 'c.martin@acme.fr' });

    expect(component.form.get('refusEtreContacte')!.value).toBeFalse();
  });

  it('transmet le refus au backend', () => {
    component.contact = { id: 9 };
    component.form.patchValue({
      nom: 'Martin', prenom: 'Claire', idCivilite: 1, fonction: 'RH',
      tel: '0102030405', mail: 'c.martin@acme.fr', refusEtreContacte: true
    });

    component.save();

    expect(contactService.update).toHaveBeenCalled();
    expect(contactService.update.calls.mostRecent().args[1].refusEtreContacte).toBeTrue();
  });
});
