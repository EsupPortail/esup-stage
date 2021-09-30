import { TestBed } from '@angular/core/testing';

import { NiveauFormationService } from './niveau-formation.service';

describe('NiveauFormationService', () => {
  let service: NiveauFormationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NiveauFormationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
