import { TestBed } from '@angular/core/testing';

import { EffectifService } from './effectif.service';

describe('EffectifService', () => {
  let service: EffectifService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EffectifService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
