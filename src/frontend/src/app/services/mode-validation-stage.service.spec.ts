import { TestBed } from '@angular/core/testing';

import { ModeValidationStageService } from './mode-validation-stage.service';

describe('ModeValidationStageService', () => {
  let service: ModeValidationStageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ModeValidationStageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
