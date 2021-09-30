import { TestBed } from '@angular/core/testing';

import { OrigineStageService } from './origine-stage.service';

describe('OrigineStageService', () => {
  let service: OrigineStageService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(OrigineStageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
