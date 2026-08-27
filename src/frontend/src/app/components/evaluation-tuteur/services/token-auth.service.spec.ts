import { TestBed } from '@angular/core/testing';

import { TokenAuthService } from './token-auth.service';

describe('TokenAuthService', () => {
  let service: TokenAuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TokenAuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
