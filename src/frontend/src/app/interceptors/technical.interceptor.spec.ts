import { TechnicalInterceptor } from './technical.interceptor';

/**
 * Ces tests portent sur le tri fait à la réception d'un 401 : redirection silencieuse quand aucune
 * session n'a jamais été ouverte (démarrage normal de l'application), fenêtre d'avertissement
 * uniquement quand une session existante vient d'être perdue.
 */
describe('TechnicalInterceptor', () => {

  let messageService: any;
  let authService: any;
  let interceptor: TechnicalInterceptor;

  const errorWithReason = (authReason: string | null) => ({
    status: 401,
    headers: { get: (name: string) => (name === 'X-Auth-Reason' ? authReason : null) },
  });

  const buildInterceptor = () => new TechnicalInterceptor(
    null as any,
    messageService,
    null as any,
    { get: () => authService } as any,
  );

  beforeEach(() => {
    messageService = jasmine.createSpyObj('MessageService', ['setWarning', 'setError']);
    authService = jasmine.createSpyObj('AuthService',
      ['hasEstablishedSession', 'markSessionDialogPending', 'redirectToLogin', 'reconnect']);
    authService.hasEstablishedSession.and.returnValue(false);
    interceptor = buildInterceptor();
  });

  it('should be created', () => {
    expect(interceptor).toBeTruthy();
  });

  it('redirige sans fenêtre quand le back indique qu\'aucune session n\'a été ouverte', () => {
    authService.hasEstablishedSession.and.returnValue(true);

    expect(() => interceptor.handleError(errorWithReason('no-session'))).toThrow();

    expect(messageService.setWarning).not.toHaveBeenCalled();
    expect(authService.redirectToLogin).toHaveBeenCalled();
  });

  it('redirige sans fenêtre quand aucune session n\'a été ouverte dans cet onglet', () => {
    // Cas de la navigation privée : le back peut annoncer "idle" faute de savoir, le front sait.
    expect(() => interceptor.handleError(errorWithReason('idle'))).toThrow();

    expect(messageService.setWarning).not.toHaveBeenCalled();
    expect(authService.redirectToLogin).toHaveBeenCalled();
    expect(authService.markSessionDialogPending).not.toHaveBeenCalled();
  });

  it('affiche la fenêtre quand une session ouverte est perdue, et reconnecte à la fermeture', () => {
    authService.hasEstablishedSession.and.returnValue(true);

    expect(() => interceptor.handleError(errorWithReason('idle'))).toThrow();

    expect(authService.markSessionDialogPending).toHaveBeenCalled();
    expect(authService.redirectToLogin).not.toHaveBeenCalled();
    const [message, keep, onClose] = messageService.setWarning.calls.mostRecent().args;
    expect(message).toContain('Votre session a expiré');
    expect(keep).toBeTrue();

    onClose();
    expect(authService.reconnect).toHaveBeenCalled();
  });

  it('n\'ouvre qu\'une seule fenêtre quand plusieurs requêtes échouent en parallèle', () => {
    authService.hasEstablishedSession.and.returnValue(true);

    expect(() => interceptor.handleError(errorWithReason('admin-logout'))).toThrow();
    expect(() => interceptor.handleError(errorWithReason('admin-logout'))).toThrow();

    expect(messageService.setWarning).toHaveBeenCalledTimes(1);
    expect(messageService.setWarning.calls.mostRecent().args[0]).toContain('Votre session a été fermée');
  });
});
