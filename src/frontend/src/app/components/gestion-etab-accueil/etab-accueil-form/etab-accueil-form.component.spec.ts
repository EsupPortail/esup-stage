import { FormBuilder } from '@angular/forms';

import { EtabAccueilFormComponent } from './etab-accueil-form.component';

describe('EtabAccueilFormComponent', () => {
  let component: EtabAccueilFormComponent;
  let authState: { admin: boolean; gestionnaire: boolean; etudiant: boolean; enseignant: boolean };

  beforeEach(() => {
    authState = { admin: false, gestionnaire: false, etudiant: false, enseignant: false };
    const authService = {
      isAdmin: () => authState.admin,
      isGestionnaire: () => authState.gestionnaire,
      isEtudiant: () => authState.etudiant,
      isEnseignant: () => authState.enseignant,
      checkRights: () => false,
      userConnected: { uid: 'u1' }
    };
    const noopService = {};

    component = new EtabAccueilFormComponent(
      noopService as any,
      noopService as any,
      noopService as any,
      noopService as any,
      noopService as any,
      noopService as any,
      noopService as any,
      noopService as any,
      authService as any,
      new FormBuilder(),
      noopService as any,
      { detectChanges: () => undefined } as any,
      noopService as any,
      noopService as any,
      noopService as any
    );
  });

  it('affiche l action de confidentialite pour un administrateur meme si la fiche SIRENE n est pas editable', () => {
    authState.admin = true;
    component.etab = { id: 1, temSiren: true };
    spyOn(component, 'canEdit').and.returnValue(false);

    expect(component.isConfidentialiteEditable()).toBeTrue();
  });

  it('affiche l action de confidentialite pour un gestionnaire', () => {
    authState.gestionnaire = true;
    component.etab = { id: 1, temSiren: true };

    expect(component.isConfidentialiteEditable()).toBeTrue();
  });

  it('masque l action de confidentialite pour un etudiant', () => {
    authState.admin = true;
    authState.etudiant = true;
    component.etab = { id: 1 };

    expect(component.isConfidentialiteEditable()).toBeFalse();
  });

  it('masque l action de confidentialite pour un enseignant', () => {
    authState.admin = true;
    authState.enseignant = true;
    component.etab = { id: 1 };

    expect(component.isConfidentialiteEditable()).toBeFalse();
  });

  it('masque l action de confidentialite tant que l organisme n existe pas', () => {
    authState.admin = true;
    component.etab = {};

    expect(component.isConfidentialiteEditable()).toBeFalse();
  });
});
