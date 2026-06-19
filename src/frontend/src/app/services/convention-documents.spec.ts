import { TestBed } from '@angular/core/testing';

import { ConventionServiceService } from './convention-service.service';

describe('ConventionServiceService', () => {
  let service: ConventionServiceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConventionServiceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
