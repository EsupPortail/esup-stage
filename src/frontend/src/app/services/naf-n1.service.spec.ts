import { TestBed } from '@angular/core/testing';

import { NafN1Service } from './naf-n1.service';

describe('NafN1Service', () => {
  let service: NafN1Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NafN1Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
