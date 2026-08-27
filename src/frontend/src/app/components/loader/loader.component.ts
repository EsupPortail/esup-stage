import { Component, OnInit, OnDestroy } from '@angular/core';
import { LoaderService } from "../../services/loader.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-loader',
  templateUrl: './loader.component.html',
  styleUrls: ['./loader.component.scss'],
  standalone: false
})
export class LoaderComponent implements OnInit, OnDestroy {
  loadingMessage = '';
  showLoader = false;

  private sub!: Subscription;
  private showLoaderTimeout?: number;
  private hideMessageTimeout?: number;

  private readonly MINIMUM_DISPLAY_TIME = 300; // ms - ne pas afficher si < 300ms
  private readonly MESSAGE_CLEAR_DELAY = 1000; // ms - effacer le message après 1s

  constructor(public loaderService: LoaderService) {}

  ngOnInit(): void {
    this.sub = this.loaderService.isLoading.subscribe(isLoading => {
      this.handleLoadingChange(isLoading);
    });
  }

  private handleLoadingChange(isLoading: boolean): void {
    // Nettoyer les timeouts existants
    if (this.showLoaderTimeout) {
      clearTimeout(this.showLoaderTimeout);
      this.showLoaderTimeout = undefined;
    }
    if (this.hideMessageTimeout) {
      clearTimeout(this.hideMessageTimeout);
      this.hideMessageTimeout = undefined;
    }

    if (isLoading) {
      // Attendre un peu avant d'afficher le loader
      // (évite le flash si le chargement est < 300ms)
      this.showLoaderTimeout = window.setTimeout(() => {
        this.showLoader = true;
        this.loadingMessage = 'Chargement en cours…';
      }, this.MINIMUM_DISPLAY_TIME);

    } else {
      // Chargement terminé
      if (this.showLoader) {
        // Le loader était visible, afficher le message de fin
        this.showLoader = false;
        this.loadingMessage = 'Chargement terminé';

        // Effacer le message après un délai
        this.hideMessageTimeout = window.setTimeout(() => {
          this.loadingMessage = '';
        }, this.MESSAGE_CLEAR_DELAY);
      } else {
        // Le loader n'était pas encore visible, ne rien faire
        this.loadingMessage = '';
      }
    }
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    if (this.showLoaderTimeout) {
      clearTimeout(this.showLoaderTimeout);
    }
    if (this.hideMessageTimeout) {
      clearTimeout(this.hideMessageTimeout);
    }
  }
}
