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
    // Récupérer le token depuis les query params uniquement
    this.token = this.route.snapshot.queryParams['token'];
    // Valider immédiatement le token et, si OK, naviguer vers l'accueil
    this.validateTokenAndRedirect();
  }

  /**
   * Valide le token et redirige vers la page d'accueil; en cas d'erreur, log sans redirection
   */
  private validateTokenAndRedirect(): void {
    if (!this.token){
      this.messageService.setError("Token manquant dans l'URL");
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
        this.router.navigate(['/evaluation-tuteur', convention.id], {
          queryParams: { token: this.token }
        });
        console.log("convention : ", this.convention)
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Erreur lors de la validation du token:', error);
      }
    });
  }
}
