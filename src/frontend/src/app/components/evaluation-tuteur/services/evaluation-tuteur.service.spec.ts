import { TestBed } from '@angular/core/testing';

import { EvaluationTuteurService } from './evaluation-tuteur.service';

describe('EvaluationTuteurService', () => {
  let service: EvaluationTuteurService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EvaluationTuteurService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
