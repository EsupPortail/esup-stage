import { TestBed } from '@angular/core/testing';

import { TemplateMailService } from './template-mail.service';

describe('TemplateMailService', () => {
  let service: TemplateMailService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TemplateMailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
