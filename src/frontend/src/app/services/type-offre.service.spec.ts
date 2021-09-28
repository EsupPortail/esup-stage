import { TestBed } from '@angular/core/testing';

import { TypeOffreService } from './type-offre.service';

describe('TypeOffreService', () => {
  let service: TypeOffreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TypeOffreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
