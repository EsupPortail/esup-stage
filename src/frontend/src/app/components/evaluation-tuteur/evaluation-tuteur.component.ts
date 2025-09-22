import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TokenAuthService } from './services/token-auth.service';

@Component({
  selector: 'app-evaluation-tuteur',
  templateUrl: './evaluation-tuteur.component.html',
  styleUrl: './evaluation-tuteur.component.scss'
})
export class EvaluationTuteurComponent implements OnInit {

  token: string | null = null;
  isLoading = true;
  isAuthorized = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private tokenAuthService: TokenAuthService
  ) {}

  ngOnInit() {
    // Récupérer le token depuis les paramètres de route ou query params
    this.token = this.route.snapshot.queryParams['token'] ||
      this.route.snapshot.params['token'] ||
      this.route.parent?.snapshot.params['token'];

    if (!this.token) {
      console.warn('Token manquant, redirection vers page d\'erreur');
      this.redirectToError('Token d\'accès manquant', 'MISSING_TOKEN');
      return;
    }

    this.validateAccess();
  }

  private validateAccess(): void {
    if (!this.token) return;

    this.tokenAuthService.validateToken(this.token).subscribe({
      next: (isValid) => {
        this.isLoading = false;
        if (isValid) {
          this.isAuthorized = true;
          console.log('Accès autorisé à la page d\'évaluation');
        } else {
          this.redirectToError('Token invalide', 'INVALID_TOKEN');
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Erreur de validation:', error);

        let message = 'Erreur de validation du token';
        if (error.status === 403) {
          message = 'Accès non autorisé';
        }

        this.redirectToError(message, 'VALIDATION_ERROR');
      }
    });
  }

  private redirectToError(message: string, code: string): void {
    this.router.navigate(['/error'], {
      queryParams: { message, code }
    });
  }
}
