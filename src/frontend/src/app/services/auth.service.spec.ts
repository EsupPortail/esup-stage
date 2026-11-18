import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    sessionStorage.clear();
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it("ignore une ancienne redirectUrl quand aucun départ vers CAS n'a été initié", () => {
    sessionStorage.setItem(
      'redirectUrl',
      '/esup-stage/frontend/#/conventions/999',
    );
    const currentHref = window.location.href;

    service.createUser({ login: 'test' });

    expect(window.location.href).toBe(currentHref);
    expect(sessionStorage.getItem('casRedirectAttempted')).toBeNull();
    expect(sessionStorage.getItem('redirectUrl')).toBeNull();
  });

  it('efface le marqueur et la cible après une restauration sans navigation', () => {
    const currentTarget = `${window.location.pathname}${window.location.search}${window.location.hash}`;
    sessionStorage.setItem('casRedirectAttempted', '1');
    sessionStorage.setItem('redirectUrl', currentTarget);
    const currentHref = window.location.href;

    service.createUser({ login: 'test' });

    expect(window.location.href).toBe(currentHref);
    expect(sessionStorage.getItem('casRedirectAttempted')).toBeNull();
    expect(sessionStorage.getItem('redirectUrl')).toBeNull();
  });
});
