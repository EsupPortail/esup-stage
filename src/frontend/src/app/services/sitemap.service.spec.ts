import { TestBed } from '@angular/core/testing';

import { SitemapService } from './sitemap.service';

describe('SitemapService', () => {
  let service: SitemapService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SitemapService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
