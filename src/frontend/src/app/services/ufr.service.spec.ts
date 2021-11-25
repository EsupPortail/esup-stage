import { TestBed } from '@angular/core/testing';

import { UfrService } from './ufr.service';

describe('UfrService', () => {
  let service: UfrService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UfrService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
