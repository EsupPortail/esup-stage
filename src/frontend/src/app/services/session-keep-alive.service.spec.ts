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

  it('pings while the user is active', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    service.stop();
  }));

  it('stops pinging once activity is older than the window (abandoned tab)', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: INTERVAL });
    tick(INTERVAL); // activité récente (entrée) -> 1 ping
    tick(INTERVAL); // plus aucune activité depuis > fenêtre -> aucun ping
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    service.stop();
  }));

  it('keeps pinging as long as the user keeps interacting', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: INTERVAL });
    tick(INTERVAL);
    window.dispatchEvent(new KeyboardEvent('keydown'));
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(2);
    service.stop();
  }));

  it('is idempotent: a second start() does not create a second interval', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    tick(INTERVAL);
    expect(authService.pingSession).toHaveBeenCalledTimes(1);
    service.stop();
  }));

  it('emits no ping after stop() (no leak)', fakeAsync(() => {
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    service.stop();
    tick(3 * INTERVAL);
    expect(authService.pingSession).not.toHaveBeenCalled();
  }));

  it('warns without clearing and stops on a 401 ping', fakeAsync(() => {
    authService.pingSession.and.returnValue(throwError(() => ({ status: 401 })));
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    tick(INTERVAL);
    expect(messageService.setWarning).toHaveBeenCalledTimes(1);
    expect(service.isRunning()).toBeFalse();
  }));

  it('does not warn when a CAS redirect is already in progress', fakeAsync(() => {
    authService.pingSession.and.returnValue(throwError(() => ({ status: 401 })));
    authService.isCasRedirectInProgress.and.returnValue(true);
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    tick(INTERVAL);
    expect(messageService.setWarning).not.toHaveBeenCalled();
    service.stop();
  }));

  it('ignores transient network errors and retries on the next tick', fakeAsync(() => {
    authService.pingSession.and.returnValues(
      throwError(() => ({ status: 0 })),
      of({}),
    );
    service.start({ pingIntervalMs: INTERVAL, activityWindowMs: 10 * INTERVAL });
    tick(INTERVAL); // erreur réseau transitoire -> pas de warning, on continue
    tick(INTERVAL); // ping suivant OK
    expect(messageService.setWarning).not.toHaveBeenCalled();
    expect(authService.pingSession).toHaveBeenCalledTimes(2);
    expect(service.isRunning()).toBeTrue();
    service.stop();
  }));
});
