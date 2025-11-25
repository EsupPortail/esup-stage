import { TestBed } from '@angular/core/testing';

import { EvaluationTuteurContextService } from './evaluation-tuteur-context.service';

describe('EvaluationTuteurContextService', () => {
  let service: EvaluationTuteurContextService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EvaluationTuteurContextService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
