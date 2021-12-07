import { TestBed } from '@angular/core/testing';

import { ParamConventionService } from './param-convention.service';

describe('ParamConventionService', () => {
  let service: ParamConventionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ParamConventionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
