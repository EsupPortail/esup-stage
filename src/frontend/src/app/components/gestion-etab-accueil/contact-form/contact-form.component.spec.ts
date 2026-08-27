import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';

import { ContactFormComponent } from './contact-form.component';
import { ContactService } from '../../../services/contact.service';
import { AuthService } from '../../../services/auth.service';
import { CentreGestionService } from '../../../services/centre-gestion.service';

describe('ContactFormComponent', () => {
  let fixture: ComponentFixture<ContactFormComponent>;
  let contactService: any;

  const contactExistant = {
    id: 9,
    nom: 'Martin',
    prenom: 'Claire',
    civilite: { id: 1 },
    fonction: 'RH',
    tel: '0102030405',
    fax: '',
    mail: 'c.martin@acme.fr',
    idCentreGestion: 3,
    refusEtreContacte: true
  };

  function creer(contact: any): ContactFormComponent {
    contactService = jasmine.createSpyObj('ContactService', ['create', 'update']);
    contactService.create.and.returnValue(of({}));
    contactService.update.and.returnValue(of({}));

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      declarations: [ContactFormComponent],
      imports: [ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: ContactService, useValue: contactService },
        { provide: MatDialogRef, useValue: { close: () => {} } },
        { provide: AuthService, useValue: { isGestionnaire: () => false, userConnected: { login: 'ges1' } } },
        { provide: CentreGestionService, useValue: { getPaginated: () => of({ data: [] }) } },
        { provide: MAT_DIALOG_DATA, useValue: { contact, service: { id: 7 }, civilites: [], idCentreGestion: 3 } }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    fixture = TestBed.createComponent(ContactFormComponent);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  it('should create', () => {
    expect(creer(null)).toBeTruthy();
  });

  it('expose le droit d\'opposition, décoché par défaut en création', () => {
    const component = creer(null);

    expect(component.form.get('refusEtreContacte')).toBeTruthy();
    expect(component.form.get('refusEtreContacte').value).toBeFalse();
  });

  it('reprend le refus déjà enregistré à l\'édition', () => {
    const component = creer(contactExistant);

    expect(component.form.get('refusEtreContacte').value).toBeTrue();
  });

  it('transmet le refus au backend', () => {
    const component = creer(contactExistant);
    component.form.get('refusEtreContacte').setValue(false);

    component.save();

    expect(contactService.update).toHaveBeenCalled();
    expect(contactService.update.calls.mostRecent().args[1].refusEtreContacte).toBeFalse();
  });
});
