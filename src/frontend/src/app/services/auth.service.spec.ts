import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { TokenService } from './token.service';
import { environment } from '../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let replaceLocation: jasmine.Spy;

  const connectedUrl = environment.apiUrl + '/users/connected';

  beforeEach(() => {
    sessionStorage.removeItem('sessionEstablished');
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {provide: TokenService, useValue: {getToken: () => '', logout: () => {}}},
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    // La redirection réelle sortirait le lanceur de tests de la page.
    replaceLocation = spyOn<any>(service, 'replaceLocation');
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.removeItem('sessionEstablished');
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('ne considère aucune session comme établie au démarrage', () => {
    expect(service.hasEstablishedSession()).toBeFalse();
  });

  it('mémorise la session dès que l\'utilisateur connecté est récupéré', () => {
    service.getCurrentUser().subscribe();
    httpMock.expectOne(connectedUrl).flush({login: 'jdoe'});

    expect(service.hasEstablishedSession()).toBeTrue();
    expect(sessionStorage.getItem('sessionEstablished')).toBe('1');
  });

  it('oublie la session en quittant l\'application vers le CAS', () => {
    service.getCurrentUser().subscribe();
    httpMock.expectOne(connectedUrl).flush({login: 'jdoe'});

    service.redirectToLogin();

    expect(replaceLocation).toHaveBeenCalled();
    expect(replaceLocation.calls.mostRecent().args[0]).not.toContain('renew=1');
    expect(service.hasEstablishedSession()).toBeFalse();
  });

  it('demande au CAS une ressaisie des identifiants lors d\'une reconnexion explicite', () => {
    service.reconnect();

    expect(replaceLocation.calls.mostRecent().args[0]).toContain('renew=1');
  });

  it('ne redirige pas sur une erreur technique, qui n\'est pas une perte de session', () => {
    service.getCurrentUser().subscribe();
    httpMock.expectOne(connectedUrl).flush('boom', {status: 500, statusText: 'Server Error'});

    expect(replaceLocation).not.toHaveBeenCalled();
  });

  it('redirige sur un 401', () => {
    service.getCurrentUser().subscribe();
    httpMock.expectOne(connectedUrl).flush('', {status: 401, statusText: 'Unauthorized'});

    expect(replaceLocation).toHaveBeenCalled();
  });

  describe('routes publiques anonymes', () => {
    let initialHash: string;

    beforeEach(() => {
      initialHash = window.location.hash;
    });

    afterEach(() => {
      window.location.hash = initialHash;
    });

    it('reconnaît la page d\'évaluation tuteur', () => {
      window.location.hash = '#/evaluation-tuteur/un-token';
      expect(service.isAnonymousPublicRoute()).toBeTrue();
    });

    it('ne prend pas une route interne pour une route publique', () => {
      window.location.hash = '#/conventions/create';
      expect(service.isAnonymousPublicRoute()).toBeFalse();
    });

    it('n\'expédie pas au CAS un visiteur sans session sur une page publique', () => {
      window.location.hash = '#/evaluation-tuteur/un-token';

      service.redirectToLogin();

      expect(replaceLocation).not.toHaveBeenCalled();
    });

    it('laisse se reconnecter un utilisateur dont la session meurt sur une page publique', () => {
      service.getCurrentUser().subscribe();
      httpMock.expectOne(connectedUrl).flush({login: 'jdoe'});
      window.location.hash = '#/evaluation-tuteur/un-token';

      service.redirectToLogin();

      expect(replaceLocation).toHaveBeenCalled();
    });
  });
});
