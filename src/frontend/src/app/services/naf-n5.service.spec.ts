import { TestBed } from '@angular/core/testing';

import { NafN5Service } from './naf-n5.service';

describe('NafN5Service', () => {
  let service: NafN5Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NafN5Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
