import { TestBed } from '@angular/core/testing';

import { DeviseService } from './devise.service';

describe('DeviseService', () => {
  let service: DeviseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeviseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
