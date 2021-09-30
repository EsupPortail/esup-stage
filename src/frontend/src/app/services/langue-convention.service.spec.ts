import { TestBed } from '@angular/core/testing';

import { LangueConventionService } from './langue-convention.service';

describe('LangueConventionService', () => {
  let service: LangueConventionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LangueConventionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
