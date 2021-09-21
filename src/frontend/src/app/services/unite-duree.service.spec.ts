import { TestBed } from '@angular/core/testing';

import { UniteDureeService } from './unite-duree.service';

describe('UniteDureeService', () => {
  let service: UniteDureeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UniteDureeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
