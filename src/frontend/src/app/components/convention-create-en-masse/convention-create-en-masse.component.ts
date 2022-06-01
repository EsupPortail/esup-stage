import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { GroupeEtudiantService } from "../../services/groupe-etudiant.service";
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

  groupeEtudiant: any;
  allValid = false;

  tabs: any = {
    0: { statut: 0, init: true },
    1: { statut: 0, init: true },
    2: { statut: 0, init: true },
  }

  constructor(private activatedRoute: ActivatedRoute,
              public groupeEtudiantService: GroupeEtudiantService,
              private titleService: TitleService,
              private authService: AuthService,
              private router: Router) {
  }

  ngOnInit(): void {
    this.groupeEtudiantService.getBrouillon().subscribe((response: any) => {

      console.log('response : ' + JSON.stringify(response, null, 2))
      this.groupeEtudiant = response;
      this.majStatus();
    });
  }

  majStatus(): void {
    if (this.groupeEtudiant){
      this.setStatus(0,2);
    }else{
      this.setStatus(0,0);
    }
  }

  setStatus(key: number, value: number): void {
    this.tabs[key].statut = value;
    this.majAllValid();
  }

  majAllValid(): void {
    this.allValid = true;
    for (let key in this.tabs) {
        if (this.tabs[key].statut !== 2) {
          this.allValid = false;
        }
    }
  }

  tabChanged(event: MatTabChangeEvent): void {
    this.tabs[event.index].init = true;
  }

  getProgressValue(key: number): number {
    if (this.tabs[key].statut === 1) return 66;
    if (this.tabs[key].statut === 2) return 100;
    return 33;
  }

  updateGroupeEtudiant(data: any): void {
    this.groupeEtudiant = data;
  }
}
