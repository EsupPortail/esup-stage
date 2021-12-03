import { Component, OnInit, Input } from '@angular/core';

@Component({
  selector: 'app-recapitulatif',
  templateUrl: './recapitulatif.component.html',
  styleUrls: ['./recapitulatif.component.scss']
})
export class RecapitulatifComponent implements OnInit {

  @Input() convention: any;
  isValide: boolean = false;

  constructor() {
  }

  ngOnInit(): void {
    this.isValide = this.convention.validationPedagogique && this.convention.validationConvention;
  }

  validate(): void {
    //TODO send mail
  }

}
