import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-recapitulatif',
  templateUrl: './recapitulatif.component.html',
  styleUrls: ['./recapitulatif.component.scss']
})
export class RecapitulatifComponent implements OnInit {

  @Input() convention: any;

  constructor() {
  }

  ngOnInit(): void {
  }

  validate(): void {
    //TODO send mail
  }

}
