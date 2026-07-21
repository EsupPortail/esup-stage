import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { SessionKeepAliveService } from './session-keep-alive.service';
import { AuthService } from './auth.service';
import { MessageService } from './message.service';

describe('SessionKeepAliveService', () => {
  let service: SessionKeepAliveService;
  let authService: jasmine.SpyObj<AuthService>;
  let messageService: jasmine.SpyObj<MessageService>;

  const INTERVAL = 1000;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['pingSession', 'isCasRedirectInProgress']);
    authService.pingSession.and.returnValue(of({}));
    authService.isCasRedirectInProgress.and.returnValue(false);
    messageService = jasmine.createSpyObj('MessageService', ['setWarning']);

    TestBed.configureTestingModule({
      providers: [
        SessionKeepAliveService,
        { provide: AuthService, useValue: authService },
        { provide: MessageService, useValue: messageService },
      ],
    });
    service = TestBed.inject(SessionKeepAliveService);
  });

  afterEach(() => {
    service.stop();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('pings immediately on start (leading edge)', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL });
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    service.stop();
  }));

  it('pings on each interval while the user is active', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    authService.pingSession.calls.reset(); // on ignore le ping de démarrage
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    window.dispatchEvent(new KeyboardEvent('keydown'));
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(2);
    service.stop();
  }));

  it('stops pinging once activity is older than the window (abandoned tab)', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: INTERVAL });
    authService.pingSession.calls.reset();
    tick(INTERVAL); // activité (démarrage) encore dans la fenêtre -> 1 ping
    tick(INTERVAL); // plus d'activité depuis > fenêtre -> aucun ping
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    service.stop();
  }));

  it('emits no ping after stop() (no leak)', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    authService.pingSession.calls.reset();
    service.stop();
    tick(3 * INTERVAL);
    expect(authService.pingSession).not.toHaveBeenCalled();
  }));

  it('is idempotent: a second start() does not create a second interval', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    // 1 ping de démarrage + 1 ping au tick = 2 (et pas 3, ce qui trahirait 2 timers)
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(2);
    service.stop();
  }));

  it('warns without clearing and stops on a 401 ping', fakeAsync(() => {
    authService.pingSession.and.returnValue(throwError(() => ({ status: 401 })));
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    // le ping de démarrage suffit à déclencher la détection
    expect(messageService.setWarning).toHaveBeenCalledTimes(1);
    expect(service.isRunning()).toBeFalse();
  }));

  it('does not warn when a CAS redirect is already in progress', fakeAsync(() => {
    authService.pingSession.and.returnValue(throwError(() => ({ status: 401 })));
    authService.isCasRedirectInProgress.and.returnValue(true);
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    expect(messageService.setWarning).not.toHaveBeenCalled();
    service.stop();
  }));

  it('ignores transient network errors and keeps running', fakeAsync(() => {
    let call = 0;
    authService.pingSession.and.callFake(() =>
      call++ === 0 ? throwError(() => ({ status: 0 })) : of({}),
    );
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    // ping de démarrage -> erreur réseau transitoire : pas de warning, on continue
    tick(INTERVAL); // ping suivant OK
    expect(messageService.setWarning).not.toHaveBeenCalled();
    expect(service.isRunning()).toBeTrue();
    service.stop();
  }));
});
