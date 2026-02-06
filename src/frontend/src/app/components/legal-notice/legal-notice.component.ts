import { Component } from '@angular/core';
import {ContenuService} from "../../services/contenu.service";

@Component({
  selector: 'app-legal-notice',
  templateUrl: './legal-notice.component.html',
  styleUrl: './legal-notice.component.scss',
  standalone: false
})
export class LegalNoticeComponent {

  content !: string;

  constructor(private contenuService: ContenuService) { }

  ngOnInit() {
    this.contenuService.get('CONTENU_PAGE_MENTIONS_LEGALES').subscribe(res => {
      this.content = res.texte;
    });
  }

}
