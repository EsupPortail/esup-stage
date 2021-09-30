import { TestBed } from '@angular/core/testing';

import { UniteGratificationService } from './unite-gratification.service';

describe('UniteGratificationService', () => {
  let service: UniteGratificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UniteGratificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
