import { TestBed } from '@angular/core/testing';

import { TypeConventionService } from './type-convention.service';

describe('TypeConventionService', () => {
  let service: TypeConventionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TypeConventionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
