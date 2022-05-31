import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { MatTabChangeEvent, MatTabGroup } from "@angular/material/tabs";
import { TitleService } from "../../services/title.service";
import { AuthService } from "../../services/auth.service";

@Component({
  selector: 'app-convention-create-en-masse',
  templateUrl: './convention-create-en-masse.component.html',
  styleUrls: ['./convention-create-en-masse.component.scss']
})
export class ConventionCreateEnMasseComponent implements OnInit {

  conventionTabIndex: number = 0;

  tabs: any = {
    0: { statut: 0, init: true },
  }

  constructor(private activatedRoute: ActivatedRoute,
              private titleService: TitleService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }
}
