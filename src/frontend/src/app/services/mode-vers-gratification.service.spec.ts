import { TestBed } from '@angular/core/testing';

import { ModeVersGratificationService } from './mode-vers-gratification.service';

describe('ModeVersGratificationService', () => {
  let service: ModeVersGratificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ModeVersGratificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
