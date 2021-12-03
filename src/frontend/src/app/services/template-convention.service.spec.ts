import { TestBed } from '@angular/core/testing';

import { TemplateConventionService } from './template-convention.service';

describe('TemplateConventionService', () => {
  let service: TemplateConventionService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplateConventionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
