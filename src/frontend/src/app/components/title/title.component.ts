import { Component, OnInit } from '@angular/core';
import { TitleService } from "../../services/title.service";

@Component({
    selector: 'app-title',
    templateUrl: './title.component.html',
    styleUrls: ['./title.component.scss'],
    standalone: false
})
export class TitleComponent implements OnInit {

  constructor(private titleService: TitleService) { }

  ngOnInit(): void {
  }

  getTitle(): string {
    return this.titleService.title;
  }

}
