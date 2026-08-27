import { TestBed } from '@angular/core/testing';
import { Route, Router } from '@angular/router';

import { AuthService } from './auth.service';
import { SitemapService } from './sitemap.service';

describe('SitemapService', () => {
  let service: SitemapService;
  let router: { config: Route[] };
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    router = { config: [] };
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['canAccess']);
    authService.canAccess.and.returnValue(true);

    TestBed.configureTestingModule({
      providers: [
        SitemapService,
        { provide: Router, useValue: router },
        { provide: AuthService, useValue: authService }
      ]
    });
    service = TestBed.inject(SitemapService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should replace terminal id route parameters with create in sitemap urls', () => {
    router.config = [
      {
        path: 'centre-gestion/:id',
        data: { sitemap: { label: 'Ajouter un centre de gestion', group: 'Centres de gestion' } }
      },
      {
        path: 'conventions/:id/details',
        data: { sitemap: { label: 'Détail convention', group: 'Conventions de stages' } }
      }
    ];

    const urls = service.getGroups().flatMap(group => group.items.map(item => item.url));

    expect(urls).toContain('/centre-gestion/create');
    expect(urls).toContain('/conventions/:id/details');
    expect(urls).not.toContain('/centre-gestion/:id');
  });
});
