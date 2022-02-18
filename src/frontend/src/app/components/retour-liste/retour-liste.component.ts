import { Component, Input, OnInit } from '@angular/core';
import { Router } from "@angular/router";

@Component({
  selector: 'app-retour-liste',
  templateUrl: './retour-liste.component.html',
  styleUrls: ['./retour-liste.component.scss']
})
export class RetourListeComponent implements OnInit {

  @Input() path!: string;
  @Input() libelle: string = 'Retour à la liste';

  constructor(private router: Router) { }

  ngOnInit(): void {
  }

  backTolist(): void {
    this.router.navigate([this.path]);
  }

}
