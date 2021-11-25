import { TestBed } from '@angular/core/testing';

import { EtapeService } from './etape.service';

describe('EtapeService', () => {
  let service: EtapeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EtapeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
