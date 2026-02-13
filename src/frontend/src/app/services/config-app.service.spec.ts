import { TestBed } from '@angular/core/testing';

import { ConfigAppService } from './config-app.service';

describe('ConfigAppService', () => {
  let service: ConfigAppService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ConfigAppService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
