import { TestBed } from '@angular/core/testing';

import { StructureService } from './structure.service';

describe('StructureService', () => {
  let service: StructureService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StructureService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
