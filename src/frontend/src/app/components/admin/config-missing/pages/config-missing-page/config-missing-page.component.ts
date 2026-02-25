import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ConfigMissingService } from "../../../../../services/config-missing.service";

/** État d'un test de connexion */
interface TestState {
  loading: boolean;
  result: 'success' | 'error' | null;
  message: string;
}

function emptyTest(): TestState {
  return { loading: false, result: null, message: '' };
}

@Component({
  selector: 'app-config-missing-page',
  templateUrl: './config-missing-page.component.html',
  styleUrl: './config-missing-page.component.scss',
  standalone: false
})
export class ConfigMissingPageComponent implements OnInit {

  missingKeys: string[] = [];
  configForm!: FormGroup;
  submitted = false;
  savedSuccess = false;

  // États des tests
  testMailer:      TestState = emptyTest();
  testReferentiel: TestState = emptyTest();
  testWebhook:     TestState = emptyTest();
  testSirene:      TestState = emptyTest();

  // Visibilité mots de passe
  showJwt          = false;
  showSmtpPwd      = false;
  showRefPwd       = false;
  showWebhookToken = false;
  showSireneToken  = false;

  private readonly REQUIRED_KEYS = [
    'referentiel.ws.login',
    'referentiel.ws.password',
    'referentiel.ws.ldap_url',
    'referentiel.ws.apogee_url',
  ];

  constructor(
    private configMissingService: ConfigMissingService,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.buildForm();

    this.configMissingService.getMissing().subscribe({
      next: (resp: { missing: string[] }) => {
        this.missingKeys = resp?.missing || [];
      },
      error: () => {
        this.missingKeys = [];
      }
    });
  }

  private buildForm(): void {
    this.configForm = this.fb.group({
      // Application
      appli_url: [null],
      appli_tokens: [null],
      appli_jwt_secret: [null],
      appli_nb_jours_valide_token: [null],

      // Mailer
      appli_mailer_protocol: ['smtp'],
      appli_mailer_host: [null],
      appli_mailer_port: [null],
      appli_mailer_auth: [''],
      appli_mailer_username: ['username@domaine.com'],
      appli_mailer_password: ['xxx'],
      appli_mailer_from: [null],
      appli_mailer_disable_delivery: [false],
      appli_mailer_delivery_address: [null],

      // Référentiel WS (obligatoires)
      referentiel_ws_login: [null, Validators.required],
      referentiel_ws_password: [null, Validators.required],
      referentiel_ws_ldap_url: [null, Validators.required],
      referentiel_ws_apogee_url: [null, Validators.required],

      // Webhook
      webhook_signature_uri: [null],
      webhook_signature_token: [null],

      // SIRENE
      sirene_url: [null],
      sirene_token: [null],
      sirene_nombre_minimum_resultats: [null],

      // Footer
      appli_footer_github: [null],
      appli_footer_site: [null],
      appli_footer_support: [null],
      appli_footer_wiki: [null],
    });
  }

  // ── Helpers ──────────────────────────────────────────────────

  isInvalid(field: string): boolean {
    const ctrl = this.configForm.get(field);
    return !!ctrl && ctrl.invalid && (ctrl.touched || this.submitted);
  }

  isMissing(key: string): boolean {
    return this.missingKeys.includes(key);
  }

  // ── Conditions d'activation des boutons de test ───────────────

  isMailerTestable(): boolean {
    const v = this.configForm.value;
    return !!(v.appli_mailer_host && v.appli_mailer_port);
  }

  isReferentielTestable(): boolean {
    const v = this.configForm.value;
    return !!(
      v.referentiel_ws_login &&
      v.referentiel_ws_password &&
      v.referentiel_ws_ldap_url &&
      v.referentiel_ws_apogee_url
    );
  }

  isWebhookTestable(): boolean {
    const v = this.configForm.value;
    return !!(v.webhook_signature_uri && v.webhook_signature_token);
  }

  isSireneTestable(): boolean {
    const v = this.configForm.value;
    return !!(v.sirene_url && v.sirene_token);
  }

  // ── Fonctions de test ─────────────────────────────────────────

  testMailerConnection(): void {
    const v = this.configForm.value;
    const params = {
      protocol: v.appli_mailer_protocol,
      host:     v.appli_mailer_host,
      port:     v.appli_mailer_port,
      auth:     v.appli_mailer_auth,
      username: v.appli_mailer_username,
      password: v.appli_mailer_password,
    };

    this.testMailer = { loading: true, result: null, message: '' };
    console.log('Test SMTP params:', params);

    // TODO: this.configMissingService.testMailer(params).subscribe(...)
    this._simulateTest(this.testMailer,
      'Connexion SMTP établie avec succès.',
      'Impossible de joindre le serveur SMTP. Vérifiez l\'hôte et le port.'
    );
  }

  testReferentielConnection(): void {
    const v = this.configForm.value;
    const params = {
      login:      v.referentiel_ws_login,
      password:   v.referentiel_ws_password,
      ldap_url:   v.referentiel_ws_ldap_url,
      apogee_url: v.referentiel_ws_apogee_url,
    };

    this.testReferentiel = { loading: true, result: null, message: '' };
    console.log('Test Référentiel WS params:', params);

    // TODO: this.configMissingService.testReferentiel(params).subscribe(...)
    this._simulateTest(this.testReferentiel,
      'Connexion au référentiel WS réussie.',
      'Échec de la connexion. Vérifiez les identifiants et les URLs.'
    );
  }

  testWebhookConnection(): void {
    const v = this.configForm.value;
    const params = {
      uri:   v.webhook_signature_uri,
      token: v.webhook_signature_token,
    };

    this.testWebhook = { loading: true, result: null, message: '' };
    console.log('Test Webhook params:', params);

    // TODO: this.configMissingService.testWebhook(params).subscribe(...)
    this._simulateTest(this.testWebhook,
      'Webhook joignable et token accepté.',
      'Le webhook n\'a pas répondu ou a rejeté le token.'
    );
  }

  testSireneConnection(): void {
    const v = this.configForm.value;
    const params = {
      url:   v.sirene_url,
      token: v.sirene_token,
    };

    this.testSirene = { loading: true, result: null, message: '' };
    console.log('Test API Sirène params:', params);

    // TODO: this.configMissingService.testSirene(params).subscribe(...)
    this._simulateTest(this.testSirene,
      'API Sirène accessible, token valide.',
      'Accès à l\'API Sirène refusé. Vérifiez l\'URL et le token INSEE.'
    );
  }

  /**
   * Simulation temporaire d'un appel async (à retirer lors de l'implémentation métier).
   * Remplace l'appel service par un délai fixe et un résultat aléatoire pour
   * valider le rendu visuel.
   */
  private _simulateTest(state: TestState, successMsg: string, errorMsg: string): void {
    setTimeout(() => {
      const ok = Math.random() > 0.4;
      state.loading = false;
      state.result  = ok ? 'success' : 'error';
      state.message = ok ? successMsg : errorMsg;
    }, 1200);
  }

  // ── Payload & soumission ──────────────────────────────────────

  buildAppPropertyPayload(): Record<string, string | null> {
    const v = this.configForm.value;
    return {
      'appli.url':                        v.appli_url ?? null,
      'appli.tokens':                     v.appli_tokens ?? null,
      'appli.jwt_secret':                 v.appli_jwt_secret ?? null,
      'appli.nb_jours_valide_token':      v.appli_nb_jours_valide_token?.toString() ?? null,
      'appli.mailer.protocol':            v.appli_mailer_protocol,
      'appli.mailer.host':                v.appli_mailer_host ?? null,
      'appli.mailer.port':                v.appli_mailer_port?.toString() ?? null,
      'appli.mailer.auth':                v.appli_mailer_auth || null,
      'appli.mailer.username':            v.appli_mailer_username,
      'appli.mailer.password':            v.appli_mailer_password,
      'appli.mailer.from':                v.appli_mailer_from ?? null,
      'appli.mailer.disable_delivery':    String(v.appli_mailer_disable_delivery),
      'appli.mailer.delivery_address':    v.appli_mailer_delivery_address ?? null,
      'referentiel.ws.login':             v.referentiel_ws_login,
      'referentiel.ws.password':          v.referentiel_ws_password,
      'referentiel.ws.ldap_url':          v.referentiel_ws_ldap_url,
      'referentiel.ws.apogee_url':        v.referentiel_ws_apogee_url,
      'webhook.signature.uri':            v.webhook_signature_uri ?? null,
      'webhook.signature.token':          v.webhook_signature_token ?? null,
      'sirene.url':                       v.sirene_url ?? null,
      'sirene.token':                     v.sirene_token ?? null,
      'sirene.nombre_minimum_resultats':  v.sirene_nombre_minimum_resultats?.toString() ?? null,
      'appli.footer.github':              v.appli_footer_github ?? null,
      'appli.footer.site':                v.appli_footer_site ?? null,
      'appli.footer.support':             v.appli_footer_support ?? null,
      'appli.footer.wiki':                v.appli_footer_wiki ?? null,
    };
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      return;
    }

    const payload = this.buildAppPropertyPayload();
    console.log('AppProperty payload:', payload);
    // TODO: this.configMissingService.saveAll(payload).subscribe(...)

    this.savedSuccess = true;
    setTimeout(() => this.savedSuccess = false, 4000);
  }

  onReset(): void {
    this.submitted = false;
    this.savedSuccess = false;
    this.testMailer      = emptyTest();
    this.testReferentiel = emptyTest();
    this.testWebhook     = emptyTest();
    this.testSirene      = emptyTest();
    this.configForm.reset({
      appli_mailer_protocol: 'smtp',
      appli_mailer_disable_delivery: false,
    });
  }
}
