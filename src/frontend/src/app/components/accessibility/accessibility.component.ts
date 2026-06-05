import {Component, OnInit} from '@angular/core';
import {ContenuService} from "../../services/contenu.service";

@Component({
  selector: 'app-accessibility',
  templateUrl: './accessibility.component.html',
  styleUrl: './accessibility.component.scss',
  standalone : false
})
export class AccessibilityComponent implements OnInit {
  content !: string;

  constructor(private contenuService: ContenuService) { }

  ngOnInit() {
    this.contenuService.get('CONTENU_PAGE_ACCESSIBILITE').subscribe(res => {
        this.content = res.texte;
    });
  }
}
