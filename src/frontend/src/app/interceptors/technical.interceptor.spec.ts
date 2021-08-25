import { TestBed } from '@angular/core/testing';

import { TechnicalInterceptor } from './technical.interceptor';

describe('TechnicalInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      TechnicalInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: TechnicalInterceptor = TestBed.inject(TechnicalInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
