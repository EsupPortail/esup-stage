import { TestBed } from '@angular/core/testing';

import { TempsTravailService } from './temps-travail.service';

describe('TempsTravailService', () => {
  let service: TempsTravailService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TempsTravailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
