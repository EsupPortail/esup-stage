import { TestBed } from '@angular/core/testing';

import { ConsigneService } from './consigne.service';

describe('ConsigneService', () => {
  let service: ConsigneService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConsigneService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
