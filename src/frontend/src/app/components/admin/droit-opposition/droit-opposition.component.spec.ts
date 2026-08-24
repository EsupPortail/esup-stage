import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { DroitOppositionComponent } from './droit-opposition.component';
import { ContactService } from '../../../services/contact.service';
import { MessageService } from '../../../services/message.service';

describe('DroitOppositionComponent', () => {
  let component: DroitOppositionComponent;
  let fixture: ComponentFixture<DroitOppositionComponent>;
  let contactService: jasmine.SpyObj<ContactService>;
  let messageService: jasmine.SpyObj<MessageService>;

  beforeEach(async () => {
    contactService = jasmine.createSpyObj('ContactService', ['enregistrerRefusEtreContacte']);
    messageService = jasmine.createSpyObj('MessageService', ['setSuccess', 'setError']);

    await TestBed.configureTestingModule({
      declarations: [DroitOppositionComponent],
      imports: [FormsModule],
      providers: [
        { provide: ContactService, useValue: contactService },
        { provide: MessageService, useValue: messageService }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DroitOppositionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('découpe le collage sur les retours à la ligne, points-virgules et virgules', () => {
    component.saisie = 'un@acme.fr\ndeux@acme.fr; trois@acme.fr,quatre@acme.fr';

    expect(component.extraireAdresses())
      .toEqual(['un@acme.fr', 'deux@acme.fr', 'trois@acme.fr', 'quatre@acme.fr']);
  });

  it('ignore les lignes vides et les espaces superflus', () => {
    component.saisie = '  un@acme.fr  \n\n\n  \ndeux@acme.fr\n';

    expect(component.extraireAdresses()).toEqual(['un@acme.fr', 'deux@acme.fr']);
  });

  it('refuse une saisie vide sans appeler le service', () => {
    component.saisie = '   ';

    component.enregistrer();

    expect(contactService.enregistrerRefusEtreContacte).not.toHaveBeenCalled();
    expect(messageService.setError).toHaveBeenCalled();
  });

  it('transmet les adresses et expose le compte-rendu', () => {
    const reponse = {
      traitees: [{ mail: 'un@acme.fr', nbContacts: 2 }],
      inconnues: ['deux@acme.fr'],
      invalides: []
    };
    contactService.enregistrerRefusEtreContacte.and.returnValue(of(reponse));
    component.saisie = 'un@acme.fr\ndeux@acme.fr';

    component.enregistrer();

    expect(contactService.enregistrerRefusEtreContacte)
      .toHaveBeenCalledWith(['un@acme.fr', 'deux@acme.fr']);
    expect(component.resultat).toBe(reponse);
    expect(component.enCours).toBeFalse();
    // le message récapitule le nombre de fiches contact impactées, pas le nombre d'adresses
    expect(messageService.setSuccess).toHaveBeenCalledWith('2 contact(s) mis à jour');
  });

  it('débloque le formulaire quand l\'appel échoue', () => {
    contactService.enregistrerRefusEtreContacte.and.returnValue(throwError(() => new Error('boom')));
    component.saisie = 'un@acme.fr';

    component.enregistrer();

    expect(component.enCours).toBeFalse();
    expect(component.resultat).toBeNull();
  });

  it('réinitialise la saisie et le compte-rendu', () => {
    component.saisie = 'un@acme.fr';
    component.resultat = { traitees: [], inconnues: [], invalides: [] };

    component.reinitialiser();

    expect(component.saisie).toBe('');
    expect(component.resultat).toBeNull();
  });
});
