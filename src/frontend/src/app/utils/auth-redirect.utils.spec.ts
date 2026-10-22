import {
  buildCasLoginPath,
  buildRedirectTarget,
  resolveCasRedirect,
  sanitizeRedirectTarget,
} from './auth-redirect.utils';

describe('auth redirect utils', () => {
  const origin = 'https://esup.univ.fr';
  const conventionTarget = '/esup-stage/frontend/#/conventions/999';

  describe('buildCasLoginPath', () => {
    it('conserve le contexte de déploiement', () => {
      expect(buildCasLoginPath('/esup-stage/frontend/')).toBe(
        '/esup-stage/login/cas',
      );
    });

    it('ignore les slashs doublés devant frontend', () => {
      expect(buildCasLoginPath('/esup-stage//frontend/')).toBe(
        '/esup-stage/login/cas',
      );
    });

    it('reste à la racine sans contexte', () => {
      expect(buildCasLoginPath('/frontend/')).toBe('/login/cas');
      expect(buildCasLoginPath('/')).toBe('/login/cas');
    });

    it('déduit le contexte hors frontend', () => {
      expect(buildCasLoginPath('/esup-stage/')).toBe('/esup-stage/login/cas');
    });
  });

  describe('buildRedirectTarget', () => {
    it('mémorise pathname, search et hash', () => {
      expect(
        buildRedirectTarget('/esup-stage/frontend/', '', '#/conventions/999'),
      ).toBe(conventionTarget);
      expect(
        buildRedirectTarget(
          '/esup-stage/frontend/',
          '?onglet=2',
          '#/conventions/999',
        ),
      ).toBe('/esup-stage/frontend/?onglet=2#/conventions/999');
    });
  });

  describe('sanitizeRedirectTarget', () => {
    it('accepte un chemin de la même origine', () => {
      expect(sanitizeRedirectTarget(conventionTarget, origin)).toBe(
        conventionTarget,
      );
    });

    it('refuse une URL absolue', () => {
      expect(
        sanitizeRedirectTarget(
          'https://evil.example/frontend/#/conventions/999',
          origin,
        ),
      ).toBeNull();
    });

    it('refuse un chemin protocol-relative', () => {
      expect(
        sanitizeRedirectTarget('//evil.example/frontend/', origin),
      ).toBeNull();
    });

    it('refuse un schéma non http', () => {
      expect(sanitizeRedirectTarget('javascript:alert(1)', origin)).toBeNull();
    });

    it('refuse une valeur absente', () => {
      expect(sanitizeRedirectTarget(null, origin)).toBeNull();
      expect(sanitizeRedirectTarget('', origin)).toBeNull();
    });
  });

  describe('resolveCasRedirect', () => {
    it("ne restaure rien quand aucun départ vers CAS n'a été initié", () => {
      expect(
        resolveCasRedirect(
          conventionTarget,
          false,
          origin,
          '/esup-stage/frontend/',
        ),
      ).toBeNull();
    });

    it('restaure la cible mémorisée au retour du CAS', () => {
      expect(
        resolveCasRedirect(
          conventionTarget,
          true,
          origin,
          '/esup-stage/frontend/',
        ),
      ).toBe(conventionTarget);
    });

    it('ne recharge pas une fiche déjà ouverte', () => {
      expect(
        resolveCasRedirect(conventionTarget, true, origin, conventionTarget),
      ).toBeNull();
    });

    it("refuse une cible d'une autre origine", () => {
      expect(
        resolveCasRedirect(
          '//evil.example/frontend/',
          true,
          origin,
          '/esup-stage/frontend/',
        ),
      ).toBeNull();
    });
  });
});
