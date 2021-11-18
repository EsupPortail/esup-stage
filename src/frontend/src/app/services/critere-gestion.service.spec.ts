import { TestBed } from '@angular/core/testing';

import { CritereGestionService } from './critere-gestion.service';

describe('CritereGestionService', () => {
  let service: CritereGestionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CritereGestionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
