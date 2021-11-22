import { TestBed } from '@angular/core/testing';

import { PersonnelCentreService } from './personnel-centre.service';

describe('PersonnelCentreService', () => {
  let service: PersonnelCentreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PersonnelCentreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
