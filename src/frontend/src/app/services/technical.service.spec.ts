import { TestBed } from '@angular/core/testing';

import { TechnicalService } from './technical.service';

describe('TechnicalService', () => {
  let service: TechnicalService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TechnicalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
