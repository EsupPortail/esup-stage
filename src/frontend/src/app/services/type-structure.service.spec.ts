import { TestBed } from '@angular/core/testing';

import { TypeStructureService } from './type-structure.service';

describe('TypeStructureService', () => {
  let service: TypeStructureService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TypeStructureService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
