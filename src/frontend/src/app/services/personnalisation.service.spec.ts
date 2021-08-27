import { TestBed } from '@angular/core/testing';

import { PersonnalisationService } from './personnalisation.service';

describe('PersonnalisationService', () => {
  let service: PersonnalisationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PersonnalisationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
