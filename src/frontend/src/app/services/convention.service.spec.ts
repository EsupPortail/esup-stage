import { TestBed } from '@angular/core/testing';

import { ConventionService } from './convention.service';

describe('ConventionService', () => {
  let service: ConventionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConventionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
