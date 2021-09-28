import { TestBed } from '@angular/core/testing';

import { ContratOffreService } from './contrat-offre.service';

describe('ContratOffreService', () => {
  let service: ContratOffreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ContratOffreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
