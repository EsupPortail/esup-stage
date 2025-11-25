import { TestBed } from '@angular/core/testing';

import { QuestionsEvaluationService } from './questions-evaluation.service';

describe('QuestionsEvaluationService', () => {
  let service: QuestionsEvaluationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(QuestionsEvaluationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
