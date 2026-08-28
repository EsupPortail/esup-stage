import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { PageEvent } from '@angular/material/paginator';
import { ApiToken, ApiTokenSecret, ApiTokenService } from '../../../services/api-token.service';
import { MessageService } from '../../../services/message.service';
import {
  ApiTokenCreateDialogComponent,
  ApiTokenFormResult
} from './create-dialog/api-token-create-dialog.component';
import { ApiTokenValueDialogComponent } from './token-value-dialog/api-token-value-dialog.component';

@Component({
  selector: 'app-api-tokens',
  templateUrl: './api-tokens.component.html',
  styleUrl: './api-tokens.component.scss',
  standalone: false
})
export class ApiTokensComponent implements OnInit {

  apiTokens: ApiToken[] = [];
  page: number = 1;
  itemsPerPage: number = 10;
  totalTokens: number = 0;
  sortPredicate: string = 'id';
  sortOrder: string = 'asc';
  filtersObj: any = {};
  private filterTimeout: any;

  constructor(
    private apiTokenService: ApiTokenService,
    private messageService: MessageService,
    private dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.getPaginated();
  }

  getPaginated(): void {
    this.apiTokenService.getPaginated(this.page, this.itemsPerPage, this.sortPredicate, this.sortOrder, this.filters)
      .subscribe(response => {
        this.apiTokens = response.data;
        this.totalTokens = response.total;
      });
  }

  creer(): void {
    this.dialog.open(ApiTokenCreateDialogComponent, {width: '520px', data: {}})
      .afterClosed()
      .subscribe((form: ApiTokenFormResult | undefined) => {
        if (!form) return;
        this.apiTokenService.create(form).subscribe(secret => {
          this.getPaginated();
          this.afficherValeur('Token créé', secret);
        });
      });
  }

  modifier(apiToken: ApiToken): void {
    this.dialog.open(ApiTokenCreateDialogComponent, {width: '520px', data: {apiToken}})
      .afterClosed()
      .subscribe((form: ApiTokenFormResult | undefined) => {
        if (!form) return;
        this.apiTokenService.update(apiToken.id, form).subscribe(() => {
          this.messageService.setSuccess('Token modifié');
          this.getPaginated();
        });
      });
  }

  copier(apiToken: ApiToken): void {
    this.apiTokenService.reveal(apiToken.id).subscribe(secret => {
      this.afficherValeur('Valeur du token', secret);
    });
  }

  renouveler(apiToken: ApiToken): void {
    this.apiTokenService.renew(apiToken.id).subscribe(secret => {
      this.getPaginated();
      this.afficherValeur('Token renouvelé', secret);
    });
  }

  changerActivation(apiToken: ApiToken, actif: boolean): void {
    this.apiTokenService.setActif(apiToken.id, actif).subscribe({
      next: () => {
        this.messageService.setSuccess(actif ? 'Token activé' : 'Token désactivé');
        this.getPaginated();
      },
      // Remise du curseur dans son état réel si le serveur a refusé
      error: () => this.getPaginated(),
    });
  }

  supprimer(apiToken: ApiToken): void {
    this.apiTokenService.delete(apiToken.id).subscribe(() => {
      this.messageService.setSuccess('Token supprimé');
      this.getPaginated();
    });
  }

  private afficherValeur(titre: string, secret: ApiTokenSecret): void {
    this.dialog.open(ApiTokenValueDialogComponent, {
      width: '560px',
      data: {titre, nomApplication: secret.apiToken.nomApplication, token: secret.token}
    });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex + 1;
    this.itemsPerPage = event.pageSize;
    this.getPaginated();
  }

  get filters(): string {
    const f: any = {};
    if (this.filtersObj.nom && this.filtersObj.nom.trim() !== '') {
      f.nom = {type: 'text', value: this.filtersObj.nom};
    }
    if (this.filtersObj.nomApplication && this.filtersObj.nomApplication.trim() !== '') {
      f.nomApplication = {type: 'text', value: this.filtersObj.nomApplication};
    }
    if (this.filtersObj.actif !== '' && this.filtersObj.actif !== null && this.filtersObj.actif !== undefined) {
      f.actif = {type: 'boolean', value: this.filtersObj.actif};
    }
    return JSON.stringify(f);
  }

  onFilterChange(): void {
    if (this.filterTimeout) {
      clearTimeout(this.filterTimeout);
    }
    this.filterTimeout = setTimeout(() => {
      this.page = 1;
      this.getPaginated();
    }, 500);
  }

  sort(column: string): void {
    if (this.sortPredicate === column) {
      this.sortOrder = this.sortOrder === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortPredicate = column;
      this.sortOrder = 'asc';
    }
    this.getPaginated();
  }

  ariaSort(column: string): string {
    if (this.sortPredicate !== column) {
      return 'none';
    }
    return this.sortOrder === 'asc' ? 'ascending' : 'descending';
  }
}
