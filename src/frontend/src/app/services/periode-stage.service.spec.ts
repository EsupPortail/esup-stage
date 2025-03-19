import { TestBed } from '@angular/core/testing';

import { PeriodeStageService } from './periode-stage.service';

describe('PeriodeStageService', () => {
  let service: PeriodeStageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PeriodeStageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
