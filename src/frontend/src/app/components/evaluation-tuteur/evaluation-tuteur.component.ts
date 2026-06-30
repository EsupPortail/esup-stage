import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TokenAuthService } from './services/token-auth.service';
import { ConventionEvaluationTuteur } from './models/convention-evaluation-tuteur.model';
import {MessageService} from "../../services/message.service";
import {EvaluationTuteurContextService} from "./services/evaluation-tuteur-context.service";


@Component({
    selector: 'app-evaluation-tuteur',
    templateUrl: './evaluation-tuteur.component.html',
    styleUrl: './evaluation-tuteur.component.scss',
    standalone: false
})
export class EvaluationTuteurComponent implements OnInit {

  token: string | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  convention!: ConventionEvaluationTuteur;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tokenAuthService: TokenAuthService,
    private messageService: MessageService,
    private ctx: EvaluationTuteurContextService
  ) {}

  ngOnInit() {
    // Recuperer le token depuis l'URL ou la session de navigation.
    this.token = this.getTokenFromUrl() || this.ctx.getStoredToken();
    if (this.token) {
      this.ctx.setToken(this.token);
      this.ensureTokenInUrl();
    }
    // Valider immédiatement le token et, si OK, naviguer vers l'accueil
    this.validateTokenAndRedirect();
  }

  /**
   * Valide le token et redirige vers la page d'accueil; en cas d'erreur, log sans redirection
   */
  private validateTokenAndRedirect(): void {
    if (!this.token){
      this.messageService.setError("Token manquant ou session expiree");
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    this.tokenAuthService.validateTokenAndGetConvention(this.token).subscribe({
      next: (convention: ConventionEvaluationTuteur) => {
        this.isLoading = false;
        this.convention = convention;
        this.ctx.setToken(this.token);
        this.ctx.setConvention(convention);
        if (this.isRootEvaluationUrl()) {
          this.router.navigate(['/evaluation-tuteur', convention.id], {
            queryParams: { token: this.token }
          });
        }
      },
      error: (error) => {
        this.isLoading = false;
        this.ctx.setToken(null);
        console.error('Erreur lors de la validation du token:', error);
      }
    });
  }

  private getTokenFromUrl(): string | null {
    const routeToken = this.route.snapshot.queryParamMap.get('token') || this.route.snapshot.queryParams['token'];
    if (routeToken) return routeToken;

    const searchToken = new URLSearchParams(window.location.search).get('token');
    if (searchToken) return searchToken;

    const hash = window.location.hash;
    const queryIndex = hash.indexOf('?');
    if (queryIndex === -1) return null;

    return new URLSearchParams(hash.substring(queryIndex + 1)).get('token');
  }

  private ensureTokenInUrl(): void {
    if (this.route.snapshot.queryParamMap.get('token')) return;
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { token: this.token },
      queryParamsHandling: 'merge',
      replaceUrl: true,
    });
  }
  private isRootEvaluationUrl(): boolean {
    return this.router.url.split('?')[0] === '/evaluation-tuteur';
  }
}
