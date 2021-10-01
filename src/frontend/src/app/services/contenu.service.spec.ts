import { TestBed } from '@angular/core/testing';

import { ContenuService } from './contenu.service';

describe('ContenuService', () => {
  let service: ContenuService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ContenuService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
