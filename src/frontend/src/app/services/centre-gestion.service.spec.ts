import { TestBed } from '@angular/core/testing';

import { CentreGestionService } from './centre-gestion.service';

describe('CentreGestionService', () => {
  let service: CentreGestionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CentreGestionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
