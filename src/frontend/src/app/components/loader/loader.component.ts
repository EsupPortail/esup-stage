import { Component, OnInit } from '@angular/core';
import { LoaderService } from "../../services/loader.service";
import {Subscription} from "rxjs";

@Component({
    selector: 'app-loader',
    templateUrl: './loader.component.html',
    styleUrls: ['./loader.component.scss'],
    standalone: false
})
export class LoaderComponent implements OnInit {

  loadingMessage = '';
  private sub!: Subscription;

  constructor(public loaderService: LoaderService) {}

  ngOnInit(): void {
    this.sub = this.loaderService.isLoading.subscribe(isLoading => {
      if (isLoading) {
        this.loadingMessage = 'Chargement en cours…';
      } else {
        this.loadingMessage = 'Chargement terminé';
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

}
