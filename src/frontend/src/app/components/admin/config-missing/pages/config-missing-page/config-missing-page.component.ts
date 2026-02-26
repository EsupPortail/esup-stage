import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { AppPropertyDto, ConfigAppService } from "../../../../../services/config-app.service";
import { ConfigMissingMailerTestDialogComponent } from "./mailer-test-dialog/config-missing-mailer-test-dialog.component";
import { ConfigMissingWebhookTestDialogComponent } from "./webhook-test-dialog/config-missing-webhook-test-dialog.component";

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

  private static readonly SIRENE_TEST_SIRET = '13001550600012';

  missingKeys: string[] = [];
  configReady = false;
  showReadyActions = false;
  configForm!: FormGroup;
  submitted = false;
  savedSuccess = false;

  testMailer:      TestState = emptyTest();
  testReferentiel: TestState = emptyTest();
  testWebhook:     TestState = emptyTest();
  testSirene:      TestState = emptyTest();

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

  private readonly FIELD_TO_KEY: Record<string, string> = {
    appli_url: 'appli.url',
    appli_tokens: 'appli.tokens',
    appli_jwt_secret: 'appli.jwt_secret',
    appli_nb_jours_valide_token: 'appli.nb_jours_valide_token',

    appli_mailer_protocol: 'appli.mailer.protocol',
    appli_mailer_host: 'appli.mailer.host',
    appli_mailer_port: 'appli.mailer.port',
    appli_mailer_auth: 'appli.mailer.auth',
    appli_mailer_username: 'appli.mailer.username',
    appli_mailer_password: 'appli.mailer.password',
    appli_mailer_from: 'appli.mailer.from',
    appli_mailer_disable_delivery: 'appli.mailer.disable_delivery',
    appli_mailer_delivery_address: 'appli.mailer.delivery_address',

    referentiel_ws_login: 'referentiel.ws.login',
    referentiel_ws_password: 'referentiel.ws.password',
    referentiel_ws_ldap_url: 'referentiel.ws.ldap_url',
    referentiel_ws_apogee_url: 'referentiel.ws.apogee_url',

    webhook_signature_uri: 'webhook.signature.uri',
    webhook_signature_token: 'webhook.signature.token',

    sirene_url: 'sirene.url',
    sirene_token: 'sirene.token',
    sirene_nombre_minimum_resultats: 'sirene.nombre_minimum_resultats',

    appli_footer_github: 'appli.footer.github',
    appli_footer_site: 'appli.footer.site',
    appli_footer_support: 'appli.footer.support',
    appli_footer_wiki: 'appli.footer.wiki',
  };

  private readonly KEY_TO_FIELD: Record<string, string> = Object.entries(this.FIELD_TO_KEY)
    .reduce((acc, [field, key]) => {
      acc[key] = field;
      return acc;
    }, {} as Record<string, string>);

  get displayMissingKeys(): string[] {
    return this.missingKeys.filter((key) => this.isMissing(key));
  }

  constructor(
    private configAppService: ConfigAppService,
    private fb: FormBuilder,
    private dialog: MatDialog,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.buildForm();

    this.loadMissingKeys();
    this.loadProperties();
  }

  private buildForm(): void {
    this.configForm = this.fb.group({
      appli_url: [null],
      appli_tokens: [null],
      appli_jwt_secret: [null],
      appli_nb_jours_valide_token: [null],

      appli_mailer_protocol: ['smtp'],
      appli_mailer_host: [null],
      appli_mailer_port: [null],
      appli_mailer_auth: [''],
      appli_mailer_username: ['username@domaine.com'],
      appli_mailer_password: ['xxx'],
      appli_mailer_from: [null],
      appli_mailer_disable_delivery: [false],
      appli_mailer_delivery_address: [null],

      referentiel_ws_login: [null, Validators.required],
      referentiel_ws_password: [null, Validators.required],
      referentiel_ws_ldap_url: [null, Validators.required],
      referentiel_ws_apogee_url: [null, Validators.required],

      webhook_signature_uri: [null],
      webhook_signature_token: [null],

      sirene_url: [null],
      sirene_token: [null],
      sirene_nombre_minimum_resultats: [null],

      appli_footer_github: [null],
      appli_footer_site: [null],
      appli_footer_support: [null],
      appli_footer_wiki: [null],
    });
  }


  isInvalid(field: string): boolean {
    const ctrl = this.configForm.get(field);
    return !!ctrl && ctrl.invalid && (ctrl.touched || this.submitted);
  }

  isMissing(key: string): boolean {
    if (!this.missingKeys.includes(key)) {
      return false;
    }
    const field = this.KEY_TO_FIELD[key];
    if (!field) {
      return true;
    }
    const ctrl = this.configForm?.get(field);
    if (!ctrl) {
      return true;
    }
    const value = ctrl.value;
    return value === null || value === undefined || value === '';
  }


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
    return !!v.webhook_signature_uri;
  }

  isSireneTestable(): boolean {
    const v = this.configForm.value;
    return !!(v.sirene_url && v.sirene_token);
  }

  openMailerTestDialog(): void {
    if (!this.isMailerTestable()) {
      return;
    }
    const dialogRef = this.dialog.open(ConfigMissingMailerTestDialogComponent, {
      data: {
        mailto: null,
        subject: null,
        content: null,
      },
      panelClass: 'config-missing-mailer-dialog',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }
      this.testMailerConnection(result);
    });
  }

  testMailerConnectionOnly(): void {
    this.testMailerConnection();
  }

  testMailerConnection(testPayload?: { mailto: string; subject: string; content: string }): void {
    const v = this.configForm.value;
    const params = {
      protocol: v.appli_mailer_protocol,
      host:     v.appli_mailer_host,
      port:     v.appli_mailer_port,
      auth:     v.appli_mailer_auth,
      username: v.appli_mailer_username,
      password: v.appli_mailer_password,
      mailto: testPayload?.mailto,
      subject: testPayload?.subject,
      content: testPayload?.content,
    };

    this.testMailer = { loading: true, result: null, message: '' };
    this.configAppService.testMailer(params).subscribe({
      next: (resp) => this.applyTestResult(this.testMailer, resp),
      error: () => this.applyTestResult(this.testMailer, {
        result: 'error',
        message: 'Erreur lors du test SMTP (non implémenté ou indisponible).'
      })
    });
  }

  testReferentielConnection(): void {
    const v = this.configForm.value;
    const params = {
      login: v.referentiel_ws_login,
      password: v.referentiel_ws_password,
      ldapUrl: v.referentiel_ws_ldap_url,
      apogeeUrl: v.referentiel_ws_apogee_url,
    };

    this.testReferentiel = { loading: true, result: null, message: '' };
    this.configAppService.testReferentiel(params).subscribe({
      next: (resp) => this.applyTestResult(this.testReferentiel, resp),
      error: () => this.applyTestResult(this.testReferentiel, {
        result: 'error',
        message: 'Erreur lors du test WS (non implémenté ou indisponible).'
      })
    });
  }

  openWebhookTestDialog(): void {
    if (!this.isWebhookTestable()) {
      return;
    }
    const dialogRef = this.dialog.open(ConfigMissingWebhookTestDialogComponent, {
      data: {
        suffix: null,
      },
      panelClass: 'config-missing-mailer-dialog',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (!result) {
        return;
      }
      this.testWebhookConnection(result?.suffix);
    });
  }

  private normalizeWebhookBase(base: string): string {
    let value = base.trim();
    try {
      value = decodeURIComponent(value);
    } catch {
      // keep raw value if decoding fails
    }
    const match = value.match(/webhook\.signature\.uri(?:=|%3D)(.+)/i);
    if (match && match[1]) {
      return match[1].trim();
    }
    return value;
  }

  private buildWebhookUrl(base: string, suffix?: string): string {
    const trimmedBase = this.normalizeWebhookBase(base);
    const trimmedSuffix = (suffix ?? '').trim();
    if (!trimmedSuffix) {
      return trimmedBase;
    }
    const baseEnds = trimmedBase.endsWith('/');
    const suffixStarts = trimmedSuffix.startsWith('/');
    if (baseEnds && suffixStarts) {
      return trimmedBase + trimmedSuffix.slice(1);
    }
    if (!baseEnds && !suffixStarts) {
      return trimmedBase + '/' + trimmedSuffix;
    }
    return trimmedBase + trimmedSuffix;
  }

  testWebhookConnection(suffix?: string): void {
    const v = this.configForm.value;
    const uri = this.buildWebhookUrl(v.webhook_signature_uri, suffix);
    const params = {
      uri,
      token: v.webhook_signature_token,
    };

    this.testWebhook = { loading: true, result: null, message: '' };
    this.configAppService.testWebhook(params).subscribe({
      next: (resp) => this.applyTestResult(this.testWebhook, resp),
      error: () => this.applyTestResult(this.testWebhook, {
        result: 'error',
        message: 'Erreur lors du test webhook (non implémenté ou indisponible).'
      })
    });
  }

  testSireneConnection(): void {
    const v = this.configForm.value;
    const params = {
      url:   v.sirene_url,
      token: v.sirene_token,
      siret: ConfigMissingPageComponent.SIRENE_TEST_SIRET,
    };

    this.testSirene = { loading: true, result: null, message: '' };
    this.configAppService.testSirene(params).subscribe({
      next: (resp) => this.applyTestResult(this.testSirene, resp),
      error: () => this.applyTestResult(this.testSirene, {
        result: 'error',
        message: 'Erreur lors du test API Sirène (non implémenté ou indisponible).'
      })
    });
  }

  private applyTestResult(state: TestState, resp: { result: 'success' | 'error'; message: string }): void {
    state.loading = false;
    state.result = resp.result;
    state.message = resp.message || '';
  }

  buildAppPropertyPayload(): AppPropertyDto[] {
    const v = this.configForm.value;
    const payload: Record<string, string | null> = {
      'appli.url':                        v.appli_url ?? null,
      'appli.tokens':                     v.appli_tokens ?? null,
      'appli.jwt_secret':                 v.appli_jwt_secret ?? null,
      'appli.nb_jours_valide_token':      v.appli_nb_jours_valide_token?.toString() ?? null,
      'appli.mailer.protocol':            v.appli_mailer_protocol ?? null,
      'appli.mailer.host':                v.appli_mailer_host ?? null,
      'appli.mailer.port':                v.appli_mailer_port?.toString() ?? null,
      'appli.mailer.auth':                v.appli_mailer_auth || null,
      'appli.mailer.username':            v.appli_mailer_username ?? null,
      'appli.mailer.password':            v.appli_mailer_password ?? null,
      'appli.mailer.from':                v.appli_mailer_from ?? null,
      'appli.mailer.disable_delivery':    String(v.appli_mailer_disable_delivery),
      'appli.mailer.delivery_address':    v.appli_mailer_delivery_address ?? null,
      'referentiel.ws.login':             v.referentiel_ws_login ?? null,
      'referentiel.ws.password':          v.referentiel_ws_password ?? null,
      'referentiel.ws.ldap_url':          v.referentiel_ws_ldap_url ?? null,
      'referentiel.ws.apogee_url':        v.referentiel_ws_apogee_url ?? null,
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

    return Object.entries(payload).map(([key, value]) => ({ key, value }));
  }

  private loadMissingKeys(afterSave = false): void {
    this.configAppService.getMissingKeys().subscribe({
      next: (resp) => {
        this.missingKeys = resp?.missing || [];
        this.configReady = this.missingKeys.length === 0;
        if (afterSave && this.configReady) {
          this.showReadyActions = true;
        }
      },
      error: () => {
        this.missingKeys = [];
        this.configReady = false;
      }
    });
  }

  private loadProperties(): void {
    this.configAppService.getProperties().subscribe({
      next: (properties) => {
        const values = this.mapPropertiesToForm(properties || []);
        this.configForm.patchValue(values, { emitEvent: false });
      },
      error: () => {}
    });
  }

  private mapPropertiesToForm(properties: AppPropertyDto[]): Record<string, unknown> {
    const map: Record<string, string | null> = {};
    for (const prop of properties) {
      if (!prop || !prop.key) {
        continue;
      }
      map[prop.key] = prop.value ?? null;
    }

    const formValues: Record<string, unknown> = {};
    Object.entries(this.FIELD_TO_KEY).forEach(([field, key]) => {
      const value = map[key] ?? null;
      switch (field) {
        case 'appli_mailer_disable_delivery':
          formValues[field] = value === 'true';
          break;
        case 'appli_mailer_port':
        case 'appli_nb_jours_valide_token':
        case 'sirene_nombre_minimum_resultats':
          formValues[field] = value !== null ? Number(value) : null;
          break;
        default:
          formValues[field] = value;
      }
    });

    return formValues;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      return;
    }

    const payload = this.buildAppPropertyPayload();
    this.configAppService.saveProperties(payload).subscribe({
      next: () => {
        this.savedSuccess = true;
        setTimeout(() => this.savedSuccess = false, 4000);
        this.showReadyActions = false;
        this.loadMissingKeys(true);
      },
      error: () => {
        this.savedSuccess = false;
      }
    });
  }

  goToHome(): void {
    this.router.navigateByUrl('/');
  }

  continueConfig(): void {
    this.showReadyActions = false;
  }

  isOnConfigMissingRoute(): boolean {
    return this.router.url.startsWith('/config-missing');
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
