import { TestBed } from '@angular/core/testing';

import { NiveauCentreService } from './niveau-centre.service';

describe('NiveauCentreService', () => {
  let service: NiveauCentreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NiveauCentreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
