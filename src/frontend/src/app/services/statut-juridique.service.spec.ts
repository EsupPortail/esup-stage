import { TestBed } from '@angular/core/testing';

import { StatutJuridiqueService } from './statut-juridique.service';

describe('StatutJuridiqueService', () => {
  let service: StatutJuridiqueService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StatutJuridiqueService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
